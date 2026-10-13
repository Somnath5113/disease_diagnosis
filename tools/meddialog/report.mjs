// Usage: node report.mjs  — writes REPORT.md from out/model.json
import { readFileSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import { FACETS } from './lib/facets.mjs'
import { SYMPTOM_META } from './lib/safety.mjs'

const here = import.meta.dirname
const m = JSON.parse(readFileSync(join(here, 'out', 'model.json'), 'utf8'))
const L = []
const f = (x) => (x == null ? 'n/a' : x)

L.push('# MedDialog → triage tree: build report', '')
L.push(`Generated ${m.generatedAt.slice(0, 10)} from **${m.corpus.dialogs.toLocaleString('en-US')}** English doctor-patient dialogs (${m.corpus.subsets.join(' + ')}; \`lighteval/med_dialog\`).`, '')
L.push('## What is learned from the data, and what is not', '')
L.push('| From MedDialog (offline mining) | Hand-written (not learned) |', '|---|---|')
L.push('| Which chief complaints become app categories (frequency ≥ 2,000 dialogs) | Emergency red-flag screens (`lib/safety.mjs`) |')
L.push('| Which yes/no questions are asked, and in what order (decision tree) | Advice, home care, and “when to seek help” text (`lib/kb.mjs`) |')
L.push('| Which conditions doctors discussed for each answer pattern, with percentages | Urgency floors and per-condition urgency |', '')
L.push('Categories were selected by corpus frequency. Topics that are frequent but not triage-able by symptom questions were excluded:', '')
for (const e of m.excluded) L.push(`- **${e.symptom}** (${e.dialogs.toLocaleString('en-US')} dialogs): ${e.reason}`)
L.push('')
L.push('## Method', '')
L.push('1. **Chief complaint**: lexicon match on the dialog title, falling back to the patient’s opening; earliest mention wins.')
L.push('2. **Features**: ~100 yes/no facets, each a pattern over the *patient’s* text (e.g. “mentions fever”).')
L.push('3. **Labels**: conditions named in the *doctor’s* reply, from a vocabulary of ~65 conditions. This is “doctor discussed X”, not a confirmed diagnosis.')
L.push('4. **Tree**: per complaint, a multi-label decision tree (depth ≤ 3) splitting on the facet with the largest total entropy reduction across the complaint’s top conditions.')
L.push('5. **Pruning and evaluation**: 70% grow / 15% prune (reduced-error, held-out log-likelihood) / 15% untouched test, split deterministically by dialog id.', '')
L.push('## Test-set results (15% of dialogs never used for growing or pruning)', '')
L.push('`logLik gain` = mean per-dialog log-likelihood of the doctor-mentioned conditions under the tree minus under a no-question baseline (higher is better; > 0 means the questions help). `hit@3` = share of test dialogs where at least one condition the doctor discussed is in the leaf’s top 3; the baseline uses the complaint’s overall top 3.', '')
L.push('| Complaint | Dialogs | Splits kept / grown | hit@3 tree | hit@3 baseline | logLik gain |', '|---|---:|---:|---:|---:|---:|')
for (const [s, e] of Object.entries(m.symptoms).sort((a, b) => b[1].dialogs - a[1].dialogs)) {
  L.push(`| ${SYMPTOM_META[s].name} | ${e.dialogs.toLocaleString('en-US')} | ${e.splitsAfterPruning} / ${e.splitsBeforePruning} | ${f(e.eval.hit3Tree)} | ${f(e.eval.hit3Baseline)} | ${f(e.eval.logLikGain)} |`)
}
L.push('', '## Questions chosen per complaint', '')
for (const [s, e] of Object.entries(m.symptoms).sort((a, b) => b[1].dialogs - a[1].dialogs)) {
  L.push(`**${SYMPTOM_META[s].name}** — ` + Object.keys(e.facetStats).map((k) => `“${FACETS[k].q}” (${e.facetStats[k].patientPct}% mention)`).join('; '), '')
}
L.push('## Audit of the hand-written red flags against real doctor language', '')
L.push('For each red flag, the share of dialogs whose patient text matches the flag and whose doctor reply uses urgent language (emergency, hospital, immediately, ambulance, …), compared with that complaint overall. Rows with few matches are weak evidence either way; this is an audit, not a validation.', '')
L.push('| Complaint | Red flag | Patient mentions | Urgent language | Complaint baseline |', '|---|---|---:|---:|---:|')
for (const [s, e] of Object.entries(m.symptoms)) {
  for (const a of e.flagAudit) L.push(`| ${SYMPTOM_META[s].name} | ${a.id} | ${a.mentions} | ${a.urgentPct == null ? 'n/a' : a.urgentPct + '%'} | ${a.baseUrgentPct}% |`)
}
L.push('', '## Limitations', '')
L.push('- MedDialog is online-forum text. Doctors reply to the question asked, often without asking follow-ups, so “not mentioned” in the patient text is treated as “No or not sure”.')
L.push('- Condition labels are regex matches over doctor text. A doctor writing “get a pregnancy test” counts as “discussed Pregnancy”. Percentages are therefore mention rates, not probabilities of having the condition.')
L.push('- Lexicons are English only and hand-built; recall is imperfect. About 28% of dialogs match no complaint and are unused.')
L.push('- The dataset licence is listed as unknown on the Hugging Face card; only aggregate statistics and original wording are shipped in the app, no dialog text.')
L.push('- Not a medical device. All advice text is general information and should be reviewed by a clinician before any public release.', '')
writeFileSync(join(here, 'REPORT.md'), L.join('\n'))
console.error('wrote REPORT.md')
