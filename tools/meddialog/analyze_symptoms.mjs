// Usage: node analyze_symptoms.mjs <raw-dir>
// Prints how often each candidate symptom is the chief complaint in MedDialog-EN.
import { loadDialogs } from './lib/load.mjs'
import { SYMPTOMS, primarySymptom } from './lib/symptoms.mjs'

const dialogs = await loadDialogs(process.argv[2])
const counts = {}
let none = 0
for (const d of dialogs) {
  const k = primarySymptom(d)
  if (!k) { none++; continue }
  counts[k] = (counts[k] || 0) + 1
}
const rows = Object.entries(counts).sort((a, b) => b[1] - a[1])
console.log(`dialogs: ${dialogs.length}  unmatched: ${none}`)
for (const [k, n] of rows) {
  console.log(`${String(n).padStart(7)}  ${(100 * n / dialogs.length).toFixed(1).padStart(5)}%  ${SYMPTOMS[k].system.padEnd(16)} ${k}`)
}
const bySystem = {}
for (const [k, n] of rows) bySystem[SYMPTOMS[k].system] = (bySystem[SYMPTOMS[k].system] || 0) + n
console.log('--- by system')
for (const [s, n] of Object.entries(bySystem).sort((a, b) => b[1] - a[1])) console.log(String(n).padStart(7), s)
