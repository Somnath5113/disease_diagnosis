// Usage: node emit.mjs [app-assets-dir]
// Combines out/model.json (mined from MedDialog) with the curated safety layer, advice text
// and clinical follow-up questions into the app's triage_tree.json. Validates before writing.
//
// Flow per complaint:  red-flag screen -> up to 3 MedDialog-derived questions
//                      -> up to 7 clinical follow-up questions -> assessment.
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { join } from 'node:path'
import { FACETS } from './lib/facets.mjs'
import { KB, FALLBACK } from './lib/kb.mjs'
import { KB_DEEP } from './lib/kb_deep.mjs'
import { REMEDIES, REMEDIES_FALLBACK } from './lib/remedies.mjs'
import { DEEP } from './lib/deep.mjs'
import { createPolicy } from './lib/policy.mjs'
import { SYMPTOM_META, RED_FLAGS, EMERGENCY_KINDS, GROUPS } from './lib/safety.mjs'

const here = import.meta.dirname
const model = JSON.parse(readFileSync(join(here, 'out', 'model.json'), 'utf8'))
const assetsDir = process.argv[2] || join(here, '..', '..', 'app', 'src', 'main', 'assets')
const ADVICE = { ...KB, ...KB_DEEP }
for (const k of Object.keys(KB_DEEP)) if (KB[k]) throw new Error(`advice defined twice: ${k}`)
for (const k of Object.keys(REMEDIES)) if (!ADVICE[k]) throw new Error(`remedies for unknown condition: ${k}`)
for (const k of Object.keys(REMEDIES_FALLBACK)) if (!FALLBACK[k]) throw new Error(`remedies fallback for unknown symptom: ${k}`)

const SHOW_MIN = 0.08 // stage-1 intro lists conditions doctors discussed in >= 8% of the leaf's dialogs
const SHOW_MAX = 3
const pct = (p) => `${Math.round(p * 100)}%`
const nodes = {}
const categories = []
const qmap = {} // node id -> { sym, qid } for verify_deep.mjs

// Nodes are built with every field filled in (so validation is simple) and written without the
// defaults that the Kotlin models already assume, which shrinks the shipped file considerably.
function addNode(n) {
  if (nodes[n.id]) throw new Error(`duplicate node ${n.id}`)
  nodes[n.id] = {
    isEmergencyCheck: false,
    options: [],
    isConclusion: false,
    ...n,
    options: (n.options || []).map((o) => ({ subtitle: null, isRedFlag: false, ...o })),
  }
}

function compact(n) {
  const o = { id: n.id, category: n.category, question: n.question }
  if (n.explanation) o.explanation = n.explanation
  if (n.isEmergencyCheck) o.isEmergencyCheck = true
  if (n.options.length) o.options = n.options.map((p) => ({ text: p.text, ...(p.subtitle ? { subtitle: p.subtitle } : {}), nextId: p.nextId, ...(p.isRedFlag ? { isRedFlag: true } : {}) }))
  if (n.isConclusion) o.isConclusion = true
  if (n.conclusion) o.conclusion = n.conclusion
  return o
}

function stage1Intro(sym, leaf) {
  const shown = Object.entries(leaf.probs)
    .filter(([, p]) => p >= SHOW_MIN)
    .sort((a, b) => b[1] - a[1])
    .slice(0, SHOW_MAX)
  const name = SYMPTOM_META[sym].name
  const stats = shown.length ? ` In ${leaf.n} similar online doctor consultations, doctors most often discussed: ${shown.map(([n, p]) => `${n} (${pct(p)})`).join(', ')}. These are mention rates, not diagnoses.` : ''
  return `So far:${stats} The next questions follow standard clinical history-taking for “${name}” to narrow this down.`
}

for (const [sym, e] of Object.entries(model.symptoms)) {
  const meta = SYMPTOM_META[sym]
  const deep = DEEP[sym]
  if (!deep) throw new Error(`No follow-up questions for ${sym}`)
  const cat = sym.toUpperCase()
  const rfId = `${sym}_rf`
  const policy = createPolicy({
    sym,
    deep,
    KB: ADVICE,
    REMEDIES,
    remediesFallback: REMEDIES_FALLBACK[sym],
    floor: meta.floor,
    fallback: FALLBACK[sym],
    addNode: (n) => {
      addNode(n)
    },
  })
  let q = 0

  // Stage 1: MedDialog-derived tree. Its leaves hand over to the follow-up policy.
  function build(n, path) {
    if (n.leaf) return policy.enter(n.probs, path, stage1Intro(sym, n))
    const id = `${sym}_q${q++}`
    const yesId = build(n.yes, [...path, { facet: n.facet, yes: true }])
    const noId = build(n.no, [...path, { facet: n.facet, yes: false }])
    const fs = e.facetStats[n.facet]
    const asked = fs.doctorAskPct >= 1 ? ` Doctors asked about it in ${fs.doctorAskPct}% of these consultations.` : ''
    addNode({
      id,
      category: cat,
      question: FACETS[n.facet].q,
      explanation: `Why we ask: this was among the strongest clues separating the conditions doctors discussed in ${e.dialogs.toLocaleString('en-US')} online doctor consultations about “${meta.name}”. ${fs.patientPct}% of patients mentioned it.${asked}`,
      isEmergencyCheck: false,
      options: [
        { text: 'Yes', subtitle: null, nextId: yesId, isRedFlag: false },
        { text: 'No or not sure', subtitle: null, nextId: noId, isRedFlag: false },
      ],
      isConclusion: false,
    })
    return id
  }
  const rootId = build(e.tree, [])

  // Curated red-flag screen goes first.
  const flags = RED_FLAGS[sym]
  if (!flags?.length) throw new Error(`No red flags for ${sym}`)
  const options = []
  for (const fl of flags) {
    const kind = EMERGENCY_KINDS[fl.kind]
    if (!kind) throw new Error(`Unknown emergency kind ${fl.kind}`)
    const emId = `${sym}_em_${fl.id}`
    addNode({
      id: emId,
      category: cat,
      question: `${kind.urgency === 'EMERGENCY' ? 'Emergency' : 'Urgent'}: ${kind.title}`,
      explanation: 'This warning sign comes from standard clinical triage guidance.',
      isEmergencyCheck: true,
      options: [],
      isConclusion: true,
      conclusion: {
        urgencyLevel: kind.urgency,
        likelyConditions: kind.conditions,
        clinicalAdvice: kind.advice,
        homeCareInstructions: kind.care,
        whenToSeekImmediateHelp: kind.seek,
      },
    })
    options.push({ text: fl.text, subtitle: fl.subtitle, nextId: emId, isRedFlag: true })
  }
  options.push({ text: 'None of these', subtitle: 'Continue with the questions', nextId: rootId, isRedFlag: false })
  addNode({
    id: rfId,
    category: cat,
    question: 'First, do any of these apply to you right now?',
    explanation: 'These warning signs always need urgent care. They come from standard clinical triage guidance.',
    isEmergencyCheck: true,
    options,
    isConclusion: false,
  })

  // Map follow-up question nodes back to their bank entries (for the simulation check).
  for (const [id, n] of Object.entries(nodes)) {
    if (n.category !== cat || n.isConclusion || n.isEmergencyCheck) continue
    const hit = deep.Q.find((d) => d.q === n.question)
    if (hit) qmap[id] = { sym, qid: hit.id }
  }

  categories.push({
    id: cat,
    name: meta.name,
    description: meta.keywords,
    icon: meta.group,
    group: meta.group,
    groupName: GROUPS[meta.group].name,
    startNodeId: rfId,
    dialogs: e.dialogs,
  })
}

// Most common complaints first, so the home screen reflects what patients actually ask about.
// Topics that are private or that many people would rather not see first (periods, pregnancy) go to
// the end of the list. They stay one search away and every other complaint keeps its frequency order.
const SHOWN_LAST = new Set(['PERIODS'])
categories.sort((a, b) => Number(SHOWN_LAST.has(a.id)) - Number(SHOWN_LAST.has(b.id)) || b.dialogs - a.dialogs)
for (const c of categories) delete c.dialogs

const out = {
  version: '3.0',
  metadata: {
    source: 'MedDialog-EN (iCliniq + HealthCareMagic), mined offline, plus clinical follow-up questions',
    description: `First questions and condition statistics learned from ${model.corpus.dialogs.toLocaleString('en-US')} real doctor-patient dialogs; follow-up questions (up to 10 layers in total) follow standard clinical history-taking; emergency red flags and advice are curated clinical guidance. This is an information tool, not a diagnosis.`,
  },
  categories,
  nodes,
}

// ---- validation -----------------------------------------------------------
const errors = []
for (const c of categories) if (!nodes[c.startNodeId]) errors.push(`missing start ${c.startNodeId}`)
for (const [id, n] of Object.entries(nodes)) {
  if (n.id !== id) errors.push(`id mismatch ${id}`)
  if (n.isConclusion) {
    if (n.options.length) errors.push(`${id}: conclusion has options`)
    if (!n.conclusion) errors.push(`${id}: conclusion missing`)
    else for (const cond of n.conclusion.likelyConditions) if (cond.includes(', ')) errors.push(`${id}: condition contains ", ": ${cond}`)
  } else if (!n.options.length) errors.push(`${id}: dead end`)
  for (const o of n.options) if (!nodes[o.nextId]) errors.push(`${id}: bad nextId ${o.nextId}`)
}
const reach = new Set()
const stack = categories.map((c) => c.startNodeId)
while (stack.length) {
  const id = stack.pop()
  if (reach.has(id)) continue
  reach.add(id)
  for (const o of nodes[id].options) stack.push(o.nextId)
}
for (const id of Object.keys(nodes)) if (!reach.has(id)) errors.push(`unreachable ${id}`)
if (errors.length) {
  console.error(errors.slice(0, 40).join('\n'))
  process.exit(1)
}

mkdirSync(assetsDir, { recursive: true })
const shipped = { ...out, nodes: Object.fromEntries(Object.entries(nodes).map(([id, n]) => [id, compact(n)])) }
const json = JSON.stringify(shipped) + '\n' // minified: the tree is generated, never hand-edited
writeFileSync(join(assetsDir, 'triage_tree.json'), json)
mkdirSync(join(here, 'out'), { recursive: true })
writeFileSync(join(here, 'out', 'qmap.json'), JSON.stringify(qmap))
const leaves = Object.values(nodes).filter((n) => n.isConclusion && !n.isEmergencyCheck).length
console.error(`wrote triage_tree.json: ${categories.length} categories, ${Object.keys(nodes).length} nodes (${leaves} assessments), ${(json.length / 1024).toFixed(0)} KB`)
