// Usage: node train.mjs <raw-dir> [out-dir]
// Mines MedDialog-EN into per-symptom decision trees (out/model.json) and an audit report.
import { mkdirSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import { loadDialogs, hash01 } from './lib/load.mjs'
import { SYMPTOMS, primarySymptom } from './lib/symptoms.mjs'
import { CONDITIONS, conditionMentions } from './lib/conditions.mjs'
import { FACETS, SYMPTOM_FACETS } from './lib/facets.mjs'
import { SYMPTOM_META, RED_FLAGS } from './lib/safety.mjs'
import { trainTree, pruneTree, meanLogLik, route, topK } from './lib/tree.mjs'

const countSplits = (n) => (n.leaf ? 0 : 1 + countSplits(n.yes) + countSplits(n.no))

const rawDir = process.argv[2]
const outDir = process.argv[3] || join(import.meta.dirname, 'out')
mkdirSync(outDir, { recursive: true })

const MIN_DIALOGS = 2000 // a complaint becomes an app category only if this common
const EXCLUDED = {
  sexual: 'Needs a private clinical consultation; symptom questions cannot triage it safely.',
  dental: 'Needs a dentist examination.',
  bp: 'A measurement rather than a symptom.',
}
const MAX_COND = 8
const MIN_COND_PREV = 0.04
const TREE = { maxDepth: 3, minGain: 0.01 }
const URGENT_RE = /emergency|hospital|immediate|urgent|ambulance|right away|at once|\ber\b|casualty/

const dialogs = await loadDialogs(rawDir)
const bySymptom = {}
for (const d of dialogs) {
  const s = primarySymptom(d)
  if (s) (bySymptom[s] ||= []).push(d)
}

const counts = Object.fromEntries(Object.entries(bySymptom).map(([k, v]) => [k, v.length]))
const ranked = Object.entries(counts).sort((a, b) => b[1] - a[1])
const selected = ranked.filter(([k, n]) => n >= MIN_DIALOGS && SYMPTOM_META[k]).map(([k]) => k)
const excludedReport = ranked
  .filter(([k, n]) => n >= MIN_DIALOGS && !SYMPTOM_META[k])
  .map(([k, n]) => ({ symptom: k, dialogs: n, reason: EXCLUDED[k] || 'Not triage-relevant.' }))

const model = {
  generatedAt: new Date().toISOString(),
  corpus: {
    dialogs: dialogs.length,
    subsets: [...new Set(dialogs.map((d) => d.subset))],
    chiefComplaintMatched: Object.values(counts).reduce((a, b) => a + b, 0),
  },
  selectionRule: `Chief complaints present in at least ${MIN_DIALOGS} dialogs, excluding non-triage topics`,
  excluded: excludedReport,
  symptoms: {},
}

const condNames = Object.keys(CONDITIONS)

for (const s of selected) {
  const rows = bySymptom[s]
  const facetKeys = SYMPTOM_FACETS[s]
  if (!facetKeys) throw new Error(`no facet list for ${s}`)

  // Per-row condition mentions.
  const mentions = rows.map((d) => conditionMentions(d.doctor))
  const prev = {}
  for (const m of mentions) for (const c of m) prev[c] = (prev[c] || 0) + 1
  const cands = Object.entries(prev)
    .filter(([, k]) => k / rows.length >= MIN_COND_PREV)
    .sort((a, b) => b[1] - a[1])
    .slice(0, MAX_COND)
    .map(([c]) => c)

  const N = rows.length
  const feats = facetKeys.map((k) => {
    const arr = new Uint8Array(N)
    for (let i = 0; i < N; i++) arr[i] = FACETS[k].re.test(rows[i].patient) ? 1 : 0
    return arr
  })
  const labels = cands.map((c) => {
    const arr = new Uint8Array(N)
    for (let i = 0; i < N; i++) arr[i] = mentions[i].includes(c) ? 1 : 0
    return arr
  })

  // 70% grow / 15% prune / 15% untouched test.
  const train = []
  const val = []
  const hold = []
  for (let i = 0; i < N; i++) {
    const h = hash01(rows[i].id)
    ;(h < 0.7 ? train : h < 0.85 ? val : hold).push(i)
  }
  const minLeaf = Math.max(60, Math.round(0.03 * train.length))
  const grown = trainTree(train, feats, labels, { ...TREE, minLeaf })
  const splitsBefore = countSplits(grown)
  const tree = pruneTree(grown, val, feats, labels)
  const splitsAfter = countSplits(tree)

  // Test evaluation (rows with at least one candidate label for hit@3; all rows for log-lik).
  const rootNode = { leaf: true, n: tree.n, probs: tree.probs }
  const rootTop3 = topK(tree.probs, 3)
  let labelled = 0
  let hitTree = 0
  let hitBase = 0
  for (const i of hold) {
    const own = new Set(cands.map((_, c) => c).filter((c) => labels[c][i]))
    if (!own.size) continue
    labelled++
    const leaf = route(tree, i, feats)
    if (topK(leaf.probs, 3).some((c) => own.has(c))) hitTree++
    if (rootTop3.some((c) => own.has(c))) hitBase++
  }
  const llTree = meanLogLik(tree, hold, feats, labels)
  const llBase = meanLogLik(rootNode, hold, feats, labels)

  // Facet statistics: patient mention rate and how often doctors ask about it.
  const facetStats = {}
  const used = new Set()
  ;(function collect(n) {
    if (n.leaf) return
    used.add(n.f)
    collect(n.yes)
    collect(n.no)
  })(tree)
  for (const f of used) {
    const key = facetKeys[f]
    let said = 0
    let asked = 0
    for (let i = 0; i < N; i++) {
      if (feats[f][i]) said++
      const qs = rows[i].doctor.split(/(?<=[.?!])\s+/).filter((x) => x.includes('?'))
      if (qs.some((x) => FACETS[key].re.test(x))) asked++
    }
    facetStats[key] = { patientPct: +(100 * said / N).toFixed(1), doctorAskPct: +(100 * asked / N).toFixed(1) }
  }

  // Audit curated red flags against real doctor language.
  const baseUrgent = rows.filter((d) => URGENT_RE.test(d.doctor)).length / N
  const flagAudit = (RED_FLAGS[s] || []).map((fl) => {
    const m = rows.filter((d) => fl.audit.test(d.patient))
    const u = m.filter((d) => URGENT_RE.test(d.doctor)).length
    return {
      id: fl.id,
      mentions: m.length,
      urgentPct: m.length ? +(100 * u / m.length).toFixed(1) : null,
      baseUrgentPct: +(100 * baseUrgent).toFixed(1),
    }
  })

  const ser = (n) =>
    n.leaf
      ? { leaf: true, n: n.n, probs: Object.fromEntries(cands.map((c, i) => [c, +n.probs[i].toFixed(4)])) }
      : {
          leaf: false,
          facet: facetKeys[n.f],
          gain: +n.gain.toFixed(4),
          n: n.n,
          yesShare: +n.yesShare.toFixed(3),
          probs: Object.fromEntries(cands.map((c, i) => [c, +n.probs[i].toFixed(4)])),
          yes: ser(n.yes),
          no: ser(n.no),
        }

  model.symptoms[s] = {
    dialogs: N,
    train: train.length,
    validation: val.length,
    holdout: hold.length,
    minLeaf,
    splitsBeforePruning: splitsBefore,
    splitsAfterPruning: splitsAfter,
    conditions: cands,
    facetStats,
    eval: {
      labelledHoldout: labelled,
      hit3Tree: labelled ? +(hitTree / labelled).toFixed(3) : null,
      hit3Baseline: labelled ? +(hitBase / labelled).toFixed(3) : null,
      meanLogLikTree: +llTree.toFixed(4),
      meanLogLikBaseline: +llBase.toFixed(4),
      logLikGain: +(llTree - llBase).toFixed(4),
    },
    flagAudit,
    tree: ser(tree),
  }
  const e = model.symptoms[s].eval
  console.error(`${s.padEnd(12)} n=${String(N).padStart(6)} splits ${splitsBefore}->${splitsAfter}  hit@3 tree=${e.hit3Tree} base=${e.hit3Baseline}  logLik gain=${e.logLikGain}`)
}

writeFileSync(join(outDir, 'model.json'), JSON.stringify(model, null, 1))
console.error(`wrote ${join(outDir, 'model.json')}`)
