// Usage: node verify_deep.mjs [tree.json]
// 1) path-depth statistics   2) "textbook patient" simulation per condition.
// The simulation answers each question the way a patient with that condition would
// (Yes where the question supports it, No where it argues against or is neutral) and
// checks the assessment names that condition. It tests wiring and reachability of the
// authored question bank, not clinical accuracy.
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import { DEEP } from './lib/deep.mjs'
import { KB } from './lib/kb.mjs'
import { KB_DEEP } from './lib/kb_deep.mjs'

const here = import.meta.dirname
const treePath = process.argv[2] || join(here, '..', '..', 'app', 'src', 'main', 'assets', 'triage_tree.json')
const t = JSON.parse(readFileSync(treePath, 'utf8'))
const qmap = JSON.parse(readFileSync(join(here, 'out', 'qmap.json'), 'utf8'))
const ADV = { ...KB, ...KB_DEEP }
const URG = { SELF_CARE: 0, ROUTINE_DOCTOR: 1, URGENT_CARE: 2, EMERGENCY: 3 }

// ---------- 1. depth statistics ----------
const hist = {}
const perCat = {}
let maxQ = 0
function walk(id, depth, cat, seen) {
  const n = t.nodes[id]
  if (seen.has(id)) throw new Error(`cycle at ${id}`)
  if (n.isConclusion) {
    const d = n.isEmergencyCheck ? null : depth
    if (d !== null) {
      hist[d] = (hist[d] || 0) + 1
      const c = (perCat[cat] ||= { paths: 0, max: 0, sum: 0 })
      c.paths++; c.max = Math.max(c.max, d); c.sum += d
      maxQ = Math.max(maxQ, d)
    }
    return
  }
  const isScreen = n.isEmergencyCheck
  for (const o of n.options) walk(o.nextId, depth + (isScreen ? 0 : 1), cat, new Set(seen).add(id))
}
for (const c of t.categories) walk(c.startNodeId, 0, c.id, new Set())
console.log('Questions asked before an assessment (excluding the emergency screen) – number of paths:')
console.log(Object.entries(hist).sort((a, b) => a[0] - b[0]).map(([d, n]) => `${d}:${n}`).join('  '))
console.log(`max question layers = ${maxQ}`)
console.table(Object.fromEntries(Object.entries(perCat).map(([c, v]) => [c, { paths: v.paths, avgQuestions: +(v.sum / v.paths).toFixed(1), maxQuestions: v.max }])))

// ---------- 2. textbook-patient simulation ----------
// A textbook patient says Yes to features that clearly support the condition (>= +2), or whose
// absence would argue strongly against it; weak (+1) clues are not defining features.
const patientSaysYes = (q, key) => Object.keys(q.yes).length > 0 && ((q.yes[key] || 0) >= 2 || ((q.yes[key] || 0) >= 0 && (q.no[key] || 0) <= -2))
const stage1Rule = (sym, facet, key) => {
  // Use the follow-up bank's own rule for the duplicated facet, if any.
  const q = DEEP[sym].Q.find((x) => x.f === facet)
  return q ? patientSaysYes(q, key) : false
}
function simulate(sym, key) {
  const cat = t.categories.find((c) => c.id === sym.toUpperCase())
  let node = t.nodes[cat.startNodeId]
  node = t.nodes[node.options.find((o) => !o.isRedFlag).nextId] // pass the emergency screen
  let steps = 0
  while (!node.isConclusion) {
    steps++
    let yes = false
    const m = qmap[node.id]
    if (m) {
      const q = DEEP[sym].Q.find((x) => x.id === m.qid)
      yes = patientSaysYes(q, key)
    } else {
      // stage-1 node: find its facet by matching the question text
      yes = stage1Yes(sym, node.question, key)
    }
    node = t.nodes[node.options[yes ? 0 : 1].nextId]
  }
  return { node, steps }
}
import { FACETS } from './lib/facets.mjs'
const facetByQ = Object.fromEntries(Object.entries(FACETS).map(([k, v]) => [v.q, k]))
function stage1Yes(sym, qtext, key) {
  const facet = facetByQ[qtext]
  return facet ? stage1Rule(sym, facet, key) : false
}

const rows = {}
let total = 0, lead = 0, listed = 0
const misses = []
const over = []
const under = []
for (const [sym, d] of Object.entries(DEEP)) {
  for (const [key, name] of Object.entries(d.C)) {
    const { node, steps } = simulate(sym, key)
    const c = node.conclusion
    const isLead = node.question === `Assessment: ${name}`
    const inList = c.likelyConditions.includes(name)
    total++; if (isLead) lead++; if (inList) listed++
    const own = ADV[name].urgency
    const note = `${sym}/${name}: own=${own}, got ${c.urgencyLevel} [${c.likelyConditions.join(' | ')}]`
    if (URG[c.urgencyLevel] > URG[own]) over.push(note)
    else if (URG[c.urgencyLevel] < URG[own]) under.push(note)
    ;(rows[sym] ||= []).push({ key, isLead, inList })
    if (!inList) misses.push(`${sym}/${name}: got "${node.question.replace('Assessment: ', '')}" [${c.likelyConditions.join(' | ')}] after ${steps} questions`)
  }
}
console.log(`\nTextbook patients: ${total} conditions. Named as lead: ${lead} (${Math.round((100 * lead) / total)}%). Present in assessment list: ${listed} (${Math.round((100 * listed) / total)}%).`)
console.table(Object.fromEntries(Object.entries(rows).map(([s, r]) => [s, { conditions: r.length, lead: r.filter((x) => x.isLead).length, inList: r.filter((x) => x.inList).length }])))
if (misses.length) console.log('\nNot reached:\n  ' + misses.join('\n  '))
// Urgency check: a textbook patient should get roughly the condition's own urgency. Above = escalated by the
// complaint's floor or by a serious condition that stayed plausible; below would be a safety concern.
console.log(`\nUrgency above the condition's own level: ${over.length}/${total}`)
if (over.length) console.log('  ' + over.join('\n  '))
console.log(`\nUrgency BELOW the condition's own level: ${under.length}/${total}`)
if (under.length) console.log('  ' + under.join('\n  '))
