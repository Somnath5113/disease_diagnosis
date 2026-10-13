// Usage: node print_trees.mjs [symptom...]  — human-readable view of out/model.json
import { readFileSync } from 'node:fs'
import { FACETS } from './lib/facets.mjs'
const m = JSON.parse(readFileSync(new URL('./out/model.json', import.meta.url)))
const only = process.argv.slice(2)
const fmt = (p) => Object.entries(p).sort((a, b) => b[1] - a[1]).slice(0, 3).filter(([, v]) => v >= 0.08).map(([k, v]) => `${k} ${(v * 100).toFixed(0)}%`).join(' | ')
function walk(n, ind, label) {
  const pad = '  '.repeat(ind)
  if (n.leaf) { console.log(`${pad}${label} -> LEAF n=${n.n}: ${fmt(n.probs)}`); return }
  console.log(`${pad}${label} Q: ${FACETS[n.facet].q}  (n=${n.n}, yes=${(n.yesShare * 100).toFixed(0)}%)`)
  walk(n.yes, ind + 1, 'YES')
  walk(n.no, ind + 1, 'NO ')
}
for (const [s, e] of Object.entries(m.symptoms)) {
  if (only.length && !only.includes(s)) continue
  console.log(`\n=== ${s} n=${e.dialogs}`)
  walk(e.tree, 0, 'ROOT')
}
