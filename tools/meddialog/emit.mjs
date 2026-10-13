// Usage: node emit.mjs [app-assets-dir]
// Combines out/model.json (mined from MedDialog) with the curated safety layer and
// advice text into the app's triage_tree.json. Validates the tree before writing.
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { join } from 'node:path'
import { FACETS } from './lib/facets.mjs'
import { KB, FALLBACK } from './lib/kb.mjs'
import { SYMPTOM_META, RED_FLAGS, EMERGENCY_KINDS, GROUPS, URGENCY_RANK, maxUrgency } from './lib/safety.mjs'

const here = import.meta.dirname
const model = JSON.parse(readFileSync(join(here, 'out', 'model.json'), 'utf8'))
const assetsDir = process.argv[2] || join(here, '..', '..', 'app', 'src', 'main', 'assets')

const SHOW_MIN = 0.08 // a condition is shown only if doctors discussed it in >= 8% of the leaf's dialogs
const SHOW_MAX = 3
const pct = (p) => `${Math.round(p * 100)}%`
const nodes = {}
const categories = []

function addNode(n) {
  if (nodes[n.id]) throw new Error(`duplicate node ${n.id}`)
  nodes[n.id] = n
}

function conclusionForLeaf(sym, leaf) {
  const meta = SYMPTOM_META[sym]
  const shown = Object.entries(leaf.probs)
    .filter(([, p]) => p >= SHOW_MIN)
    .sort((a, b) => b[1] - a[1])
    .slice(0, SHOW_MAX)

  let urgency = meta.floor
  for (const [name, p] of shown) {
    const kb = KB[name]
    if (!kb) throw new Error(`No KB entry for "${name}" (symptom ${sym})`)
    if (p >= (kb.serious ? 0.1 : 0.15)) urgency = maxUrgency(urgency, kb.urgency)
  }
  // Safety: lead with the most urgent condition that crossed its threshold, not merely
  // the most probable one, so reassuring advice never sits above a serious possibility.
  const raised = shown.filter(([name, p]) => p >= (KB[name].serious ? 0.1 : 0.15) && URGENCY_RANK[KB[name].urgency] >= URGENCY_RANK[urgency])
  const lead = raised.length ? raised[0] : shown[0]

  const fb = FALLBACK[sym]
  if (!fb) throw new Error(`No fallback for ${sym}`)
  if (!shown.length) {
    return {
      title: 'No single cause stands out',
      explanation: `Among ${leaf.n} similar MedDialog consultations, doctors did not converge on one cause for these answers.`,
      conclusion: {
        urgencyLevel: urgency,
        likelyConditions: ['Needs a doctor’s assessment'],
        clinicalAdvice: fb.advice,
        homeCareInstructions: fb.care,
        whenToSeekImmediateHelp: fb.seek,
      },
    }
  }

  const top = KB[lead[0]]
  const stats = shown.map(([n, p]) => `${n} (${pct(p)})`).join(', ')
  // High-floor symptoms (chest pain, breathlessness) always carry their own warning line,
  // unless the lead condition's advice is already urgent.
  const needsWarning = meta.floor === 'URGENT_CARE' && URGENCY_RANK[top.urgency] < URGENCY_RANK.URGENT_CARE
  const seek = needsWarning ? `${top.seek} ${fb.seek}` : top.seek
  return {
    title: lead[0],
    explanation: `Among ${leaf.n} similar MedDialog consultations with these answers, doctors most often discussed: ${stats}. This counts what doctors mentioned in their replies; it is not a diagnosis.`,
    conclusion: {
      urgencyLevel: urgency,
      likelyConditions: shown.map(([n]) => n),
      clinicalAdvice: `${top.advice}\n\nIn similar MedDialog consultations, doctors most often discussed: ${stats}.`,
      homeCareInstructions: top.care,
      whenToSeekImmediateHelp: seek,
    },
  }
}

for (const [sym, e] of Object.entries(model.symptoms)) {
  const meta = SYMPTOM_META[sym]
  const cat = sym.toUpperCase()
  const rfId = `${sym}_rf`
  let q = 0
  let leaf = 0

  // Build the learned part first so the red-flag screen can point at its root.
  function build(n) {
    if (n.leaf) {
      const id = `${sym}_leaf${leaf++}`
      const c = conclusionForLeaf(sym, n)
      addNode({ id, category: cat, question: `Assessment: ${c.title}`, explanation: c.explanation, isEmergencyCheck: false, options: [], isConclusion: true, conclusion: c.conclusion })
      return id
    }
    const id = `${sym}_q${q++}`
    const yesId = build(n.yes)
    const noId = build(n.no)
    const fs = e.facetStats[n.facet]
    const asked = fs.doctorAskPct >= 1 ? ` Doctors asked about it in ${fs.doctorAskPct}% of these consultations.` : ''
    addNode({
      id,
      category: cat,
      question: FACETS[n.facet].q,
      explanation: `Why we ask: this was among the strongest clues separating the conditions doctors discussed in ${e.dialogs.toLocaleString('en-US')} MedDialog “${meta.name}” consultations. ${fs.patientPct}% of patients mentioned it.${asked}`,
      isEmergencyCheck: false,
      options: [
        { text: 'Yes', subtitle: null, nextId: yesId, isRedFlag: false },
        { text: 'No or not sure', subtitle: null, nextId: noId, isRedFlag: false },
      ],
      isConclusion: false,
    })
    return id
  }
  const rootId = build(e.tree)

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
      explanation: 'This warning sign comes from standard clinical triage guidance. It was not learned from the MedDialog data.',
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
  options.push({ text: 'None of these', subtitle: 'Continue with a few quick questions', nextId: rootId, isRedFlag: false })
  addNode({
    id: rfId,
    category: cat,
    question: 'First, do any of these apply to you right now?',
    explanation: 'These warning signs always need urgent care. They come from standard clinical triage guidance, not from the MedDialog data.',
    isEmergencyCheck: true,
    options,
    isConclusion: false,
  })

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
categories.sort((a, b) => b.dialogs - a.dialogs)
for (const c of categories) delete c.dialogs

const out = {
  version: '2.0',
  metadata: {
    source: 'MedDialog-EN (iCliniq + HealthCareMagic), mined offline',
    description: `Questions and likely-condition statistics learned from ${model.corpus.dialogs.toLocaleString('en-US')} real doctor-patient dialogs. Emergency red flags and advice text are curated clinical guidance. This is an information tool, not a diagnosis.`,
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
  console.error(errors.join('\n'))
  process.exit(1)
}

mkdirSync(assetsDir, { recursive: true })
writeFileSync(join(assetsDir, 'triage_tree.json'), JSON.stringify(out, null, 2) + '\n')
const leaves = Object.values(nodes).filter((n) => n.isConclusion && !n.isEmergencyCheck).length
console.error(`wrote triage_tree.json: ${categories.length} categories, ${Object.keys(nodes).length} nodes (${leaves} data-derived leaves)`)
