// Usage: node verify_tree.mjs [tree.json] — structural checks mirroring ExampleRobolectricTest.
import { readFileSync } from 'node:fs'
const path = process.argv[2] || new URL('../../app/src/main/assets/triage_tree.json', import.meta.url)
const t = JSON.parse(readFileSync(path))
const URG = new Set(['EMERGENCY', 'URGENT_CARE', 'ROUTINE_DOCTOR', 'SELF_CARE'])
const opts = (n) => n.options || []
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
  if (!opts(s).some((o) => o.isRedFlag)) fail.push(`${c.id}: no red flag`)
  if (!opts(s).some((o) => !o.isRedFlag)) fail.push(`${c.id}: cannot continue`)
}
for (const [id, n] of Object.entries(t.nodes)) {
  if (n.id !== id) fail.push(`${id}: id mismatch`)
  if (!n.category || !n.question) fail.push(`${id}: missing category/question`)
  if (!n.isConclusion && !opts(n).length) fail.push(`${id}: dead end`)
  if (n.isConclusion && opts(n).length) fail.push(`${id}: conclusion has options`)
  for (const o of opts(n)) {
    const tgt = t.nodes[o.nextId]
    if (!tgt) fail.push(`${id}: bad nextId ${o.nextId}`)
    else if (o.isRedFlag && !tgt.isConclusion) fail.push(`${id}: red flag target not conclusion`)
  }
  if (n.conclusion) {
    const c = n.conclusion
    if (!URG.has(c.urgencyLevel)) fail.push(`${id}: bad urgency`)
    for (const x of c.likelyConditions) if (x.includes(', ')) fail.push(`${id}: comma in "${x}"`)
    if (!c.clinicalAdvice || !c.whenToSeekImmediateHelp || !c.homeCareInstructions?.length) fail.push(`${id}: empty advice`)
    if (n.isEmergencyCheck && !['EMERGENCY', 'URGENT_CARE'].includes(c.urgencyLevel)) fail.push(`${id}: emergency node not urgent`)
  }
}
// Longest path in screens (emergency screen + questions); memoised DFS also proves acyclicity.
const memo = new Map()
function screens(id, visiting) {
  if (memo.has(id)) return memo.get(id)
  if (visiting.has(id)) { fail.push(`cycle through ${id}`); return 0 }
  visiting.add(id)
  const n = t.nodes[id]
  const d = n.isConclusion ? 0 : 1 + Math.max(0, ...opts(n).map((o) => screens(o.nextId, visiting)))
  visiting.delete(id)
  memo.set(id, d)
  return d
}
const longest = Math.max(...t.categories.map((c) => screens(c.startNodeId, new Set())))
if (longest < 8) fail.push(`longest path only ${longest} screens (expected >= 8)`)
if (longest > 10) fail.push(`longest path is ${longest} screens (limit 10)`)
const reach = new Set()
const stack = t.categories.map((c) => c.startNodeId)
while (stack.length) { const id = stack.pop(); if (reach.has(id)) continue; reach.add(id); for (const o of opts(t.nodes[id])) stack.push(o.nextId) }
for (const id of Object.keys(t.nodes)) if (!reach.has(id)) fail.push(`unreachable ${id}`)
const mix = {}
for (const n of Object.values(t.nodes)) if (n.conclusion) mix[n.conclusion.urgencyLevel] = (mix[n.conclusion.urgencyLevel] || 0) + 1
console.log(`categories=${t.categories.length} nodes=${Object.keys(t.nodes).length} longestPath=${longest} screens`, mix)
if (fail.length) { console.error('FAIL\n' + fail.slice(0, 30).join('\n')); process.exit(1) }
console.log('OK: all structural checks passed')
