// Usage: node eval_search.mjs <raw-dir>
// Compares ways to map a patient's typed words to one of the app's complaints:
//   tfidf      – keyword (TF-IDF, class centroid), no model
//   embed-cent – MiniLM embedding, nearest class centroid
//   embed-knn  – MiniLM embedding, k nearest labelled examples
// Labels come from dialog titles; the query is the patient's own text (title not included).
import { pipeline } from '@huggingface/transformers'
import { loadDialogs, hash01 } from './lib/load.mjs'
import { titleSymptom } from './lib/symptoms.mjs'
import { SYMPTOM_META } from './lib/safety.mjs'

const TRAIN_PER = 400
const TEST_PER = 150
const classes = Object.keys(SYMPTOM_META)

const dialogs = await loadDialogs(process.argv[2])
const train = []
const test = []
const cnt = {}
for (const d of dialogs) {
  const s = titleSymptom(d)
  if (!s || !SYMPTOM_META[s]) continue
  const h = hash01(d.id)
  const text = d.patient.replace(/^(hi|hello|dear)\b[^,.]{0,20}[,.]?\s*/i, '').slice(0, 250)
  if (text.length < 20) continue
  const key = `${s}:${h < 0.7 ? 'tr' : h >= 0.85 ? 'te' : 'x'}`
  if (key.endsWith(':x')) continue
  cnt[key] = (cnt[key] || 0) + 1
  if (h < 0.7 && cnt[key] <= TRAIN_PER) train.push({ s, text })
  else if (h >= 0.85 && cnt[key] <= TEST_PER) test.push({ s, text })
}
console.error(`train=${train.length} test=${test.length}`)

// Short, realistic things a patient might type. Written independently of the lexicons.
const HAND = [
  ['chestpain', 'my chest feels heavy when I walk'], ['chestpain', 'pain in the left side of my chest'],
  ['breathless', 'I get out of breath climbing stairs'], ['breathless', 'wheezing and cannot breathe properly'],
  ['cough', 'dry cough for two weeks'], ['cough', 'coughing up yellow phlegm'],
  ['headache', 'throbbing pain on one side of my head'], ['headache', 'my head hurts every evening'],
  ['dizziness', 'the room is spinning when I turn in bed'], ['dizziness', 'I feel lightheaded when I stand up'],
  ['abdpain', 'sharp pain in my lower stomach'], ['abdpain', 'burning pain in upper belly after eating'],
  ['vomiting', 'I have been throwing up since last night'], ['vomiting', 'loose motions three times today'],
  ['fever', 'high temperature and shivering'], ['fever', 'fever for three days with body ache'],
  ['rash', 'red itchy spots all over my arms'], ['rash', 'skin is itching badly at night'],
  ['acne', 'lots of pimples on my face'], ['acne', 'oily skin and acne scars'],
  ['backpain', 'lower back pain after lifting'], ['backpain', 'neck is stiff and painful'],
  ['jointpain', 'my knee hurts and is swollen'], ['jointpain', 'pain in both shoulders and wrists'],
  ['urinary', 'burning when I pass urine'], ['urinary', 'I need to pee all the time'],
  ['periods', 'my period is late and I might be pregnant'], ['periods', 'irregular periods for months'],
  ['anxiety', 'I get panic attacks and my heart races'], ['anxiety', 'constantly worried and cannot relax'],
  ['depression', 'I feel sad and hopeless all the time'], ['depression', 'lost interest in everything'],
  ['fatigue', 'always tired and weak'], ['fatigue', 'no energy even after sleeping'],
  ['numbness', 'tingling in my fingers and toes'], ['numbness', 'my hand goes numb at night'],
  ['palpitation', 'my heart skips beats'], ['palpitation', 'heart pounding fast for no reason'],
  ['sorethroat', 'painful throat and blocked nose'], ['sorethroat', 'tonsils are swollen and it hurts to swallow'],
].map(([s, text]) => ({ s, text }))

// ---------- TF-IDF centroid ----------
const STOP = new Set('i me my we you your a an the and or of to in on at for with is are was were be been it its this that have has had do does did not no but so if as by from am im ive get got can could would will please doctor hello hi dear thanks thank since from also very much any some there then than too just'.split(' '))
const toks = (t) => {
  const w = t.toLowerCase().replace(/[^a-z\s]/g, ' ').split(/\s+/).filter((x) => x.length > 1 && !STOP.has(x))
  const out = [...w]
  for (let i = 0; i < w.length - 1; i++) out.push(`${w[i]}_${w[i + 1]}`)
  return out
}
const df = new Map()
for (const d of train) for (const k of new Set(toks(d.text))) df.set(k, (df.get(k) || 0) + 1)
const N = train.length
const vec = (text) => {
  const tf = new Map()
  for (const k of toks(text)) tf.set(k, (tf.get(k) || 0) + 1)
  const v = new Map()
  let n2 = 0
  for (const [k, c] of tf) {
    const d = df.get(k)
    if (!d) continue
    const w = (1 + Math.log(c)) * Math.log((N + 1) / (d + 1))
    v.set(k, w)
    n2 += w * w
  }
  const n = Math.sqrt(n2) || 1
  for (const [k, w] of v) v.set(k, w / n)
  return v
}
const cent = Object.fromEntries(classes.map((c) => [c, new Map()]))
for (const d of train) {
  const c = cent[d.s]
  for (const [k, w] of vec(d.text)) c.set(k, (c.get(k) || 0) + w)
}
for (const c of classes) {
  let n2 = 0
  for (const w of cent[c].values()) n2 += w * w
  const n = Math.sqrt(n2) || 1
  for (const [k, w] of cent[c]) cent[c].set(k, w / n)
}
const dot = (a, b) => { let s = 0; for (const [k, w] of a) { const x = b.get(k); if (x) s += w * x } return s }
const tfidfScores = (text) => { const v = vec(text); return classes.map((c) => dot(v, cent[c])) }

// ---------- embeddings ----------
const extractor = await pipeline('feature-extraction', 'Xenova/all-MiniLM-L6-v2', { dtype: 'q8' })
async function embed(texts) {
  const out = []
  for (let i = 0; i < texts.length; i += 64) {
    const t = await extractor(texts.slice(i, i + 64), { pooling: 'mean', normalize: true })
    const dim = t.dims[1]
    for (let j = 0; j < t.dims[0]; j++) out.push(Float32Array.from(t.data.slice(j * dim, (j + 1) * dim)))
    if (i % 1024 === 0) console.error(`embedded ${Math.min(i + 64, texts.length)}/${texts.length}`)
  }
  return out
}
const trE = await embed(train.map((d) => d.text))
const teE = await embed(test.map((d) => d.text))
const hdE = await embed(HAND.map((d) => d.text))
const D = trE[0].length
const dotf = (a, b) => { let s = 0; for (let i = 0; i < D; i++) s += a[i] * b[i]; return s }
const ec = Object.fromEntries(classes.map((c) => [c, new Float32Array(D)]))
train.forEach((d, i) => { for (let j = 0; j < D; j++) ec[d.s][j] += trE[i][j] })
for (const c of classes) { let n = 0; for (let j = 0; j < D; j++) n += ec[c][j] ** 2; n = Math.sqrt(n) || 1; for (let j = 0; j < D; j++) ec[c][j] /= n }
const centScores = (e) => classes.map((c) => dotf(e, ec[c]))
const knnScores = (e, k = 15) => {
  const sims = trE.map((t, i) => [dotf(e, t), train[i].s]).sort((a, b) => b[0] - a[0]).slice(0, k)
  const sc = Object.fromEntries(classes.map((c) => [c, 0]))
  for (const [s, c] of sims) sc[c] += s
  return classes.map((c) => sc[c])
}

// ---------- evaluate ----------
const rank = (scores) => scores.map((s, i) => [s, classes[i]]).sort((a, b) => b[0] - a[0]).map((x) => x[1])
function evaluate(name, items, fns) {
  const res = {}
  for (const [m, f] of Object.entries(fns)) {
    let t1 = 0, t3 = 0
    items.forEach((it, i) => { const r = rank(f(it, i)); if (r[0] === it.s) t1++; if (r.slice(0, 3).includes(it.s)) t3++ })
    res[m] = { top1: +(t1 / items.length).toFixed(3), top3: +(t3 / items.length).toFixed(3) }
  }
  console.log(`\n${name} (n=${items.length}, ${classes.length} classes, chance top1=${(1 / classes.length).toFixed(3)})`)
  console.table(res)
  return res
}
const z = (a) => a
evaluate('Held-out dialogs: patient text -> complaint', test, {
  tfidf: (it) => tfidfScores(it.text),
  'embed-cent': (_, i) => centScores(teE[i]),
  'embed-knn': (_, i) => knnScores(teE[i]),
  'tfidf+embed': (it, i) => { const a = z(tfidfScores(it.text)), b = centScores(teE[i]); const ma = Math.max(...a) || 1, mb = Math.max(...b) || 1; return a.map((x, j) => x / ma + b[j] / mb) },
})
const hand = evaluate('Hand-written short queries', HAND, {
  tfidf: (it) => tfidfScores(it.text),
  'embed-cent': (_, i) => hdE[i] && centScores(hdE[i]),
  'embed-knn': (_, i) => knnScores(hdE[i]),
  'tfidf+embed': (it, i) => { const a = tfidfScores(it.text), b = centScores(hdE[i]); const ma = Math.max(...a) || 1, mb = Math.max(...b) || 1; return a.map((x, j) => x / ma + b[j] / mb) },
})
console.log('\nMisses on hand-written queries (embed-cent):')
HAND.forEach((it, i) => { const r = rank(centScores(hdE[i])); if (r[0] !== it.s) console.log(`  "${it.text}" expected ${it.s}, got ${r.slice(0, 3).join('/')}`) })
console.log('\nMisses on hand-written queries (tfidf):')
HAND.forEach((it) => { const r = rank(tfidfScores(it.text)); if (r[0] !== it.s) console.log(`  "${it.text}" expected ${it.s}, got ${r.slice(0, 3).join('/')}`) })
