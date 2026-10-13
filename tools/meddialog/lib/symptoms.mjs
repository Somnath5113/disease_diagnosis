/**
 * Candidate chief-complaint lexicon. This is only the *vocabulary*; which symptoms
 * (and therefore which app categories) survive is decided by corpus frequency in
 * analyze_symptoms.mjs / build_tree.mjs.
 *
 * `system` groups symptoms into app categories.
 */
export const SYMPTOMS = {
  cough: { system: 'RESPIRATORY', label: 'Cough', re: /\bcough(ing|s)?\b|phlegm|sputum/ },
  breathless: { system: 'RESPIRATORY', label: 'Shortness of breath', re: /short(ness)? of breath|breathless|difficulty (in )?breathing|breathing (problem|difficult)|wheez/ },
  sorethroat: { system: 'RESPIRATORY', label: 'Sore throat / cold', re: /sore throat|throat (pain|infection|irritation)|tonsil|runny nose|nasal congestion|stuffy nose|common cold|sinus/ },
  chestpain: { system: 'CARDIAC', label: 'Chest pain', re: /chest (pain|tight|discomfort|pressure)|pain in (the )?chest/ },
  palpitation: { system: 'CARDIAC', label: 'Palpitations / racing heart', re: /palpitation|heart (racing|beat|rate)|racing heart|skipped beat|irregular heart/ },
  bp: { system: 'CARDIAC', label: 'Blood pressure', re: /blood pressure|hypertension|\bbp\b/ },
  abdpain: { system: 'GASTROINTESTINAL', label: 'Abdominal / stomach pain', re: /abdominal pain|stomach (pain|ache)|pain in (the )?(abdomen|stomach)|belly (pain|ache)|abdomen pain|cramps?\b/ },
  vomiting: { system: 'GASTROINTESTINAL', label: 'Nausea / vomiting', re: /vomit|nausea|nauseous/ },
  diarrhea: { system: 'GASTROINTESTINAL', label: 'Diarrhea / loose stools', re: /diarrh|loose (motion|stool)|watery stool/ },
  constipation: { system: 'GASTROINTESTINAL', label: 'Constipation', re: /constipat|hard stool|difficulty (in )?(passing|moving) (stool|bowel)/ },
  acidity: { system: 'GASTROINTESTINAL', label: 'Heartburn / acidity', re: /acid reflux|heartburn|acidity|gerd|indigestion|gastritis|burning (in )?(the )?(stomach|chest)/ },
  headache: { system: 'NEUROLOGICAL', label: 'Headache', re: /headache|head ache|migraine|pain in (the )?head/ },
  dizziness: { system: 'NEUROLOGICAL', label: 'Dizziness / vertigo', re: /dizz|vertigo|light[- ]?headed|giddiness/ },
  numbness: { system: 'NEUROLOGICAL', label: 'Numbness / tingling', re: /\bnumb|tingling|pins and needles/ },
  rash: { system: 'SKIN', label: 'Rash / skin eruption', re: /\brash|hives|urticaria|blister|boils?\b|bumps? on|red spots|skin (lesion|infection|eruption)|eczema|ringworm|fungal/ },
  itching: { system: 'SKIN', label: 'Itching', re: /itch/ },
  acne: { system: 'SKIN', label: 'Acne / pimples', re: /acne|pimple|blackhead/ },
  hairloss: { system: 'SKIN', label: 'Hair loss', re: /hair (loss|fall)|falling hair|baldness|alopecia/ },
  fever: { system: 'FEVER', label: 'Fever / chills', re: /\bfever|temperature|chills/ },
  fatigue: { system: 'FEVER', label: 'Fatigue / weakness', re: /fatigue|tired|weakness|body ache|lethargy|exhaust/ },
  weightloss: { system: 'FEVER', label: 'Weight loss', re: /weight loss|losing weight|lost weight|weight gain|gaining weight/ },
  backpain: { system: 'MUSCULOSKELETAL', label: 'Back / neck pain', re: /back pain|backache|pain in (my |the )?(lower )?back|neck pain|spine|sciatica|slip disc|disc/ },
  jointpain: { system: 'MUSCULOSKELETAL', label: 'Joint / limb pain', re: /joint pain|knee pain|arthritis|pain in (my |the )?(knee|joint|leg|arm|shoulder|hip|ankle|foot|wrist)|leg pain|shoulder pain|swollen (joint|knee|ankle)/ },
  urinary: { system: 'UROGENITAL', label: 'Urinary problem', re: /urin|burning (sensation )?(while|during|on) (passing|urinat)|uti\b|kidney stone|frequent urination/ },
  periods: { system: 'UROGENITAL', label: 'Period / menstrual problem', re: /menstru|\bperiods?\b(?! of time)|pregnan|vaginal|white discharge|pcos|pcod/ },
  sexual: { system: 'UROGENITAL', label: 'Sexual health', re: /erectile|erection|premature ejaculation|masturbat|sex(ual)?\b|libido|sperm/ },
  anxiety: { system: 'MENTAL', label: 'Anxiety / stress', re: /anxi|panic|stress|worry|fear/ },
  depression: { system: 'MENTAL', label: 'Low mood / depression', re: /depress|sad|hopeless|suicid/ },
  insomnia: { system: 'MENTAL', label: 'Sleep problems', re: /insomnia|sleep(ing)? (problem|difficult|issue)|cannot sleep|can't sleep|unable to sleep/ },
  earpain: { system: 'ENT_EYE', label: 'Ear problem', re: /\bear\b.*(pain|ache|discharge|block|ring)|tinnitus|hearing/ },
  eye: { system: 'ENT_EYE', label: 'Eye problem', re: /\beyes?\b.*(pain|red|itch|water|vision)|blurred vision|vision (problem|loss)|conjunctiv/ },
  dental: { system: 'ENT_EYE', label: 'Tooth / mouth problem', re: /tooth|teeth|gum|mouth ulcer|dental/ },
}

/**
 * Assign one primary symptom per dialog. Title (`tgt`) is a human summary of the
 * chief concern so it is weighted heavily; the patient's opening is the fallback.
 * Returns the symptom key or null.
 */
const MERGE = { itching: 'rash', diarrhea: 'vomiting' }

/** Label from the dialog title only (used to evaluate free-text matching without peeking at the query text). */
export function titleSymptom(d) {
  let best = null
  let pos = Infinity
  for (const [key, s] of Object.entries(SYMPTOMS)) {
    const t = d.titleLc.search(s.re)
    if (t >= 0 && t < pos) { best = key; pos = t }
  }
  return best ? (MERGE[best] || best) : null
}

export function primarySymptom(d) {
  // Earliest mention wins: first in the title, otherwise first in the patient's opening.
  // (Ties are by position in the text, not by lexicon order.)
  const opening = d.patient.slice(0, 350)
  let best = null
  let bestKey = [2, Infinity]
  for (const [key, s] of Object.entries(SYMPTOMS)) {
    const t = d.titleLc.search(s.re)
    const o = t < 0 ? opening.search(s.re) : -1
    const k = t >= 0 ? [0, t] : o >= 0 ? [1, o] : null
    if (k && (k[0] < bestKey[0] || (k[0] === bestKey[0] && k[1] < bestKey[1]))) {
      best = key
      bestKey = k
    }
  }
  return best ? (MERGE[best] || best) : null
}
