# MedDialog → triage tree: build report

Generated 2026-10-13 from **212,098** English doctor-patient dialogs (healthcaremagic + icliniq; `lighteval/med_dialog`).

## What is learned from the data, and what is not

| From MedDialog (offline mining) | Hand-written (not learned) |
|---|---|
| Which chief complaints become app categories (frequency ≥ 2,000 dialogs) | Emergency red-flag screens (`lib/safety.mjs`) |
| Which yes/no questions are asked, and in what order (decision tree) | Advice, home care, and “when to seek help” text (`lib/kb.mjs`) |
| Which conditions doctors discussed for each answer pattern, with percentages | Urgency floors and per-condition urgency |

Categories were selected by corpus frequency. Topics that are frequent but not triage-able by symptom questions were excluded:

- **sexual** (10,669 dialogs): Needs a private clinical consultation; symptom questions cannot triage it safely.
- **dental** (8,437 dialogs): Needs a dentist examination.
- **bp** (4,996 dialogs): A measurement rather than a symptom.

## Method

1. **Chief complaint**: lexicon match on the dialog title, falling back to the patient’s opening; earliest mention wins.
2. **Features**: ~100 yes/no facets, each a pattern over the *patient’s* text (e.g. “mentions fever”).
3. **Labels**: conditions named in the *doctor’s* reply, from a vocabulary of ~65 conditions. This is “doctor discussed X”, not a confirmed diagnosis.
4. **Tree**: per complaint, a multi-label decision tree (depth ≤ 3) splitting on the facet with the largest total entropy reduction across the complaint’s top conditions.
5. **Pruning and evaluation**: 70% grow / 15% prune (reduced-error, held-out log-likelihood) / 15% untouched test, split deterministically by dialog id.

## Test-set results (15% of dialogs never used for growing or pruning)

`logLik gain` = mean per-dialog log-likelihood of the doctor-mentioned conditions under the tree minus under a no-question baseline (higher is better; > 0 means the questions help). `hit@3` = share of test dialogs where at least one condition the doctor discussed is in the leaf’s top 3; the baseline uses the complaint’s overall top 3.

| Complaint | Dialogs | Splits kept / grown | hit@3 tree | hit@3 baseline | logLik gain |
|---|---:|---:|---:|---:|---:|
| Period or pregnancy concern | 18,104 | 6 / 6 | 0.94 | 0.973 | 0.5666 |
| Rash or itching | 13,999 | 5 / 5 | 0.699 | 0.611 | 0.074 |
| Back or neck pain | 13,955 | 3 / 5 | 0.667 | 0.667 | 0.043 |
| Urinary problem | 10,138 | 3 / 3 | 0.884 | 0.77 | 0.1111 |
| Stomach pain | 7,712 | 4 / 5 | 0.688 | 0.67 | 0.0807 |
| Fever or chills | 6,021 | 3 / 4 | 0.735 | 0.668 | 0.0288 |
| Vomiting or diarrhea | 5,934 | 3 / 5 | 0.845 | 0.752 | 0.307 |
| Cough | 5,556 | 5 / 7 | 0.688 | 0.648 | 0.0619 |
| Anxiety or stress | 5,318 | 3 / 3 | 0.954 | 0.946 | 0.1029 |
| Numbness or tingling | 4,542 | 3 / 4 | 0.76 | 0.751 | 0.119 |
| Joint or limb pain | 4,452 | 4 / 6 | 0.793 | 0.802 | 0.0517 |
| Chest pain | 3,787 | 3 / 4 | 0.815 | 0.772 | 0.0806 |
| Tiredness or weakness | 3,508 | 3 / 3 | 0.672 | 0.596 | 0.1019 |
| Dizziness or vertigo | 3,016 | 3 / 4 | 0.756 | 0.587 | 0.134 |
| Shortness of breath | 2,948 | 3 / 4 | 0.815 | 0.745 | 0.1652 |
| Sore throat, cold or sinus | 2,872 | 6 / 7 | 0.769 | 0.747 | 0.1028 |
| Headache | 2,830 | 4 / 4 | 0.724 | 0.653 | 0.1171 |
| Low mood or depression | 2,541 | 1 / 1 | 1 | 1 | 0.0722 |
| Palpitations or racing heart | 2,142 | 4 / 5 | 0.89 | 0.881 | 0.0198 |
| Acne or pimples | 2,093 | 3 / 4 | 0.943 | 0.933 | 0.0501 |

## Questions chosen per complaint

**Period or pregnancy concern** — “Do you have acne, excess facial hair or weight gain?” (19% mention); “Are you under a lot of stress or worry?” (14% mention); “Has this been going on for more than 2 weeks?” (28.1% mention); “Was there unprotected sex recently?” (20% mention); “Is your period late or missed?” (10.5% mention)

**Rash or itching** — “Do you have allergies (dust, pollen, food, or medicines)?” (13.1% mention); “Is it itchy?” (56.6% mention); “Are there blisters or pus-filled spots?” (17% mention); “Have you had a fever or chills?” (5.2% mention)

**Back or neck pain** — “Was there a recent injury, fall, or heavy lifting?” (12.6% mention); “Does pain travel down one leg?” (4.7% mention); “Does it follow long sitting, bending, or lifting?” (10.5% mention)

**Urinary problem** — “Have you had kidney stones before?” (13.9% mention); “Do you have diabetes or high blood sugar?” (5.4% mention); “Does it burn when you pass urine?” (4.7% mention)

**Stomach pain** — “Have you had a fever or chills?” (11.1% mention); “Have you already tried medicine without much relief?” (11.6% mention); “Do you get heartburn, a sour taste, or burping?” (4.8% mention); “Do you have urinary symptoms (burning or frequent urination)?” (13.4% mention)

**Fever or chills** — “Mosquito bites, travel, or an outbreak in your area?” (6.7% mention); “Do you have a runny nose, sneezing or blocked nose?” (18.2% mention); “Has this been going on for more than 2 weeks?” (23.3% mention)

**Vomiting or diarrhea** — “Do you have loose stools?” (27.4% mention); “Has this been going on for more than 2 weeks?” (21.9% mention); “Have you had a fever or chills?” (11.9% mention)

**Cough** — “Do you have allergies (dust, pollen, food, or medicines)?” (18.1% mention); “Has this been going on for more than 2 weeks?” (36.7% mention); “Do you have a runny nose, sneezing or blocked nose?” (36.4% mention); “Have you had a fever or chills?” (17.2% mention); “Do you feel short of breath or wheezy?” (9.2% mention)

**Anxiety or stress** — “Have you felt sad, low, or lost interest in things?” (14.1% mention); “Do you get sudden episodes of intense fear with physical symptoms?” (19.8% mention)

**Numbness or tingling** — “Is it in your hands or feet?” (45.3% mention); “Was there a recent injury, fall, or heavy lifting?” (14.7% mention)

**Joint or limb pain** — “Was there a recent injury, fall, or heavy lifting?” (17% mention); “Is more than one joint affected?” (14.4% mention); “Has this been going on for more than 2 weeks?” (33.7% mention); “Is there any swelling?” (17.7% mention)

**Chest pain** — “Do you get anxious or have panic attacks?” (8.5% mention); “Do you get heartburn, a sour taste, or burping?” (7.8% mention); “Do you also have a cough?” (7.9% mention)

**Tiredness or weakness** — “Do you look pale or get tired easily?” (7.7% mention); “Have you felt sad, low, or lost interest in things?” (5.5% mention); “Do you have diabetes or high blood sugar?” (5.5% mention)

**Dizziness or vertigo** — “Does the room spin around you?” (17.4% mention); “Was there a recent injury, fall, or heavy lifting?” (12.4% mention); “Do you have high blood pressure?” (14.2% mention)

**Shortness of breath** — “Do you have allergies (dust, pollen, food, or medicines)?” (11.8% mention); “Do you get anxious or have panic attacks?” (7% mention); “Do you also have a cough?” (16.2% mention)

**Sore throat, cold or sinus** — “Do you have allergies (dust, pollen, food, or medicines)?” (21.6% mention); “Do you have a runny nose, sneezing or blocked nose?” (35.3% mention); “Has this been going on for more than 2 weeks?” (33.3% mention); “Have you had a fever or chills?” (15.7% mention); “Do you also have a cough?” (17.9% mention)

**Headache** — “Was there a recent injury, fall, or heavy lifting?” (12.2% mention); “Has this been going on for more than 2 weeks?” (33.4% mention); “Do you have high blood pressure?” (6.3% mention); “Have you had a fever or chills?” (9.6% mention)

**Low mood or depression** — “Do you get anxious or have panic attacks?” (21.4% mention)

**Palpitations or racing heart** — “Do you get anxious or have panic attacks?” (14.3% mention); “Do you have high blood pressure?” (17% mention); “Has this been going on for more than 2 weeks?” (30.3% mention); “Are you under a lot of stress or worry?” (32.4% mention)

**Acne or pimples** — “Are you under a lot of stress or worry?” (8.2% mention); “Are the pimples deep, painful, or leaving scars?” (27% mention); “Have you already tried medicine without much relief?” (16.9% mention)

## Audit of the hand-written red flags against real doctor language

For each red flag, the share of dialogs whose patient text matches the flag and whose doctor reply uses urgent language (emergency, hospital, immediately, ambulance, …), compared with that complaint overall. Rows with few matches are weak evidence either way; this is an audit, not a validation.

| Complaint | Red flag | Patient mentions | Urgent language | Complaint baseline |
|---|---|---:|---:|---:|
| Period or pregnancy concern | ectopic | 213 | 7.5% | 5.3% |
| Period or pregnancy concern | bleed | 336 | 8% | 5.3% |
| Rash or itching | airway | 31 | 12.9% | 5% |
| Rash or itching | skin_reaction | 240 | 5% | 5% |
| Rash or itching | purpuric | 26 | 0% | 5% |
| Back or neck pain | cauda | 29 | 20.7% | 6% |
| Back or neck pain | trauma | 498 | 5.6% | 6% |
| Urinary problem | retention | 70 | 2.9% | 5.7% |
| Urinary problem | kidney | 26 | 7.7% | 5.7% |
| Stomach pain | rigid | 345 | 13.6% | 8.6% |
| Stomach pain | blood | 51 | 5.9% | 8.6% |
| Stomach pain | ectopic | 65 | 12.3% | 8.6% |
| Fever or chills | neck | 102 | 12.7% | 9.4% |
| Fever or chills | infant | 667 | 9.6% | 9.4% |
| Fever or chills | breath | 263 | 11.8% | 9.4% |
| Vomiting or diarrhea | blood | 35 | 22.9% | 8.2% |
| Vomiting or diarrhea | dehydrated | 100 | 9% | 8.2% |
| Vomiting or diarrhea | neck | 53 | 9.4% | 8.2% |
| Cough | breath | 29 | 13.8% | 5.9% |
| Cough | blood | 64 | 7.8% | 5.9% |
| Anxiety or stress | suicide | 77 | 7.8% | 4.8% |
| Anxiety or stress | cardiac | 72 | 2.8% | 4.8% |
| Numbness or tingling | stroke | 124 | 10.5% | 6.6% |
| Numbness or tingling | cauda | 5 | 0% | 6.6% |
| Joint or limb pain | septic | 15 | 13.3% | 3.9% |
| Joint or limb pain | deformity | 156 | 2.6% | 3.9% |
| Chest pain | heart | 994 | 9.8% | 7.7% |
| Chest pain | breath | 71 | 8.5% | 7.7% |
| Chest pain | faint | 67 | 10.4% | 7.7% |
| Tiredness or weakness | cardiac | 86 | 8.1% | 5.7% |
| Tiredness or weakness | gi | 3 | 33.3% | 5.7% |
| Dizziness or vertigo | stroke | 95 | 9.5% | 9.8% |
| Dizziness or vertigo | cardiac | 314 | 8% | 9.8% |
| Shortness of breath | rest | 86 | 8.1% | 7.5% |
| Shortness of breath | chest | 133 | 12.8% | 7.5% |
| Shortness of breath | allergy | 1 | 0% | 7.5% |
| Sore throat, cold or sinus | airway | 12 | 8.3% | 4.7% |
| Sore throat, cold or sinus | neck | 32 | 9.4% | 4.7% |
| Headache | thunderclap | 10 | 10% | 10.2% |
| Headache | stroke | 195 | 16.4% | 10.2% |
| Headache | neck | 52 | 32.7% | 10.2% |
| Headache | injury | 160 | 27.5% | 10.2% |
| Low mood or depression | suicide | 358 | 12.3% | 5.9% |
| Palpitations or racing heart | cardiac | 170 | 7.6% | 7.7% |
| Palpitations or racing heart | sustained | 99 | 7.1% | 7.7% |
| Acne or pimples | cellulitis | 1 | 0% | 10.9% |

## Limitations

- MedDialog is online-forum text. Doctors reply to the question asked, often without asking follow-ups, so “not mentioned” in the patient text is treated as “No or not sure”.
- Condition labels are regex matches over doctor text. A doctor writing “get a pregnancy test” counts as “discussed Pregnancy”. Percentages are therefore mention rates, not probabilities of having the condition.
- Lexicons are English only and hand-built; recall is imperfect. About 28% of dialogs match no complaint and are unused.
- The dataset licence is listed as unknown on the Hugging Face card; only aggregate statistics and original wording are shipped in the app, no dialog text.
- Not a medical device. All advice text is general information and should be reviewed by a clinician before any public release.
