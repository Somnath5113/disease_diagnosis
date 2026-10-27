/**
 * Hand-curated safety layer. Nothing here is learned from MedDialog: forum chat is
 * not a safe source for emergency rules. The corpus is used only to AUDIT these
 * flags (how often real doctors use urgent language when the flag is mentioned).
 *
 * Condition names must not contain ", " (the app joins/splits on it).
 */

export const EMERGENCY_NUMBER = '112 (ambulance 108 or 102 in many states)'

/** Emergency conclusion templates, reused by many red flags. */
export const EMERGENCY_KINDS = {
  stroke: {
    title: 'Possible stroke',
    urgency: 'EMERGENCY',
    conditions: ['Stroke or TIA', 'Brain bleed', 'Other sudden brain or nerve emergency'],
    advice: 'Sudden weakness, face drooping, slurred speech, or loss of vision can mean a stroke. Treatment works best in the first hours, so do not wait to see if it passes.',
    care: ['Note the exact time the symptoms began and tell the ambulance crew', 'Do not eat, drink, or take aspirin or other medicine unless a doctor tells you to', 'Lie down with your head slightly raised and stay with someone'],
    seek: `Call 112 now. Do not drive yourself.`,
  },
  cardiac: {
    title: 'Possible heart or lung emergency',
    urgency: 'EMERGENCY',
    conditions: ['Heart attack', 'Dangerous heart rhythm', 'Blood clot in the lung'],
    advice: 'Chest pressure, pain spreading to the arm or jaw, fainting, or breathlessness with these symptoms can mean a heart or lung emergency. Do not wait for it to pass or try home remedies.',
    care: ['Stop what you are doing and sit or lie down', 'Only take aspirin if the emergency operator tells you to', 'Unlock the door and keep your phone with you'],
    seek: `Call 112 now. Do not drive yourself.`,
  },
  breathing: {
    title: 'Severe breathing difficulty',
    urgency: 'EMERGENCY',
    conditions: ['Severe asthma attack', 'Severe allergic reaction', 'Serious chest infection'],
    advice: 'Struggling to breathe, being unable to speak a full sentence, or blue lips needs emergency treatment. It can worsen within minutes.',
    care: ['Sit upright and loosen tight clothing', 'Use your rescue inhaler or adrenaline pen if you have one', 'Stay with someone and do not lie flat'],
    seek: `Call 112 now.`,
  },
  airway: {
    title: 'Possible airway swelling or blockage',
    urgency: 'EMERGENCY',
    conditions: ['Severe allergic reaction (anaphylaxis)', 'Deep throat infection', 'Swollen airway'],
    advice: 'Swelling of the lips, tongue, or throat, drooling, or noisy breathing can close the airway. This is an emergency even if you feel only mildly unwell.',
    care: ['Use an adrenaline pen if you have one', 'Sit upright and do not try to swallow food or pills', 'Stay with someone until help arrives'],
    seek: `Call 112 now.`,
  },
  meningitis: {
    title: 'Possible meningitis or brain infection',
    urgency: 'EMERGENCY',
    conditions: ['Meningitis', 'Blood infection (sepsis)', 'Bleeding around the brain'],
    advice: 'Fever with a stiff neck, severe headache, confusion, or a rash that does not fade when pressed can mean meningitis or a blood infection. Hospital treatment is needed straight away.',
    care: ['Do not wait for the rash to spread', 'Press a clear glass on the rash: if it does not fade, say so on the phone', 'Stay with someone and keep warm'],
    seek: `Call 112 now or go to the nearest emergency department.`,
  },
  thunderclap: {
    title: 'Possible bleeding in the brain',
    urgency: 'EMERGENCY',
    conditions: ['Bleeding around the brain (subarachnoid haemorrhage)', 'Stroke or TIA', 'Meningitis'],
    advice: 'A headache that reaches full strength within seconds, or the worst headache of your life, needs emergency scanning.',
    care: ['Rest in a quiet place and avoid straining', 'Do not drive yourself', 'Do not take extra pain medicine while waiting'],
    seek: `Call 112 now.`,
  },
  head_injury: {
    title: 'Possible serious head injury',
    urgency: 'EMERGENCY',
    conditions: ['Head injury with bleeding or swelling', 'Concussion', 'Skull fracture'],
    advice: 'Headache after a head injury with vomiting, drowsiness, confusion, or a seizure needs urgent scanning.',
    care: ['Keep the person still and awake if possible', 'Do not give food, drink, or alcohol', 'Note vomiting, drowsiness, or any change in behaviour'],
    seek: `Call 112 now.`,
  },
  gi_bleed: {
    title: 'Possible internal bleeding',
    urgency: 'EMERGENCY',
    conditions: ['Bleeding stomach ulcer', 'Bleeding from the gut', 'Torn food pipe'],
    advice: 'Vomiting blood, black tarry stools, or passing blood can mean internal bleeding that needs urgent treatment.',
    care: ['Do not eat or drink anything', 'Lie down with your legs raised if you feel faint', 'Do not take aspirin or ibuprofen'],
    seek: `Call 112 now or go to the nearest emergency department.`,
  },
  acute_abdomen: {
    title: 'Possible surgical abdominal emergency',
    urgency: 'EMERGENCY',
    conditions: ['Appendicitis', 'Bowel blockage or perforation', 'Pancreatitis'],
    advice: 'Severe constant belly pain, a hard rigid belly, or pain that keeps you from moving can mean a surgical emergency.',
    care: ['Do not eat or drink in case an operation is needed', 'Do not use a heating pad or take laxatives', 'Avoid strong painkillers until you are seen'],
    seek: `Go to the nearest emergency department now. Call 112 if you cannot travel safely.`,
  },
  ectopic: {
    title: 'Possible ectopic pregnancy or internal bleeding',
    urgency: 'EMERGENCY',
    conditions: ['Ectopic pregnancy', 'Ruptured ovarian cyst', 'Miscarriage'],
    advice: 'Severe one-sided lower belly pain or heavy bleeding when you might be pregnant needs same-hour emergency assessment.',
    care: ['Do not eat or drink in case surgery is needed', 'Lie down and have someone take you to hospital', 'Bring a pregnancy test result if you have one'],
    seek: `Call 112 now or go to the nearest emergency department.`,
  },
  heavy_bleeding: {
    title: 'Dangerous vaginal bleeding',
    urgency: 'EMERGENCY',
    conditions: ['Heavy menstrual bleeding', 'Miscarriage', 'Bleeding disorder'],
    advice: 'Soaking a pad every hour for two hours or more, or feeling faint, can lead to dangerous blood loss.',
    care: ['Lie down with your feet raised', 'Sip water if you can drink', 'Have someone take you to hospital'],
    seek: `Call 112 or go to the nearest emergency department now.`,
  },
  urinary_retention: {
    title: 'Unable to pass urine',
    urgency: 'EMERGENCY',
    conditions: ['Urinary retention', 'Kidney stone blockage', 'Prostate enlargement'],
    advice: 'Being unable to pass any urine, especially with lower belly pain, needs a catheter or relief the same day.',
    care: ['Do not drink large amounts of fluid', 'Do not strain', 'Go to an emergency department now'],
    seek: 'Go to the nearest emergency department now.',
  },
  kidney_sepsis: {
    title: 'Possible kidney infection spreading',
    urgency: 'EMERGENCY',
    conditions: ['Kidney infection (pyelonephritis)', 'Blood infection (sepsis)', 'Kidney stone with infection'],
    advice: 'High fever with shaking chills and severe back or side pain can mean a kidney infection that has reached the blood.',
    care: ['Sip fluids if you can', 'Take paracetamol for the fever only if you have no allergy', 'Go to an emergency department now'],
    seek: `Call 112 or go to the nearest emergency department now.`,
  },
  cauda_equina: {
    title: 'Possible spinal nerve compression',
    urgency: 'EMERGENCY',
    conditions: ['Cauda equina syndrome', 'Severe disc prolapse', 'Spinal cord compression'],
    advice: 'New numbness around the groin, loss of bladder or bowel control, or sudden leg weakness with back pain needs an urgent spinal scan. Delay can cause permanent damage.',
    care: ['Do not wait to see if it improves', 'Avoid bending or lifting', 'Go to an emergency department now'],
    seek: `Go to the nearest emergency department now. Call 112 if you cannot walk.`,
  },
  spine_trauma: {
    title: 'Possible spinal or bone injury',
    urgency: 'EMERGENCY',
    conditions: ['Spinal fracture', 'Bone fracture', 'Internal injury'],
    advice: 'Back pain after a serious fall, road accident, or heavy blow can mean a fracture or internal injury.',
    care: ['Do not move if you have severe neck or back pain', 'Do not eat or drink', 'Wait for the ambulance'],
    seek: `Call 112 now.`,
  },
  septic_joint: {
    title: 'Possible infected joint',
    urgency: 'EMERGENCY',
    conditions: ['Septic arthritis', 'Gout flare', 'Joint injury'],
    advice: 'A hot, red, swollen joint with fever, especially if you cannot move it, can be an infected joint that needs urgent drainage and antibiotics.',
    care: ['Rest the joint', 'Do not put weight on it', 'Do not wait for an appointment'],
    seek: 'Go to the nearest emergency department now.',
  },
  fracture: {
    title: 'Possible fracture or serious joint injury',
    urgency: 'URGENT_CARE',
    conditions: ['Bone fracture', 'Ligament tear', 'Dislocated joint'],
    advice: 'Obvious deformity, being unable to bear weight, or severe pain after an injury needs an X-ray today.',
    care: ['Rest and avoid putting weight on the limb', 'Apply a cold pack wrapped in cloth for 15 minutes at a time', 'Support the limb in the most comfortable position'],
    seek: `Go to urgent care or an emergency department today. Call 112 if there is heavy bleeding or the limb looks pale or blue.`,
  },
  serious_skin: {
    title: 'Possible serious skin or drug reaction',
    urgency: 'EMERGENCY',
    conditions: ['Stevens-Johnson reaction', 'Severe drug reaction', 'Blood infection (sepsis)'],
    advice: 'Widespread blistering, peeling skin, or sores in the mouth or eyes after starting a medicine can be a serious drug reaction.',
    care: ['Stop any new medicine and tell the doctors which one', 'Do not pick at blisters or peel skin', 'Go to an emergency department now'],
    seek: `Go to an emergency department now. Call 112 if breathing is affected.`,
  },
  dehydration: {
    title: 'Severe dehydration',
    urgency: 'EMERGENCY',
    conditions: ['Severe dehydration', 'Kidney strain', 'Electrolyte imbalance'],
    advice: 'Being unable to keep any fluids down, passing almost no urine, or being very drowsy means you probably need fluids through a drip.',
    care: ['Sip oral rehydration solution a teaspoon at a time if you can', 'Do not drink large amounts quickly', 'Go to an emergency department now'],
    seek: `Go to an emergency department now. Call 112 if you are confused or cannot stay awake.`,
  },
  sepsis_fever: {
    title: 'Dangerous fever',
    urgency: 'EMERGENCY',
    conditions: ['Blood infection (sepsis)', 'Meningitis', 'Severe infection'],
    advice: 'Fever in a baby under 3 months, a very high temperature, fits, or confusion needs emergency assessment.',
    care: ['Do not wait to see if the fever settles', 'Remove heavy blankets but do not cool with ice or alcohol', 'Go to an emergency department now'],
    seek: `Call 112 or go to the nearest emergency department now.`,
  },
  crisis: {
    title: 'You may be in crisis',
    urgency: 'EMERGENCY',
    conditions: ['Suicidal thoughts', 'Severe depression', 'Acute mental health crisis'],
    advice: 'Thoughts of ending your life or hurting yourself are a medical emergency. You do not have to face them alone, and help works.',
    care: ['Stay with someone you trust and tell them how you feel', 'Move anything you could use to hurt yourself out of reach', 'Call Tele-MANAS (14416) or 112 now'],
    seek: `Call 112 or Tele-MANAS (14416) now.`,
  },
  throat_blocked: {
    title: 'Possible deep throat infection',
    urgency: 'EMERGENCY',
    conditions: ['Peritonsillar abscess', 'Epiglottitis', 'Deep neck infection'],
    advice: 'Drooling, being unable to swallow saliva, a muffled voice, or noisy breathing can mean the throat is closing. This is an emergency.',
    care: ['Sit upright and lean slightly forward', 'Do not force food, drink, or pills down', 'Go to an emergency department now'],
    seek: `Call 112 now.`,
  },
  coughing_blood: {
    title: 'Coughing up blood',
    urgency: 'URGENT_CARE',
    conditions: ['Chest infection', 'Tuberculosis', 'Lung clot or tumour'],
    advice: 'Coughing up blood needs a doctor and usually a chest X-ray the same day.',
    care: ['Rest and avoid exertion', 'Do not take aspirin or ibuprofen', 'Note how much blood and how often'],
    seek: `Go to an emergency department now if there is more than a teaspoon, you feel faint, or you are breathless. Call 112 if severe.`,
  },
}

/** Per-symptom metadata for app cards and urgency floors. */
export const SYMPTOM_META = {
  periods: { name: 'Period or pregnancy concern', group: 'UROGENITAL', floor: 'ROUTINE_DOCTOR', keywords: 'late period, missed period, irregular periods, pregnancy, bleeding, menstrual pain' },
  urinary: { name: 'Urinary problem', group: 'UROGENITAL', floor: 'ROUTINE_DOCTOR', keywords: 'burning urine, frequent urination, blood in urine, kidney stone, UTI' },
  backpain: { name: 'Back or neck pain', group: 'MUSCULOSKELETAL', floor: 'SELF_CARE', keywords: 'lower back pain, neck pain, sciatica, slipped disc, spine' },
  jointpain: { name: 'Joint or limb pain', group: 'MUSCULOSKELETAL', floor: 'SELF_CARE', keywords: 'knee pain, shoulder pain, arthritis, swollen joint, injury' },
  rash: { name: 'Rash or itching', group: 'SKIN', floor: 'SELF_CARE', keywords: 'rash, itching, hives, blisters, fungal infection, eczema' },
  acne: { name: 'Acne or pimples', group: 'SKIN', floor: 'SELF_CARE', keywords: 'acne, pimples, oily skin, scars' },
  abdpain: { name: 'Stomach pain', group: 'GASTROINTESTINAL', floor: 'ROUTINE_DOCTOR', keywords: 'abdominal pain, stomach ache, cramps, acidity, gastritis' },
  vomiting: { name: 'Vomiting or diarrhea', group: 'GASTROINTESTINAL', floor: 'SELF_CARE', keywords: 'nausea, vomiting, loose motions, diarrhea, food poisoning' },
  cough: { name: 'Cough', group: 'RESPIRATORY', floor: 'SELF_CARE', keywords: 'dry cough, wet cough, phlegm, bronchitis, chest infection' },
  breathless: { name: 'Shortness of breath', group: 'RESPIRATORY', floor: 'URGENT_CARE', keywords: 'breathlessness, wheezing, asthma, difficulty breathing' },
  sorethroat: { name: 'Sore throat, cold or sinus', group: 'RESPIRATORY', floor: 'SELF_CARE', keywords: 'sore throat, tonsils, runny nose, blocked nose, sinus, cold' },
  anxiety: { name: 'Anxiety or stress', group: 'MENTAL', floor: 'ROUTINE_DOCTOR', keywords: 'anxiety, panic attacks, stress, worry, restless' },
  depression: { name: 'Low mood or depression', group: 'MENTAL', floor: 'ROUTINE_DOCTOR', keywords: 'depression, sadness, hopeless, low mood' },
  fever: { name: 'Fever or chills', group: 'FEVER', floor: 'SELF_CARE', keywords: 'fever, chills, high temperature, viral fever, body ache' },
  fatigue: { name: 'Tiredness or weakness', group: 'FEVER', floor: 'ROUTINE_DOCTOR', keywords: 'fatigue, weakness, tiredness, low energy, anemia' },
  numbness: { name: 'Numbness or tingling', group: 'NEUROLOGICAL', floor: 'ROUTINE_DOCTOR', keywords: 'numbness, tingling, pins and needles, nerve, hand, foot' },
  headache: { name: 'Headache', group: 'NEUROLOGICAL', floor: 'SELF_CARE', keywords: 'headache, migraine, head pain, sinus headache' },
  dizziness: { name: 'Dizziness or vertigo', group: 'NEUROLOGICAL', floor: 'ROUTINE_DOCTOR', keywords: 'dizziness, vertigo, lightheaded, spinning, fainting' },
  chestpain: { name: 'Chest pain', group: 'CARDIAC', floor: 'URGENT_CARE', keywords: 'chest pain, chest tightness, heart pain, pressure' },
  palpitation: { name: 'Palpitations or racing heart', group: 'CARDIAC', floor: 'ROUTINE_DOCTOR', keywords: 'palpitations, racing heart, irregular heartbeat, skipped beats' },
}

/**
 * Red-flag screen per symptom. `kind` points into EMERGENCY_KINDS; `audit` is a
 * pattern over the patient's text used only to measure how often doctors respond
 * with urgent language when patients mention it.
 */
export const RED_FLAGS = {
  periods: [
    { id: 'ectopic', kind: 'ectopic', text: 'Severe one-sided lower belly pain, and I could be pregnant', subtitle: 'Late period or positive test, with sharp pain, shoulder-tip pain, or fainting', audit: /ectopic|severe pain.*(pregnan|late)|pregnan.*severe pain/ },
    { id: 'bleed', kind: 'heavy_bleeding', text: 'Soaking a pad every hour, or feeling faint with bleeding', subtitle: 'Large clots, dizziness, or racing heart', audit: /soaking|heavy bleeding|clots|faint/ },
  ],
  urinary: [
    { id: 'retention', kind: 'urinary_retention', text: 'I cannot pass urine at all', subtitle: 'Painful full bladder with nothing coming out', audit: /unable to (pass|urinate)|cannot (pass|urinate)|retention|no urine/ },
    { id: 'kidney', kind: 'kidney_sepsis', text: 'High fever with shaking chills and severe back or side pain', subtitle: 'Feeling very unwell, vomiting, or confused', audit: /chills.*(back|flank|side)|(back|flank|side).*chills|high fever.*(back|kidney)/ },
  ],
  backpain: [
    { id: 'cauda', kind: 'cauda_equina', text: 'Numbness between my legs, or I cannot control my urine or stool', subtitle: 'New leg weakness along with back pain', audit: /incontinen|saddle|loss of (bladder|bowel)|cannot control (urine|bladder|stool)/ },
    { id: 'trauma', kind: 'spine_trauma', text: 'Back pain after a serious fall or accident', subtitle: 'Road accident, fall from height, or heavy blow', audit: /accident|fell from|fall from|fracture/ },
  ],
  jointpain: [
    { id: 'septic', kind: 'septic_joint', text: 'A hot, red, very swollen joint with fever', subtitle: 'I cannot move it, and I feel unwell', audit: /(swollen|red|hot).*(joint|knee).*fever|fever.*(swollen|red).*(joint|knee)/ },
    { id: 'deformity', kind: 'fracture', text: 'Injury with obvious deformity, or I cannot put any weight on it', subtitle: 'Bone looks out of place or the limb looks pale or blue', audit: /fracture|deform|cannot (walk|bear weight)|dislocat/ },
  ],
  rash: [
    { id: 'airway', kind: 'airway', text: 'Rash with swollen lips, face, or tongue, or trouble breathing', subtitle: 'Started after food, medicine, or an insect sting', audit: /swelling of (lips|face|tongue)|anaphyla|angioedema|difficulty breathing/ },
    { id: 'skin_reaction', kind: 'serious_skin', text: 'Widespread blisters, peeling skin, or mouth and eye sores', subtitle: 'After starting a new medicine', audit: /stevens|peeling|blister.*(mouth|eyes)|mouth ulcers.*medicine/ },
    { id: 'purpuric', kind: 'meningitis', text: 'Purple or red spots that do not fade when pressed, with fever', subtitle: 'Press a clear glass on the rash and the spots remain', audit: /purpur|non[- ]?blanching|petechia|do not fade/ },
  ],
  acne: [
    { id: 'cellulitis', kind: 'sepsis_fever', text: 'Fever with a hot, spreading, swollen area of the face', subtitle: 'Red streaks, very painful swelling near the eyes or nose', audit: /cellulitis|spreading redness|swelling near (the )?eye/ },
  ],
  abdpain: [
    { id: 'rigid', kind: 'acute_abdomen', text: 'Severe constant belly pain, or a hard rigid belly', subtitle: 'Cannot stand straight, pain from jolting, or pain moved to the lower right side', audit: /rigid|rebound|appendicitis|peritonitis|perforat|severe abdominal pain/ },
    { id: 'blood', kind: 'gi_bleed', text: 'Vomiting blood, black tarry stools, or blood in the stool', subtitle: 'Coffee-ground vomit or very dark sticky stool', audit: /vomit(ing)? blood|black stool|tarry|blood in (the )?stool|melena|hematemesis/ },
    { id: 'ectopic', kind: 'ectopic', text: 'Severe lower belly pain, and I could be pregnant', subtitle: 'Late period, one-sided pain, or fainting', audit: /ectopic|pregnan.*severe|severe.*pregnan/ },
  ],
  vomiting: [
    { id: 'blood', kind: 'gi_bleed', text: 'I am vomiting blood or have black tarry stools', subtitle: 'Red blood or coffee-ground vomit', audit: /vomit(ing)? blood|blood in vomit|black stool|tarry/ },
    { id: 'dehydrated', kind: 'dehydration', text: 'I cannot keep any fluids down, or passed almost no urine for 8 hours', subtitle: 'Very drowsy, dizzy on standing, or sunken eyes', audit: /dehydrat|cannot (drink|keep)|no urine/ },
    { id: 'neck', kind: 'meningitis', text: 'Vomiting with severe headache, stiff neck, or fever and confusion', subtitle: 'Light hurts my eyes or I feel very drowsy', audit: /stiff neck|meningitis|confus/ },
  ],
  cough: [
    { id: 'breath', kind: 'breathing', text: 'I am struggling to breathe or cannot speak a full sentence', subtitle: 'Blue lips, or gasping even at rest', audit: /struggling to breathe|gasping|blue lips|cyanos|cannot breathe|unable to breathe/ },
    { id: 'blood', kind: 'coughing_blood', text: 'I am coughing up blood', subtitle: 'Streaks, clots, or rusty phlegm', audit: /coughing (up )?blood|blood in (my )?(sputum|phlegm)|hemoptysis|haemoptysis/ },
  ],
  breathless: [
    { id: 'rest', kind: 'breathing', text: 'Severe breathlessness at rest, or I cannot speak a full sentence', subtitle: 'Blue lips, gasping, or choking sound', audit: /at rest|gasping|blue lips|cannot breathe|unable to breathe|cyanos/ },
    { id: 'chest', kind: 'cardiac', text: 'Breathlessness with chest pain, fainting, or a racing heart', subtitle: 'Or a swollen painful calf, or recent long flight or surgery', audit: /chest pain.*(breath|faint)|(breath|faint).*chest pain|pulmonary embol|dvt|swollen calf/ },
    { id: 'allergy', kind: 'airway', text: 'Breathlessness with swollen lips or tongue, or after a sting or new medicine', subtitle: 'Hives or itching all over', audit: /anaphyla|swelling of (lips|tongue|face)|angioedema/ },
  ],
  sorethroat: [
    { id: 'airway', kind: 'throat_blocked', text: 'I am drooling, cannot swallow my saliva, or my voice sounds muffled', subtitle: 'Noisy breathing, cannot open my mouth fully, or breathing feels tight', audit: /drool|cannot swallow|stridor|muffled|epiglott|quinsy|peritonsillar/ },
    { id: 'neck', kind: 'meningitis', text: 'Throat pain with high fever, stiff neck, and confusion', subtitle: 'Severe headache or a rash that does not fade', audit: /stiff neck|meningitis|confus/ },
  ],
  anxiety: [
    { id: 'suicide', kind: 'crisis', text: 'I am having thoughts of ending my life or hurting myself', subtitle: 'Even if you are unsure, choose this', audit: /suicid|end my life|kill myself|self[- ]?harm|want to die/ },
    { id: 'cardiac', kind: 'cardiac', text: 'Chest pressure, pain spreading to the arm or jaw, or fainting', subtitle: 'Heart attacks can feel like panic', audit: /chest pain.*(arm|jaw)|crushing|faint/ },
  ],
  depression: [
    { id: 'suicide', kind: 'crisis', text: 'I am having thoughts of ending my life or hurting myself', subtitle: 'Even if you are unsure, choose this', audit: /suicid|end my life|kill myself|self[- ]?harm|want to die/ },
  ],
  fever: [
    { id: 'neck', kind: 'meningitis', text: 'Fever with stiff neck, severe headache, confusion, or a rash that does not fade', subtitle: 'Light hurts my eyes or I am very drowsy', audit: /stiff neck|meningitis|confus|non[- ]?blanching|purpur/ },
    { id: 'infant', kind: 'sepsis_fever', text: 'The person is a baby under 3 months, has a fit, or the temperature is above 40 °C (104 °F)', subtitle: 'Or breathing fast, floppy, or very hard to wake', audit: /seizure|convuls|febrile|newborn|infant|months? old/ },
    { id: 'breath', kind: 'breathing', text: 'Fever with severe breathing difficulty or blue lips', subtitle: 'Cannot speak a full sentence', audit: /breath|blue lips|cyanos/ },
  ],
  fatigue: [
    { id: 'cardiac', kind: 'cardiac', text: 'Tiredness with chest pain, fainting, or breathlessness at rest', subtitle: 'Or black stools or vomiting blood', audit: /chest pain|faint|syncope|breathless at rest/ },
    { id: 'gi', kind: 'gi_bleed', text: 'Tiredness with black tarry stools or vomiting blood', subtitle: 'Looking very pale or feeling faint', audit: /black stool|tarry|vomit(ing)? blood|melena/ },
  ],
  numbness: [
    { id: 'stroke', kind: 'stroke', text: 'Sudden face drooping, arm or leg weakness, slurred speech, or loss of vision', subtitle: 'Even if it is already improving', audit: /face droop|slurred|sudden weakness|stroke|facial droop|sudden (loss of )?vision/ },
    { id: 'cauda', kind: 'cauda_equina', text: 'Numbness between my legs, or I cannot control my urine or stool', subtitle: 'New leg weakness with back pain', audit: /incontinen|saddle|loss of (bladder|bowel)|cannot control (urine|bladder|stool)/ },
  ],
  headache: [
    { id: 'thunderclap', kind: 'thunderclap', text: 'The worst headache of my life, reaching full strength within seconds', subtitle: 'Like a sudden explosion in the head', audit: /worst headache|thunderclap|sudden severe headache|subarachnoid/ },
    { id: 'stroke', kind: 'stroke', text: 'Headache with face drooping, weakness, slurred speech, or loss of vision', subtitle: 'Or confusion or a seizure', audit: /face droop|slurred|stroke|facial droop|seizure|confus/ },
    { id: 'neck', kind: 'meningitis', text: 'Headache with high fever, stiff neck, or a rash that does not fade', subtitle: 'Light hurts my eyes or I am very drowsy', audit: /stiff neck|meningitis|neck stiff|non[- ]?blanching|purpur/ },
    { id: 'injury', kind: 'head_injury', text: 'Headache after a head injury, with vomiting or drowsiness', subtitle: 'Repeated vomiting, confusion, or a fit', audit: /head injury|hit (my )?head|concuss|fell.*head/ },
  ],
  dizziness: [
    { id: 'stroke', kind: 'stroke', text: 'Dizziness with double vision, slurred speech, weakness, or I cannot walk', subtitle: 'Or sudden severe headache or vomiting', audit: /double vision|slurred|cannot walk|unable to walk|stroke|facial droop/ },
    { id: 'cardiac', kind: 'cardiac', text: 'Dizziness with fainting, chest pain, or a racing or very slow heart', subtitle: 'Fainted without warning', audit: /faint|syncope|collapse|chest pain|palpitation/ },
  ],
  chestpain: [
    { id: 'heart', kind: 'cardiac', text: 'Pressure or crushing pain, spreading to the arm, jaw, or back, with sweating or nausea', subtitle: 'Lasting more than a few minutes, or coming on with effort', audit: /crushing|pressure|radiat.*(arm|jaw)|left arm|jaw|sweat|heart attack|angina/ },
    { id: 'breath', kind: 'cardiac', text: 'Sudden sharp pain with breathlessness, or coughing blood', subtitle: 'Or a swollen calf, or a recent long flight or surgery', audit: /sudden.*breath|breath.*sudden|pulmonary embol|dvt|swollen calf|haemoptysis|hemoptysis/ },
    { id: 'faint', kind: 'cardiac', text: 'Chest pain with fainting, or a tearing pain going through to my back', subtitle: 'Cold sweat or feeling that something is very wrong', audit: /faint|syncope|collapse|tearing|aortic/ },
  ],
  palpitation: [
    { id: 'cardiac', kind: 'cardiac', text: 'Racing heart with chest pain, fainting, or severe breathlessness', subtitle: 'Or confusion or feeling that I will pass out', audit: /faint|syncope|chest pain|breathless|collapse/ },
    { id: 'sustained', kind: 'cardiac', text: 'My heart is racing and will not settle after 15 minutes at rest', subtitle: 'Fast, pounding, and sustained', audit: /not settl|continuous|sustained|svt|tachycardia/ },
  ],
}

/** Urgency ordering helper. */
export const URGENCY_RANK = { SELF_CARE: 0, ROUTINE_DOCTOR: 1, URGENT_CARE: 2, EMERGENCY: 3 }
export const maxUrgency = (a, b) => (URGENCY_RANK[a] >= URGENCY_RANK[b] ? a : b)

/** Group metadata for cards/chips. */
export const GROUPS = {
  RESPIRATORY: { name: 'Breathing and throat' },
  CARDIAC: { name: 'Heart and chest' },
  GASTROINTESTINAL: { name: 'Digestion' },
  NEUROLOGICAL: { name: 'Head and nerves' },
  SKIN: { name: 'Skin' },
  FEVER: { name: 'Fever and general' },
  MUSCULOSKELETAL: { name: 'Bones and muscles' },
  UROGENITAL: { name: 'Urine and reproductive' },
  MENTAL: { name: 'Mind and mood' },
}
