// Usage: node verify_tree.mjs — structural checks mirroring ExampleRobolectricTest, plus path enumeration.
import { readFileSync } from 'node:fs'
const t = JSON.parse(readFileSync(new URL('../../app/src/main/assets/triage_tree.json', import.meta.url)))
const URG = new Set(['EMERGENCY', 'URGENT_CARE', 'ROUTINE_DOCTOR', 'SELF_CARE'])
const fail = []
const ids = t.categories.map((c) => c.id)
if (new Set(ids).size !== ids.length) fail.push('duplicate category ids')
if (t.categories.length < 15) fail.push('fewer than 15 categories')
for (const id of ['CHESTPAIN', 'BREATHLESS', 'HEADACHE', 'ABDPAIN', 'COUGH', 'FEVER']) if (!ids.includes(id)) fail.push(`missing ${id}`)
for (const c of t.categories) {
  if (!c.group || !c.groupName || !c.icon) fail.push(`${c.id}: group/icon missing`)
  const s = t.nodes[c.startNodeId]
  if (!s) { fail.push(`${c.id}: no start`); continue }
  if (!s.isEmergencyCheck) fail.push(`${c.id}: start not emergency screen`)
  if (!s.options.some((o) => o.isRedFlag)) fail.push(`${c.id}: no red flag`)
  if (!s.options.some((o) => !o.isRedFlag)) fail.push(`${c.id}: cannot continue`)
}
for (const [id, n] of Object.entries(t.nodes)) {
  for (const o of n.options) {
    const tgt = t.nodes[o.nextId]
    if (!tgt) fail.push(`${id}: bad nextId ${o.nextId}`)
    else if (o.isRedFlag && !tgt.isConclusion) fail.push(`${id}: red flag target not conclusion`)
  }
  if (n.conclusion) {
    if (!URG.has(n.conclusion.urgencyLevel)) fail.push(`${id}: bad urgency`)
    for (const c of n.conclusion.likelyConditions) if (c.includes(', ')) fail.push(`${id}: comma in "${c}"`)
    if (!n.conclusion.clinicalAdvice || !n.conclusion.whenToSeekImmediateHelp) fail.push(`${id}: empty advice`)
    if (n.isEmergencyCheck && !['EMERGENCY', 'URGENT_CARE'].includes(n.conclusion.urgencyLevel)) fail.push(`${id}: emergency node not urgent`)
  }
}
// Enumerate every answer path per category: acyclic, ends in a conclusion, record depth + outcome mix.
let paths = 0, maxDepth = 0
const mix = {}
function walk(id, depth, seen) {
  if (seen.has(id)) { fail.push(`cycle at ${id}`); return }
  const n = t.nodes[id]
  if (n.isConclusion) { paths++; maxDepth = Math.max(maxDepth, depth); mix[n.conclusion.urgencyLevel] = (mix[n.conclusion.urgencyLevel] || 0) + 1; return }
  for (const o of n.options) walk(o.nextId, depth + 1, new Set(seen).add(id))
}
for (const c of t.categories) walk(c.startNodeId, 0, new Set())
console.log(`categories=${t.categories.length} nodes=${Object.keys(t.nodes).length} paths=${paths} maxDepth=${maxDepth}`, mix)
if (fail.length) { console.error('FAIL\n' + fail.join('\n')); process.exit(1) }
console.log('OK: all structural checks passed')
