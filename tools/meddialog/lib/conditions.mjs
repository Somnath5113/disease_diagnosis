/**
 * Condition vocabulary used to LABEL dialogs from the doctor's reply.
 * Each entry: canonical display name (no commas — the app joins with ", ") and
 * a regex over lower-cased doctor text. Which conditions matter for a given symptom
 * is decided by frequency in the corpus (see build_tree.mjs), not listed here.
 */
export const CONDITIONS = {
  // respiratory / ENT
  'Common cold or viral upper respiratory infection': /common cold|upper respiratory (tract )?infection|\burti\b|\buri\b|cold and cough/,
  'Bronchitis': /bronchitis/,
  'Asthma or reactive airway': /asthma|bronchospasm|wheez|reactive airway/,
  'Pneumonia or chest infection': /pneumonia|chest infection|lower respiratory/,
  'Allergic rhinitis or allergy': /allergic (rhinitis|cough|bronchitis|condition|problem|disorder|trigger)|rhinitis|hay fever|post[- ]?nasal|due to (an )?allerg|dust allerg|pollen|allergen/,
  'Sinusitis': /sinusitis|sinus infection/,
  'Tonsillitis or pharyngitis': /tonsillitis|pharyngitis|strep/,
  'Acid reflux (GERD)': /\bgerd\b|reflux|\bacidity\b|hyperacidity|heartburn|acid peptic/,
  'Tuberculosis': /tuberculosis|\btb\b|koch/,
  'COPD or smoking-related lung disease': /copd|emphysema|chronic obstructive/,
  'Anxiety or panic': /anxiety|panic (attack|disorder)|psychosomatic|anxious/,
  // cardiac
  'Angina or heart disease': /angina|coronary|heart disease|cardiac (cause|problem)|ischemi|myocardial|heart attack/,
  'Muscle or chest wall strain': /costochondritis|chest wall|muscular|muscle (pain|strain|spasm)|musculoskeletal|intercostal/,
  'High blood pressure': /hypertension|high blood pressure|\bbp\b/,
  'Arrhythmia or ectopic beats': /arrhythmia|ectopic|extra ?systole|premature (beats|contraction)|tachycardia|palpitation|atrial|\bpvc\b/,
  'Thyroid disorder': /thyroid/,
  'Anemia': /anemi|anaemi|hemoglobin|haemoglobin|\biron\b/,
  // GI
  'Gastroenteritis or food poisoning': /gastroenteritis|food poisoning|stomach (bug|flu|infection)|gastro/,
  'Gastritis or peptic ulcer': /gastritis|ulcer|h\.? ?pylori|pylori/,
  'Irritable bowel syndrome': /irritable bowel|\bibs\b/,
  'Constipation': /constipat/,
  'Appendicitis': /appendicitis|appendix/,
  'Gallstones or gallbladder disease': /gallstone|gall ?bladder|cholecyst|biliary/,
  'Urinary tract infection': /\buti\b|urinary tract infection|urine infection|cystitis|bladder infection|urinary infection/,
  'Kidney stone': /kidney stone|renal (stone|calcul|colic)|ureteric|calculi|calculus/,
  'Intestinal infection or parasites': /worms?\b|parasit|amoeb|giardia|typhoid|dysentery/,
  'Pregnancy': /pregnan/,
  'Hormonal imbalance or PCOS': /pcos|pcod|polycystic|hormon/,
  'Menstrual irregularity': /irregular (period|cycle|menses)|menstrual (irregular|cycle)|delayed (period|menses)|late period/,
  'Fibroid or ovarian cyst': /fibroid|ovarian cyst|\bcyst\b/,
  'Pelvic or vaginal infection': /vaginal infection|vaginitis|candid|yeast|pelvic inflammatory|\bpid\b|bacterial vaginosis|\bstd\b|sexually transmitted/,
  'Prostate problem': /prostat/,
  'Dehydration': /dehydrat/,
  // neuro
  'Migraine': /migraine/,
  'Tension headache': /tension (type )?headache|stress headache|tension[- ]type/,
  'Cluster headache': /cluster headache/,
  'Cervical spondylosis': /cervical spondyl|cervical (spine|disc)|neck (strain|problem)|spondylosis/,
  'Eye strain or refractive error': /eye ?strain|refractive|eye sight|glasses|spectacle|vision problem/,
  'Vertigo (BPPV or inner ear)': /vertigo|bppv|labyrinth|vestibular|inner ear|meniere/,
  'Low blood pressure or postural drop': /low blood pressure|hypotension|orthostatic|postural/,
  'Vitamin deficiency': /vitamin ?(b ?12|d|b)|b12|deficien/,
  'Neuropathy or nerve compression': /neuropath|nerve (compression|damage|entrap)|carpal tunnel|radiculopathy|pinched nerve|sciatica|nerve root/,
  'Diabetes': /diabet|blood sugar|glucose|hba1c/,
  'Epilepsy or seizure disorder': /epilep|seizure|convulsion/,
  'Stroke or TIA': /\bstroke\b|\btia\b|cerebrovascular/,
  'Depression': /depress(ion|ed|ive)\b(?! respiratory)/,
  // musculoskeletal
  'Lumbar strain or muscle spasm': /muscle (spasm|strain|pull)|lumbar strain|mechanical (back|pain)|back strain|sprain|strain/,
  'Slipped disc or disc bulge': /slip(ped)? disc|disc (bulge|prolapse|herniation)|herniat|prolapse|lumbar spondyl|\bpid\b/,
  'Sciatica': /sciatica/,
  'Osteoarthritis': /osteoarthritis|degenerative|arthrosis|wear and tear/,
  'Rheumatoid or inflammatory arthritis': /rheumatoid|arthritis|\bra factor|gout|uric acid|autoimmune/,
  'Vitamin D or calcium deficiency': /vitamin d|calcium|osteopor|bone density/,
  'Injury or fracture': /fracture|injur|trauma|ligament|meniscus|tendon|tear\b/,
  'Tendinitis or bursitis': /tendin|bursitis|tennis elbow|frozen shoulder|rotator|plantar fasci/,
  // skin
  'Fungal skin infection': /fungal|ringworm|tinea|candid|dermatophyt|athlete'?s foot/,
  'Eczema or dermatitis': /eczema|dermatitis|atopic/,
  'Allergic reaction or hives': /urticaria|hives|allergic (reaction|rash)|angioedema/,
  'Scabies': /scabies/,
  'Bacterial skin infection': /bacterial (skin )?infection|impetigo|cellulitis|folliculitis|abscess|boil|furuncle/,
  'Viral skin infection': /viral (rash|infection)|herpes|shingles|zoster|chicken ?pox|varicella|molluscum|wart|measles/,
  'Psoriasis': /psoria/,
  'Acne': /\bacne\b|pimple/,
  'Hair or scalp condition': /dandruff|seborrh|alopecia|telogen|androgenetic|hair (fall|loss)/,
  'Heat rash or dry skin': /heat rash|prickly|dry skin|xerosis|insect bite/,
  // general / fever
  'Viral fever': /viral (fever|infection|illness|urti|uri|gastro\w*)|\bflu\b|influenza/,
  'Malaria or dengue': /malaria|dengue|chikungunya/,
  'Typhoid': /typhoid|enteric fever/,
  'Chronic fatigue or sleep problem': /insomnia|sleep (deprivation|problem|disorder|apnea)|chronic fatigue|poor sleep/,
  'Insomnia': /insomnia/,
}

/** Conditions whose *name* implies a possible emergency — used only for audit stats. */
export const SERIOUS = new Set([
  'Angina or heart disease', 'Appendicitis', 'Stroke or TIA', 'Pneumonia or chest infection',
  'Kidney stone', 'Gallstones or gallbladder disease', 'Tuberculosis', 'Malaria or dengue', 'Typhoid',
])

/** Count every vocabulary condition mentioned in a doctor's reply. */
export function conditionMentions(doctorText) {
  const hits = []
  for (const [name, re] of Object.entries(CONDITIONS)) if (re.test(doctorText)) hits.push(name)
  return hits
}
