# MedTriage: Offline Clinical Intake & Symptom Triage Engine

MedTriage is a 100% offline, privacy-first patient intake and symptom evaluation Android app built with Kotlin and Jetpack Compose. Its questions and likely-condition statistics are **mined offline from the English MedDialog corpus** (~212k iCliniq + HealthCareMagic doctor-patient dialogs, `lighteval/med_dialog`) by the pipeline in `tools/meddialog/`. The result is a static decision tree (`triage_tree.json`) covering the 20 most frequent triage-relevant chief complaints, with hand-written emergency red-flag screens, local Room history, and exportable clinical visit summaries. No backend and no AI at run time.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural parameters have been established based on your choices:

- **Symptom Domains (Data-selected)**: Chief complaints are chosen by corpus frequency (≥ 2,000 MedDialog dialogs), excluding topics that cannot be triaged by symptom questions (sexual health, dental, blood-pressure readings). Result: 20 complaints in 9 body-system groups — Respiratory (cough, shortness of breath, sore throat/cold), Heart & chest (chest pain, palpitations), Digestion (stomach pain, vomiting/diarrhea), Head & nerves (headache, dizziness, numbness), Skin (rash/itching, acne), Fever & general (fever, tiredness), Bones & muscles (back/neck pain, joint pain), Urine & reproductive (urinary, periods/pregnancy), Mind & mood (anxiety, depression).
- **Data Persistence & Privacy (Confirmed)**: 100% local on-device SQLite storage using **Room Database**. No network permissions or cloud backend needed; patient data never leaves the device.
- **Consultation Export (Confirmed)**: Generates a structured "Doctor Consultation Summary" (Chief Complaint, Chronological Answers, Clinical Rule Path, Urgency Level, and Timestamp) shareable via the standard Android share sheet.
- **Emergency Safety Intercept (Confirmed)**: Instant detection of red-flag symptoms (e.g. crushing chest pain, sudden unilateral numbness, anaphylactic breathing distress, stiff neck with high fever). Triggers a full-screen emergency alert modal with one-touch emergency hotline dialing (`Intent.ACTION_DIAL`).

---

## 1. Overview & Core Concept

- **What It Does**: Patients select their primary concern or search their chief complaint, walk through a step-by-step diagnostic questionnaire derived from real-world clinical consultation protocols (MedDialog), receive immediate triage classifications (Emergency / Urgent Physician Visit / Routine Appointment / Self-Care Advice), and maintain a private timeline of assessments.
- **Target Audience / Persona**: Individuals seeking structured pre-appointment intake guidance, parents checking symptom severity, or patients in low-connectivity/offline environments who need immediate clinical decision support without privacy risks.
- **Key Value**: Replaces black-box generative AI hallucinations and unreliable online search rabbit holes with validated, deterministic clinical pathways, guaranteed zero latency, and strict patient confidentiality.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Intake / Home Screen**:
   - Welcome banner with medical disclaimer badge ("Clinical Decision Support Tool - Not a Substitute for Emergency Services").
   - Quick Search bar for symptoms and category chips (Respiratory, Gastro, Neuro, Derm, Fever).
   - Prominent "Start New Intake" primary card with visual system icons.
   - Recent Consultations preview list with urgency status pills.
2. **Interactive Consultation Screen**:
   - Multi-step questionnaire card with animated step transitions (`AnimatedContent`).
   - Progress indicator (current step, estimated depth, breadcrumb back navigation).
   - Question header, clarifying clinical context ("Why we ask: ..."), and single-select or multi-choice response cards with tactile ripple feedback.
   - Red-Flag Monitor: If any selected symptom meets critical criteria, the flow halts immediately into the **Emergency Alert Modal**.
3. **Assessment & Recommendation Outcome Screen**:
   - Triage Urgency Banner:
     - 🔴 **Immediate Emergency** (Emergency Room / 911 / 112)
     - 🟠 **Urgent Medical Attention** (Seek clinic within 12–24h)
     - 🟡 **Routine Physician Evaluation** (Schedule non-emergency appointment)
     - 🟢 **Supportive Self-Care & Monitoring** (Home remedies with red-flag warning thresholds)
   - Diagnostic Insights: Possible clinical considerations matching MedDialog disease categories (e.g., Bronchitis, Migraine, Gastroenteritis, Allergic Contact Dermatitis).
   - Doctor Intake Summary: Chronological log of symptoms, duration, severity, and clarifying answers.
   - Action Buttons: "Share Summary with Doctor", "Save Assessment", and "Return to Home".
4. **Local History & Records Screen**:
   - Filterable timeline of past assessments by date and symptom category.
   - Full detail view for re-reading past recommendations and sharing doctor summaries.

### Visual Identity & Theme

- **Aesthetic Direction**: Clean Clinical Minimalism meets modern Material 3 ergonomics. Inspires calm, confidence, and high legibility under stress.
- **Color Palette**:
  - `Primary`: Deep Clinical Teal (`#00696E` / dark: `#80D5DC`)
  - `PrimaryContainer`: Soft Aqua Mint (`#9CF0F7` / dark: `#004F53`)
  - `Secondary`: Slate Indigo (`#4A6267` / dark: `#B0CBD1`)
  - `Urgency Critical`: Crimson Red (`#BA1A1A` / dark: `#FFB4AB`)
  - `Urgency Moderate`: Warm Amber (`#B06000` / dark: `#FFB77B`)
  - `Urgency Low`: Forest Sage (`#2E6A38` / dark: `#97D59A`)
  - `Surface`: Warm Off-White (`#FAFDFD` / dark: `#191C1D`)
- **Typography**: Clean humanist sans-serif with accessible line-heights and high contrast ratios.
- **Component Styling**: Rounded card containers (16.dp corner radius), minimum 48.dp touch targets, clear icons for symptom identification, and high contrast urgency tags.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Bundled Structured JSON vs. Hardcoded Kotlin Logic
- **Chosen Approach**: Store the MedDialog-derived decision tree in a typed JSON asset (`assets/triage_tree.json`) loaded and cached into memory via Moshi on startup. The file is **generated** by `tools/meddialog/` (mine → prune → emit → validate), never edited by hand.
- **How the data is used**: per complaint, a multi-label decision tree is trained on patient-text facets (e.g. "mentions fever") against conditions named in the doctor's reply, then pruned on a validation slice and scored on an untouched 15% test slice (see `tools/meddialog/REPORT.md`). Leaf percentages are mention rates among similar dialogs, not diagnoses.
- **What is not data-derived**: emergency red-flag screens, urgency floors, and advice text are curated (`lib/safety.mjs`, `lib/kb.mjs`) because forum text is not a safe source for them. Every complaint starts with the red-flag screen; a leaf's lead advice comes from its most urgent plausible condition, not the most frequent.
- **Why**: Separates medical content from presentation code, makes the question set reproducible and auditable from the dataset, and lets the corpus decide which questions matter.
- **Alternatives Considered**: Hardcoded Kotlin `when` expressions would create maintenance bloat and violate the 500-line modularity guideline.

### Decision 2: Zero-Permission Share Sheet vs. PDF Generation
- **Chosen Approach**: Formats the doctor consultation summary into a standardized clinical text/markdown structure dispatched via Android `Intent.ACTION_SEND` (Share via WhatsApp, Email, Notes, SMS).
- **Why**: Eliminates heavy PDF generation libraries, works seamlessly across all Android devices, and avoids file storage permissions while still providing doctor-ready documentation.

### Decision 3: Local SQLite via Room
- **Chosen Approach**: Store completed triage sessions in a Room entity (`TriageRecordEntity`) containing timestamp, category, chief complaint, outcome status, matched conditions, and full Q&A transcript.
- **Why**: 100% offline, reactive with Kotlin `Flow`, resilient across app updates, and maintains complete patient confidentiality on device.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       Presentation Layer                    │
│                                                             │
│   HomeScreen           ConsultationScreen   HistoryScreen   │
│   (Category grid,      (Animated Q&A,       (Timeline,      │
│    search, intake CTA)  emergency alerts)    detail dialog) │
└──────────────┬──────────────────┬──────────────────┬────────┘
               │                  │                  │
               ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                       ViewModel Layer                       │
│                                                             │
│   HomeViewModel         TriageViewModel     HistoryViewModel│
│   (Recent intake stats) (State machine,     (Room Flow,     │
│                          branch traversal,   deletion)      │
│                          red-flag check)                    │
└──────────────┬──────────────────┬──────────────────┬────────┘
               │                  │                  │
               ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                        Domain Layer                         │
│                                                             │
│   TriageEngine          RedFlagDetector    SummaryFormatter │
│   (Tree traversal)      (Safety check)     (Doctor memo)    │
└──────────────┬──────────────────┬───────────────────────────┘
               │                  │
               ▼                  ▼
┌───────────────────────────────────────┬─────────────────────┐
│              Data Layer               │   Bundled Assets    │
│                                       │                     │
│  TriageRepository                     │  assets/            │
│  └── Room DB (AppDatabase)            │  └── triage_tree    │
│      ├── TriageRecordEntity           │      .json          │
│      └── TriageDao (Flow reactive)    │  (MedDialog trees)  │
└───────────────────────────────────────┴─────────────────────┘
```

### Data Models & Decision Tree Schema

- **`TriageNode`**:
  - `id: String` (e.g. `resp_cough_start`, `neuro_headache_redflag`)
  - `category: SymptomCategory` (Respiratory, Gastro, Neuro, Derm, Fever)
  - `question: String` (Doctor query from MedDialog)
  - `explanation: String?` (Clinical rationale for the question)
  - `isEmergencyCheck: Boolean` (Whether this node tests critical red-flags)
  - `options: List<TriageOption>`
  - `isConclusion: Boolean`
  - `conclusionData: TriageConclusion?`
- **`TriageOption`**:
  - `text: String`
  - `subtitle: String?`
  - `nextId: String`
  - `isRedFlag: Boolean`
- **`TriageConclusion`**:
  - `urgencyLevel: UrgencyLevel` (`EMERGENCY`, `URGENT_CARE`, `ROUTINE_DOCTOR`, `SELF_CARE`)
  - `likelyConditions: List<String>` (e.g., "Acute Bronchitis", "Viral Upper Respiratory Infection")
  - `clinicalAdvice: String`
  - `homeCareInstructions: List<String>`
  - `whenToSeekImmediateHelp: String`
- **`TriageRecordEntity` (Room)**:
  - `id: Long` (auto-generated primary key)
  - `timestamp: Long`
  - `category: String`
  - `chiefComplaint: String`
  - `urgencyLevel: String`
  - `likelyConditionsJson: String`
  - `transcriptJson: String` (Full history of questions and selected answers)

### Interactive State Transitions

- **Start Session**: User picks symptom category $\to$ initializes `TriageViewModel` with root node $\to$ resets history path.
- **Answer Selection**: User taps option $\to$ option marked in transcript $\to$ if `isRedFlag`, triggers Emergency Dialog immediately $\to$ if not, fetches `nextId` node with smooth animated sliding transition.
- **Backtrack Support**: User can tap "Back" to undo previous answer and take an alternate path without restarting.
- **Session Complete**: Displays comprehensive recommendation card $\to$ triggers `saveRecord()` to Room $\to$ offers Share Summary button.

---

## 5. Verification Plan

1. **JSON Tree Validation**: `npm run emit` in `tools/meddialog/` fails on any dead end, unreachable node, cycle, missing advice, or condition name containing ", ". `ExampleRobolectricTest` re-checks the shipped asset (all categories start on an emergency screen; every red flag leads to a conclusion).
   - **Data check**: held-out log-likelihood gain over a no-question baseline is positive for all 20 complaints (`REPORT.md`).
2. **Build & Compilation**: Run `compile_applet` to ensure Room annotations, KSP generation, Moshi converters, and Compose UI compile cleanly.
3. **Emergency Intercept Test**: Verify red-flag triggers immediately display emergency overlay and dispatch dial intent.
4. **Room Persistence & Summary Export**: Verify past intakes correctly save, load reactively, and produce a well-formatted doctor consultation summary.
