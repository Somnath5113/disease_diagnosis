// Usage: node explore_conditions.mjs <raw-dir> [topN]
// For each frequent symptom, show which vocabulary conditions doctors name most often.
import { loadDialogs } from './lib/load.mjs'
import { SYMPTOMS, primarySymptom } from './lib/symptoms.mjs'
import { conditionMentions } from './lib/conditions.mjs'

const topN = Number(process.argv[3] || 8)
const dialogs = await loadDialogs(process.argv[2])
const bySym = {}
for (const d of dialogs) {
  const s = primarySymptom(d)
  if (!s) continue
  const hits = conditionMentions(d.doctor)
  const e = (bySym[s] ||= { n: 0, withLabel: 0, c: {} })
  e.n++
  if (hits.length) e.withLabel++
  for (const h of hits) e.c[h] = (e.c[h] || 0) + 1
}
for (const [s, e] of Object.entries(bySym).sort((a, b) => b[1].n - a[1].n).slice(0, 24)) {
  console.log(`\n## ${s} (${SYMPTOMS[s].system}) n=${e.n} labelled=${(100 * e.withLabel / e.n).toFixed(0)}%`)
  for (const [c, k] of Object.entries(e.c).sort((a, b) => b[1] - a[1]).slice(0, topN)) {
    console.log(`  ${String(k).padStart(6)} ${(100 * k / e.n).toFixed(1).padStart(5)}%  ${c}`)
  }
}
