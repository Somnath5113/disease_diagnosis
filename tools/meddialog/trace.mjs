// Usage: node trace.mjs <symptom> <conditionKey> [tree.json]  — print the questions a textbook patient is asked.
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import { DEEP } from './lib/deep.mjs'
import { FACETS } from './lib/facets.mjs'
const here = import.meta.dirname
const [sym, key, tp] = process.argv.slice(2)
const t = JSON.parse(readFileSync(tp || join(here, '..', '..', 'app', 'src', 'main', 'assets', 'triage_tree.json'), 'utf8'))
const qmap = JSON.parse(readFileSync(join(here, 'out', 'qmap.json'), 'utf8'))
const facetByQ = Object.fromEntries(Object.entries(FACETS).map(([k, v]) => [v.q, k]))
const says = (q) => Object.keys(q.yes).length > 0 && ((q.yes[key] || 0) >= 2 || ((q.yes[key] || 0) >= 0 && (q.no[key] || 0) <= -2))
let node = t.nodes[t.nodes[t.categories.find((c) => c.id === sym.toUpperCase()).startNodeId].options.find((o) => !o.isRedFlag).nextId]
let i = 0
while (!node.isConclusion) {
  const m = qmap[node.id]
  let q = m ? DEEP[sym].Q.find((x) => x.id === m.qid) : DEEP[sym].Q.find((x) => x.f && x.f === facetByQ[node.question])
  const yes = q ? says(q) : false
  console.log(`${++i}. [${m ? 'deep ' : 'stage1'}] ${node.question.slice(0, 90)} -> ${yes ? 'YES' : 'no'}`)
  node = t.nodes[node.options[yes ? 0 : 1].nextId]
}
console.log('=>', node.question, '|', node.conclusion.likelyConditions.join(' | '), '|', node.conclusion.urgencyLevel)
