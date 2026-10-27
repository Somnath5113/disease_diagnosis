# MedDialog → MedTriage decision tree

Offline pipeline that turns the English MedDialog corpus into `app/src/main/assets/triage_tree.json`. The app itself stays 100% offline: it only ships the generated JSON (aggregate statistics and original wording, no dialog text).

See [REPORT.md](REPORT.md) for the latest run: selected complaints, held-out results, and limitations.

## Data

Download the parquet files of [`lighteval/med_dialog`](https://huggingface.co/datasets/lighteval/med_dialog) (iCliniq + HealthCareMagic, ~212k dialogs) into one folder, e.g.

```
healthcaremagic/train-00000-of-00001.parquet  →  <raw>/healthcaremagic_train-00000-of-00001.parquet
icliniq/{train,validation,test}-*.parquet     →  <raw>/icliniq_{train,validation,test}-*.parquet
```

The file name must be `<subset>_<split>-….parquet`. Keep the data outside the repo.

## Run

```
npm install
node --max-old-space-size=6000 train.mjs <raw>    # mine → out/model.json (~1 min)
npm run emit                                      # out/model.json + safety + advice → triage_tree.json, then validate
npm run report                                    # → REPORT.md
npm run trees [symptom…]                          # print the learned trees
```

## Layers

Each complaint runs: emergency screen (1) → up to 3 MedDialog-derived questions → up to 6 clinical follow-up questions → assessment, so at most **10 screens**. The follow-up questions (`lib/deep.mjs`, sources in [SOURCES.md](SOURCES.md)) are hand-written, scored per condition, and compiled offline by `lib/policy.mjs` into ordinary tree nodes; the app engine is unchanged.

`npm run verify` checks the structure and simulates a textbook patient for every condition. `node trace.mjs <symptom> <conditionKey>` prints the questions such a patient is asked.

## What is data-derived and what is not

| Learned from MedDialog | Hand-written |
|---|---|
| Which chief complaints become categories (≥ 2,000 dialogs) | Emergency red-flag screens: `lib/safety.mjs` |
| Which yes/no questions are asked and in what order | Advice, home care, when-to-seek text: `lib/kb.mjs` |
| Which conditions doctors discussed per answer pattern, with % | Urgency floors and per-condition urgency |
| Starting scores for the follow-up stage | The follow-up questions and their scoring (`lib/deep.mjs`, `lib/kb_deep.mjs`) |

`lib/symptoms.mjs`, `lib/facets.mjs` and `lib/conditions.mjs` are the vocabularies (what to look for); the corpus decides which of them matter.

## Changing things

- **New complaint**: add it to `SYMPTOMS`, `SYMPTOM_FACETS`, `SYMPTOM_META`, `RED_FLAGS` and `FALLBACK`; rerun.
- **A condition appears in a leaf without advice**: `emit.mjs` fails with `No KB entry for "…"`; add it to `lib/kb.mjs`.
- **Safety rule**: a leaf's urgency is the highest of the symptom's floor and every shown condition whose mention rate passes 15% (10% for `serious` ones). The lead advice comes from that most urgent condition, not the most frequent.

Not a medical device. Have a clinician review `lib/safety.mjs` and `lib/kb.mjs` before any public release.
