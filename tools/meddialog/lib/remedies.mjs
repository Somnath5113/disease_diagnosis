/**
 * Traditional Indian home remedies shown under an assessment, keyed by the same condition
 * names as `KB` / `KB_DEEP`. Hand-written, not learned from MedDialog.
 *
 * Only kitchen-level, low-risk practices are listed (honey, salt gargle, ORS and chaas, steam,
 * warm compress, haldi doodh, jaggery and greens, pranayama). Evidence for most of these is
 * limited to traditional use or small studies, so they are offered as comfort measures next to
 * the medical advice, never instead of it. Cautions (honey under 1 year, ajwain in pregnancy,
 * diabetes, blood pressure) are written into the item itself.
 *
 * Conditions that are emergencies, infections needing prescribed treatment, or have no safe
 * kitchen remedy are left out on purpose. `emit.mjs` shows remedies only on SELF_CARE and
 * ROUTINE_DOCTOR assessments.
 */
export const REMEDIES = {
  'Acid reflux (GERD)': [
    'Chew half a teaspoon of saunf (fennel seeds) after meals',
    'Sip cool water or a little plain cold milk or chaas (no spices) when you feel burning',
    'Eat dinner early, and keep chai, pickles, and fried snacks to a minimum',
  ],
  'Acne': [
    'Apply a multani mitti (fuller’s earth) pack for 10 minutes once or twice a week if your skin is oily',
    'Dab pure aloe vera gel on spots; patch-test on the inner arm first',
    'Drink plenty of water and cut down on sweets, fried food, and too much milk tea',
  ],
  'Allergic reaction or hives': [
    'Apply pure aloe vera gel or a cool damp cloth to itchy areas',
    'A lukewarm oatmeal (daliya powder) bath can calm itching',
    'Wear loose cotton clothes and avoid hot water on the skin',
  ],
  'Allergic rhinitis or allergy': [
    'Inhale steam of plain hot water for 5 minutes, keeping the face a safe distance away',
    'Sip a warm tulsi and ginger kadha with a little honey (not for children under 1 year)',
    'Haldi doodh (a pinch of turmeric in warm milk) at night is a traditional comfort drink',
  ],
  'Anemia': [
    'Add iron-rich Indian foods: palak and methi leaves, bajra, ragi, rajma, chana, dates, and a little gur (jaggery)',
    'Squeeze lemon over greens and dal; vitamin C helps iron absorption',
    'Cook in an iron kadhai when possible, and keep chai and coffee away from meal times',
  ],
  'Anxiety or panic': [
    'Practise anulom vilom (alternate nostril breathing) or bhramari (humming bee breath) for 5 to 10 minutes daily',
    'A warm glass of milk or tulsi tea in the evening helps some people wind down',
    'A short walk in the morning sun and a few minutes of meditation or yoga nidra',
  ],
  'Bronchitis': [
    'Take a teaspoon of honey with a few drops of ginger juice, or in warm water (not for children under 1 year)',
    'Sip tulsi, ginger, and black pepper kadha 2 to 3 times a day',
    'Steam inhalation with plain hot water, and haldi doodh at night',
  ],
  'Common cold or viral upper respiratory infection': [
    'Tulsi, ginger, and black pepper kadha with a little honey or gur, sipped warm',
    'Steam inhalation 2 to 3 times a day, and gargle with warm salt water',
    'Haldi doodh at night, and plenty of warm fluids such as dal soup or thin khichdi',
  ],
  'Dehydration': [
    'Homemade ORS: 6 level teaspoons of sugar and half a teaspoon of salt in 1 litre of boiled, cooled water',
    'Nimbu pani with a pinch of salt and sugar, nariyal pani (coconut water), or thin chaas with a pinch of salt',
    'Rice kanji (the starchy water from cooked rice) or dal water with a little salt',
  ],
  'Depression': [
    'Get 15 to 20 minutes of morning sunlight and a daily walk, even a short one',
    'Try yoga, pranayama, or meditation, and keep fixed times for meals and sleep',
    'Talk to someone you trust; do not take herbal mood supplements such as ashwagandha or brahmi without asking your doctor',
  ],
  'Diabetes': [
    'Choose whole grains such as jowar, bajra, ragi, and brown rice, and fill half the plate with vegetables and dal',
    'Take a 10 to 15 minute walk after meals',
    'Cut back on sugary chai, sweets, and fruit juice. Do not replace diabetes medicines with methi or karela',
  ],
  'Eczema or dermatitis': [
    'Apply cold-pressed coconut oil or a plain moisturiser on damp skin after a short lukewarm bath',
    'A lukewarm oatmeal bath can ease itching',
    'Wear soft cotton and avoid harsh soaps, hot water, and woollens on the skin',
  ],
  'Fungal skin infection': [
    'Keep the area clean and completely dry, and wear loose cotton clothes',
    'Wash with cooled water boiled with neem leaves as a soothing rinse; it does not replace an antifungal cream',
    'Do not share towels, and dry clothes in direct sunlight',
  ],
  'Gastritis or peptic ulcer': [
    'Eat small, regular meals such as curd rice, banana, plain khichdi, and boiled vegetables',
    'Sip coconut water or cool, unspiced chaas',
    'Avoid chai or coffee on an empty stomach, and cut down on chillies, pickles, and fried food',
  ],
  'Gastroenteritis or food poisoning': [
    'Sip homemade ORS (6 teaspoons of sugar and half a teaspoon of salt in 1 litre of boiled, cooled water), nariyal pani, or salted chaas often',
    'Once hunger returns, eat banana, curd rice, plain khichdi, or dal water',
    'Avoid milk, oily and spicy food until you recover. Drink only boiled or filtered water',
  ],
  'High blood pressure': [
    'Cut salt: limit achar (pickles), papad, namkeen, and processed snacks',
    'Eat potassium-rich foods such as banana, coconut water, and leafy vegetables, unless your doctor has restricted potassium',
    'Walk 30 minutes a day and practise slow pranayama; keep taking your prescribed medicines',
  ],
  'Hormonal imbalance or PCOS': [
    'Choose whole grains, dal, vegetables, and nuts, and cut down on maida, sugar, and cold drinks',
    'Surya namaskar, brisk walking, or yoga for 30 minutes most days',
    'Cumin (jeera) water in the morning is a common habit; check with your doctor before using methi, cinnamon, or turmeric in large amounts',
  ],
  'Intestinal infection or parasites': [
    'Drink only boiled or filtered water, wash vegetables well, and wash hands with soap before eating',
    'Plain curd or chaas and light khichdi are easy on the gut',
    'Keep nails short and take any worm treatment your doctor prescribes for the whole household',
  ],
  'Kidney stone': [
    'Drink 3 litres or more of water a day, spread through the day',
    'Nimbu pani (lemon water) and nariyal pani can help; barley water (jau ka pani) is a traditional choice',
    'Cut back on salt and avoid rich oxalate foods such as spinach in large amounts, if you have had stones before',
  ],
  'Low blood pressure or postural drop': [
    'Drink water or salted nimbu pani, nariyal pani, or chaas through the day',
    'Eat small, frequent meals; soaked raisins or a few dates can help with weakness',
    'Get up slowly from lying or sitting',
  ],
  'Lumbar strain or muscle spasm': [
    'Apply a warm compress or hot water bag for 15 to 20 minutes (garam sikai)',
    'Ajwain potli: heat a cloth bundle of roasted ajwain and rock salt and press gently on the sore area (test the temperature first)',
    'Haldi doodh at night and gentle walking; avoid bed rest for long periods',
  ],
  'Malaria or dengue': [
    'Drink plenty of ORS, nariyal pani, soups, and dal water to prevent dehydration',
    'Use paracetamol only for fever; do not take aspirin or ibuprofen',
    'Papaya leaf juice is popular but is not proven; do not delay testing or treatment for it',
  ],
  'Menstrual irregularity': [
    'Jeera (cumin) water or warm saunf water each morning is a common traditional habit',
    'Eat regular meals with greens, dal, and seasonal fruit, and sleep at fixed times',
    'Light yoga or walking; avoid ajwain and papaya in large amounts if there is any chance you are pregnant',
  ],
  'Migraine': [
    'Rest in a dark, quiet room and apply a cold cloth to the forehead',
    'A cup of ginger tea (adrak chai) at the start of a headache may help; ginger powder has shown benefit in a small trial',
    'Do not skip meals, drink enough water, and keep fixed sleep times',
  ],
  'Muscle or chest wall strain': [
    'Apply a warm compress for 15 minutes a few times a day',
    'Haldi doodh at night',
    'Gentle stretching and deep breathing as pain allows',
  ],
  'Neuropathy or nerve compression': [
    'Eat B12-rich foods: milk, curd, paneer, and eggs if you eat them',
    'Massage hands and feet gently with lukewarm til (sesame) or coconut oil',
    'Change position often and avoid pressure on the nerve',
  ],
  'Pregnancy': [
    'For morning nausea: dry snacks like roasted chana or khakhra before getting up, small frequent meals, and a little ginger or lemon',
    'Avoid papaya (raw or unripe), large amounts of ajwain, and herbal kadhas unless your gynaecologist agrees',
  ],
  'Rheumatoid or inflammatory arthritis': [
    'Warm compress or a warm bath for morning stiffness',
    'Gentle yoga and joint movements in the morning',
    'Haldi doodh is a comfort drink only; keep taking prescribed medicines',
  ],
  'Sinusitis': [
    'Steam inhalation 2 to 3 times a day with plain hot water',
    'Warm compress over the cheeks and forehead',
    'Sip tulsi and ginger kadha, and drink plenty of warm fluids',
  ],
  'Slipped disc or disc bulge': [
    'Warm compress for 15 to 20 minutes for muscle spasm',
    'Short, gentle walks; avoid heavy lifting and forward bending',
    'Haldi doodh at night as a comfort drink',
  ],
  'Tonsillitis or pharyngitis': [
    'Gargle with warm salt water 3 to 4 times a day',
    'Take a teaspoon of honey with grated ginger or tulsi juice (not for children under 1 year)',
    'Warm haldi water gargle or haldi doodh at night; liquorice (mulethi) tea is traditional but avoid it if you have high blood pressure',
  ],
  'Urinary tract infection': [
    'Drink plenty of water, nimbu pani, nariyal pani, or barley water (jau ka pani) to flush the bladder',
    'Chaas or curd is soothing; avoid chai, coffee, and spicy food until it settles',
    'Do not hold urine. These measures do not replace the antibiotic your doctor prescribes',
  ],
  'Viral fever': [
    'Tulsi, ginger, and black pepper kadha, sipped warm',
    'Light, easily digested food: moong dal khichdi, dal soup, or rice kanji',
    'Plenty of fluids: ORS, nariyal pani, nimbu pani. Sponge the forehead with lukewarm water if the fever is high',
  ],
  'Viral skin infection': [
    'A cool oatmeal bath and calamine lotion ease itching',
    'Neem leaves in bath water are a traditional comfort measure for chickenpox-type rashes',
    'Keep nails short, wear loose cotton, and stay away from others until a doctor says it is safe',
  ],
  'Vitamin D or calcium deficiency': [
    'Get 15 to 20 minutes of sunlight on bare arms and legs in the morning, most days',
    'Include milk, curd, paneer, ragi, til (sesame), and eggs if you eat them',
    'Walk or do light weight-bearing exercise',
  ],
  'Vitamin deficiency': [
    'For B12: milk, curd, paneer, and eggs if you eat them; vegetarians usually need supplements as well',
    'Add sprouts, dal, nuts, and seasonal fruit and vegetables to every day',
  ],

  // ---- kb_deep.mjs ----
  'Tension headache': [
    'Gently massage the temples, forehead, and neck with a little warm coconut or til oil',
    'A cup of ginger or tulsi tea, plus a warm compress on the neck and shoulders',
    'Take screen breaks, drink enough water, and practise slow breathing or pranayama',
  ],
  'Eye strain or refractive error': [
    'Follow the 20-20-20 rule: every 20 minutes look at something 20 feet away for 20 seconds',
    'Splash cool water on closed eyes or rest cool cucumber slices on them for 10 minutes',
    'Reduce screen brightness, and get your eyes tested',
  ],
  'Cervical spondylosis': [
    'Warm compress on the neck and shoulders for 15 minutes',
    'Gentle neck stretches and shoulder rolls; avoid heavy pillows and long periods looking down at a phone',
    'Haldi doodh at night as a comfort drink',
  ],
  'Gallstones or gallbladder disease': [
    'Eat light, low-fat food: boiled or steamed vegetables, dal, and fruit; avoid ghee-heavy and fried meals',
    'Do not try oil or lemon “flushes” to dissolve stones; they are not proven and can be harmful',
  ],
  'Irritable bowel syndrome': [
    'Jeera, saunf, or ajwain water after meals can ease gas (avoid ajwain in large amounts during pregnancy)',
    'Plain curd or chaas daily, and eat at regular times in small meals',
    'Yoga such as pawanmuktasana and slow pranayama to reduce stress',
  ],
  'Constipation': [
    'Drink 2 to 3 glasses of warm water on waking, and have more fibre from fruit, vegetables, dalia, and whole grains',
    'Isabgol (psyllium husk): 1 to 2 teaspoons in a glass of water at bedtime, with plenty of water afterwards',
    'Papaya, soaked prunes (aloo bukhara) or raisins, and a regular toilet time each morning',
  ],
  'Stress or weight-related cycle change': [
    'Regular sleep, yoga, and pranayama to lower stress',
    'Eat regular meals with dal, greens, and seasonal fruit; avoid crash diets',
  ],
  'Scabies': [
    'Wash clothes, towels, and bedding in hot water and dry them in strong sunlight',
    'A cool bath or cool damp cloth can ease itching until you get the prescribed cream',
  ],
  'Contact dermatitis': [
    'Wash the area with cool water and stop the suspected soap, cream, or metal',
    'Apply pure aloe vera gel or a cool damp cloth',
    'Keep the skin moisturised with plain coconut oil or a fragrance-free cream',
  ],
  'Heat rash or dry skin': [
    'Take cool baths; sandalwood (chandan) or multani mitti paste with rose water can feel soothing',
    'Wear loose cotton clothes and keep skin folds dry',
    'For dry skin, apply coconut oil or a plain moisturiser after bathing, and drink plenty of water',
  ],
  'Psoriasis': [
    'Moisturise daily with coconut oil, pure aloe vera gel, or a plain emollient',
    'Brief morning sunlight on affected skin can help; avoid sunburn',
    'Avoid scratching and harsh soaps',
  ],
  'Folliculitis': [
    'Apply a warm, clean compress for 10 minutes a few times a day',
    'Wear loose cotton clothes, wash sweaty clothes after each use, and avoid shaving the area',
  ],
  'Rosacea': [
    'Use cool compresses and avoid very hot drinks, spicy food, and alcohol, which trigger flushing',
    'Use sunscreen outdoors and avoid scrubbing the face',
  ],
  'Cosmetic or medicine-related acne': [
    'Stop the suspected cosmetic or hair oil and wash the face gently twice a day',
    'A multani mitti pack once a week can help oily skin',
  ],
  'Sciatica': [
    'Warm compress on the lower back for 15 to 20 minutes',
    'Ajwain potli (warm cloth bundle of roasted ajwain and rock salt) on the sore area, tested for heat first',
    'Short walks and gentle stretches; avoid long sitting and heavy lifting',
  ],
  'Osteoarthritis': [
    'Warm compress, or gently massage the joint with lukewarm til (sesame) oil',
    'Walking, swimming, or gentle yoga to keep joints moving; losing extra weight helps the knees',
    'Haldi doodh is a comfort drink only; do not stop prescribed medicines',
  ],
  'Gout': [
    'Drink plenty of water and nimbu pani through the day',
    'Avoid alcohol, red meat, organ meat, and sugary drinks',
    'Rest the joint and apply a cool cloth for short periods',
  ],
  'Tendinitis or bursitis': [
    'Rest the joint and use a cool cloth for the first 2 days, then a warm compress for stiffness',
    'Haldi doodh at night as a comfort drink',
  ],
  'Carpal tunnel syndrome': [
    'Rest the wrist, avoid long typing or phone use, and keep the wrist straight at night',
    'Gently stretch the fingers and wrist, and shake out the hands during breaks',
  ],
  'Cervical radiculopathy': [
    'Warm compress on the neck and shoulders',
    'Avoid long periods bending the neck down, and use a thin, supportive pillow',
  ],
  'Peripheral neuropathy': [
    'Eat B12-rich foods: milk, curd, paneer, and eggs if you eat them',
    'Massage feet gently with lukewarm til or coconut oil and check feet daily for cuts',
    'If you have diabetes, keep sugar in control; this matters more than any home remedy',
  ],
  'Insomnia or poor sleep': [
    'Warm milk with a pinch of haldi or nutmeg (jaiphal) 30 minutes before bed',
    'Massage the soles of the feet with a little warm oil or ghee before sleep',
    'Switch off screens an hour before bed, and avoid chai or coffee after 4 pm. Try bhramari or slow breathing',
  ],
  'Generalized anxiety': [
    'Anulom vilom and bhramari pranayama for 5 to 10 minutes a day',
    'Tulsi tea in place of extra cups of chai, and a regular walk in daylight',
    'Check with your doctor before taking ashwagandha or brahmi, especially with other medicines',
  ],
  'Panic attacks': [
    'During an attack, breathe in for 4 and out for 6 seconds, and name five things you can see',
    'Anulom vilom pranayama daily and less chai or coffee',
  ],
  'Caffeine or substance effect': [
    'Cut down chai, coffee, and energy drinks gradually to avoid withdrawal headaches',
    'Swap some cups for tulsi tea, jeera or saunf water, or nimbu pani',
  ],
  'Grief or adjustment reaction': [
    'Keep a daily routine, with morning sunlight and a short walk',
    'Talk to family or friends, and try meditation or yoga nidra',
  ],
  'Vestibular neuritis or labyrinthitis': [
    'Sip ginger tea for nausea, and drink plenty of fluids',
    'Rest, then get up slowly; avoid driving until it settles',
  ],
  'Low blood sugar': [
    'At once, take 15 grams of fast sugar: 3 teaspoons of sugar or glucose in water, or half a glass of fruit juice',
    'Follow with a snack such as a roti with dal, a banana, or biscuits',
    'Do not skip meals; always carry sugar or glucose if you take diabetes medicines',
  ],
}

/**
 * Used when no single condition stands out. Keys match `FALLBACK` in kb.mjs. Left out on
 * purpose: breathless, chestpain, palpitation, where a kitchen remedy could delay care.
 */
export const REMEDIES_FALLBACK = {
  periods: ['Warm jeera (cumin) or ajwain water can ease cramps (skip ajwain if there is any chance you are pregnant)', 'A hot water bag on the lower belly for 15 minutes', 'Eat iron-rich food such as palak, dates, and gur, and rest well'],
  urinary: ['Drink plenty of water, nimbu pani, nariyal pani, or barley water (jau ka pani)', 'Chaas or curd is soothing; avoid chai, coffee, and spicy food until it settles'],
  backpain: ['Warm compress for 15 to 20 minutes', 'Ajwain potli (warm cloth bundle of roasted ajwain and rock salt), tested for heat first', 'Haldi doodh at night and gentle walking'],
  jointpain: ['Warm compress, or gentle massage with lukewarm til (sesame) oil', 'Haldi doodh at night as a comfort drink', 'Gentle yoga or walking to keep the joint moving'],
  rash: ['Apply pure aloe vera gel or a cool damp cloth to itchy skin', 'A lukewarm oatmeal bath can ease itching', 'Wear loose cotton clothes and avoid scratching'],
  acne: ['Multani mitti pack once or twice a week if your skin is oily', 'Pure aloe vera gel on spots; patch-test first', 'Drink water, and cut down on sweets and fried food'],
  abdpain: ['Sip jeera, saunf, or ajwain water if the pain feels like gas (skip ajwain in pregnancy)', 'Eat light food such as curd rice or plain khichdi', 'A warm hot water bag on the belly can ease cramps'],
  vomiting: ['Sip ORS, nariyal pani, nimbu pani, or thin chaas often in small amounts', 'A little ginger juice with honey or sucking on a clove may ease nausea (honey not for children under 1 year)', 'Eat banana, curd rice, or khichdi once hunger returns'],
  cough: ['A teaspoon of honey with ginger juice or in warm water (not for children under 1 year)', 'Tulsi, ginger, and black pepper kadha, sipped warm', 'Steam inhalation and warm salt water gargles'],
  sorethroat: ['Gargle with warm salt water 3 to 4 times a day', 'Honey with grated ginger or tulsi juice (not for children under 1 year)', 'Warm haldi doodh at night'],
  anxiety: ['Anulom vilom and bhramari pranayama for 5 to 10 minutes a day', 'Tulsi tea or warm milk in the evening', 'A daily walk in daylight and a fixed sleep time'],
  depression: ['Morning sunlight and a daily walk', 'Yoga, pranayama, or meditation', 'Keep a regular routine and talk to someone you trust'],
  fever: ['Tulsi, ginger, and black pepper kadha, sipped warm', 'Light food such as moong dal khichdi or dal soup', 'ORS, nariyal pani, or nimbu pani; sponge the forehead with lukewarm water'],
  fatigue: ['Eat iron- and protein-rich foods: dal, palak, chana, dates, gur, curd, and eggs if you eat them', 'Drink enough water, and keep fixed meal and sleep times', 'A morning walk in sunlight'],
  numbness: ['Eat B12-rich foods: milk, curd, paneer, and eggs if you eat them', 'Gentle massage of hands and feet with lukewarm til oil', 'Change position often and avoid pressure on the area'],
  headache: ['Drink water and rest in a dark, quiet room', 'Ginger or tulsi tea, and gentle head and neck massage with warm oil', 'Do not skip meals, and keep fixed sleep times'],
  dizziness: ['Sit or lie down, and drink ORS, nimbu pani with a pinch of salt, or nariyal pani', 'Eat small, regular meals; soaked raisins or dates can help with weakness', 'Get up slowly'],
}
