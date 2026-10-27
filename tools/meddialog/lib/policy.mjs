/**
 * Compiles a stage-2 question policy into static tree nodes.
 *
 * Given the data-derived starting scores (from a stage-1 MedDialog leaf), it repeatedly
 * picks the unanswered question that best separates the currently leading conditions,
 * branches on Yes / "No or not sure", and stops when one condition clearly leads,
 * no question discriminates, or the depth budget is spent. Safety (escalation)
 * questions are asked last as a short chain: a Yes ends the chain with a raised urgency.
 *
 * The result is ordinary nodes (question + two options, or a conclusion); the app's
 * engine does not change and no scoring happens on the device.
 */
import { URGENCY_RANK, maxUrgency } from './safety.mjs'

const TEMP = 0.5 // softmax temperature turning points into rough weights
const MIN_DISC = 0.15 // below this a question does not separate the leaders enough to ask
const MARGIN = 4 // stop once the leader is this many points ahead of the runner-up
const SHOW_WITHIN = 2 // conditions this close to the leader are shown
const SERIOUS_WITHIN = 3 // serious conditions this close are flagged in text and urgency
const SHOW_MAX = 3
const MAX_UNCOVERED = 1 // minor conditions that may stay unscreened when one condition clearly leads
const EVIDENCE = 2 // a condition is only named once it has at least one point of evidence above the baseline
const BASE = 1 // every plausible condition starts at least here, so rare causes still get screened
const EXPLORE = 0.35 // share of picker weight spread evenly over all candidates

// maxDeep = 6 follow-up questions: emergency screen (1) + MedDialog questions (<=3) + follow-up (<=6) = 10 layers.
export function createPolicy({ sym, deep, KB, REMEDIES = {}, remediesFallback = [], floor, fallback, addNode, maxDeep = 6 }) {
  const KEYS = Object.keys(deep.C)
  const NAMES = KEYS.map((k) => deep.C[k])
  const idx = Object.fromEntries(KEYS.map((k, i) => [k, i]))

  for (const [k, name] of Object.entries(deep.C)) if (!KB[name]) throw new Error(`${sym}: no advice for "${name}" (${k})`)
  for (const q of deep.Q) {
    for (const k of [...Object.keys(q.yes), ...Object.keys(q.no)]) if (!(k in idx)) throw new Error(`${sym}.${q.id}: unknown key ${k}`)
    if (q.esc && !(q.esc in URGENCY_RANK)) throw new Error(`${sym}.${q.id}: bad esc`)
  }
  // Anything that scores competes in the normal pool (including escalation questions that also
  // discriminate); every escalation question also stays in the safety chain below.
  const disc = deep.Q.filter((q) => Object.keys(q.yes).length || Object.keys(q.no).length)
  // Most urgent first so a Yes on an URGENT item never hides behind a ROUTINE one.
  const chain = deep.Q.filter((q) => q.esc).sort((a, b) => URGENCY_RANK[b.esc] - URGENCY_RANK[a.esc])
  const byId = Object.fromEntries(deep.Q.map((q) => [q.id, q]))

  const memo = new Map()
  const leafMemo = new Map()
  let nq = 0
  let nl = 0

  const half = (x) => Math.round(x * 2) / 2

  function applyAnswer(state, q, yes) {
    const pts = yes ? q.yes : q.no
    const s = state.s.slice()
    for (const [k, v] of Object.entries(pts)) s[idx[k]] = half(s[idx[k]] + v)
    let urg = state.urg
    const notes = state.notes.slice()
    if (yes && q.esc) {
      urg = maxUrgency(urg, q.esc)
      if (q.note && !notes.includes(q.note)) notes.push(q.note)
    }
    return { s, asked: [...state.asked, q.id], urg, notes, depth: state.depth + 1 }
  }

  /** Seed scores from the stage-1 leaf's mention rates (the data-derived prior). */
  function initialState(leafProbs, path) {
    const s = new Array(KEYS.length).fill(BASE)
    const pmax = Math.max(0.0001, ...Object.values(leafProbs))
    for (const [name, p] of Object.entries(leafProbs)) {
      if (p < 0.05) continue
      const targets = name in (deep.alias || {}) ? deep.alias[name] : KEYS.filter((k) => deep.C[k] === name)
      for (const t of targets) s[idx[t]] = Math.max(s[idx[t]], half((3 * p) / pmax))
    }
    let state = { s, asked: [], urg: floor, notes: [], depth: 0 }
    // Carry over answers already given to stage-1 questions that this bank duplicates.
    for (const step of path) {
      for (const q of deep.Q) {
        if (q.f && q.f === step.facet && !state.asked.includes(q.id)) {
          const next = applyAnswer(state, q, step.yes)
          state = { ...next, depth: state.depth } // carried answers do not use depth budget
        }
      }
    }
    return state
  }

  function weights(s) {
    const m = Math.max(...s)
    const e = s.map((x) => Math.exp(TEMP * (x - m)))
    const z = e.reduce((a, b) => a + b, 0)
    return e.map((x) => x / z)
  }

  function pick(state) {
    // Mix in a uniform share so low-prior conditions still get their screening question.
    const p = weights(state.s).map((x) => (1 - EXPLORE) * x + EXPLORE / KEYS.length)
    let best = null
    let bestD = MIN_DISC
    for (const q of disc) {
      if (state.asked.includes(q.id)) continue
      let d = 0
      for (let i = 0; i < KEYS.length; i++) {
        for (let j = i + 1; j < KEYS.length; j++) {
          const yi = q.yes[KEYS[i]] || 0, yj = q.yes[KEYS[j]] || 0
          const ni = q.no[KEYS[i]] || 0, nj = q.no[KEYS[j]] || 0
          d += p[i] * p[j] * (Math.abs(yi - yj) + Math.abs(ni - nj))
        }
      }
      if (d > bestD) { best = q; bestD = d }
    }
    return best
  }

  const seriousIdx = KEYS.map((k, i) => (KB[NAMES[i]].serious ? i : -1)).filter((i) => i >= 0)

  /**
   * Safety: a plausible serious condition must not be dropped just because a common one leads.
   * While one is still unresolved (not already leading, not yet argued against), ask the
   * unasked question that most strongly screens for it.
   */
  /**
   * Coverage: every plausible condition should have had one strong screening question
   * (a question worth >= +3 for it) before we conclude. Returns the uncovered conditions
   * with their best unasked screening question, serious ones first, then by current score.
   */
  function uncovered(state) {
    const top = Math.max(...state.s)
    const out = []
    for (let i = 0; i < KEYS.length; i++) {
      if (state.s[i] <= -2 || state.s[i] >= top) continue // ruled down, or already leading
      const k = KEYS[i]
      const screened = deep.Q.some((q) => state.asked.includes(q.id) && (q.yes[k] || 0) >= 3)
      if (screened) continue
      let best = null
      for (const q of disc) {
        if (state.asked.includes(q.id)) continue
        const pts = q.yes[k] || 0
        if (pts >= 3 && (!best || pts > (best.yes[k] || 0))) best = q
      }
      if (best) out.push({ i, q: best, serious: seriousIdx.includes(i) })
    }
    return out.sort((a, b) => Number(b.serious) - Number(a.serious) || state.s[b.i] - state.s[a.i])
  }

  const top2 = (s) => {
    const sorted = s.slice().sort((a, b) => b - a)
    return sorted[0] - (sorted[1] ?? -Infinity)
  }

  function leaf(state) {
    const ranked = KEYS.map((k, i) => ({ k, name: NAMES[i], score: state.s[i], i })).sort((a, b) => b.score - a.score || a.i - b.i)
    const top = ranked[0]
    let urgency = maxUrgency(floor, state.urg)
    const notes = state.notes
    let title, likely, lead
    const extras = []
    if (top.score < EVIDENCE) {
      title = 'No single cause stands out'
      likely = ['Needs a doctor’s assessment']
    } else {
      const shown = ranked.filter((r) => r.score >= EVIDENCE && r.score >= top.score - SHOW_WITHIN).slice(0, SHOW_MAX)
      for (const r of ranked) {
        if (KB[r.name].serious && r.score >= EVIDENCE && r.score >= top.score - SERIOUS_WITHIN && !shown.includes(r)) extras.push(r)
      }
      const all = [...shown, ...extras]
      for (const r of all) urgency = maxUrgency(urgency, KB[r.name].urgency)
      lead = all.slice().sort((a, b) => URGENCY_RANK[KB[b.name].urgency] - URGENCY_RANK[KB[a.name].urgency] || b.score - a.score)[0]
      title = lead.name
      // Canonical order (lead first, rest in bank order) so permutations merge into one assessment.
      const byBank = (a, b) => a.i - b.i
      const rest = (list) => list.filter((r) => r !== lead).sort(byBank).map((r) => r.name)
      likely = [lead.name, ...rest(all)]
      lead.shown = (shown.includes(lead) ? [lead.name] : []).concat(rest(shown))
      lead.extras = (extras.includes(lead) ? [] : rest(extras))
    }

    let advice, care, seek
    if (!lead) {
      advice = fallback.advice
      care = fallback.care
      seek = fallback.seek
    } else {
      const kb = KB[lead.name]
      const parts = [kb.advice]
      parts.push(`Your answers fit best with: ${lead.shown.join('; ')}.` + (lead.extras.length ? ` Also worth ruling out: ${lead.extras.join('; ')}.` : ''))
      advice = parts.join('\n\n')
      care = kb.care
      seek = kb.seek
      if (floor === 'URGENT_CARE' && URGENCY_RANK[kb.urgency] < URGENCY_RANK.URGENT_CARE) seek = `${seek} ${fallback.seek}`
    }
    if (notes.length) advice = `${advice}\n\nImportant: ${notes.join(' ')}`

    // Home remedies are comfort measures: never shown next to an urgent or emergency assessment.
    const remedies = (lead ? REMEDIES[lead.name] : remediesFallback) || []
    const conclusion = {
      urgencyLevel: urgency,
      likelyConditions: likely,
      clinicalAdvice: advice,
      homeCareInstructions: care,
      ...(remedies.length && URGENCY_RANK[urgency] <= URGENCY_RANK.ROUTINE_DOCTOR ? { indianHomeRemedies: remedies } : {}),
      whenToSeekImmediateHelp: seek,
    }
    const key = JSON.stringify([title, conclusion])
    if (leafMemo.has(key)) return leafMemo.get(key)
    const id = `${sym}_dx${nl++}`
    addNode({
      id,
      category: sym.toUpperCase(),
      question: `Assessment: ${title}`,
      isConclusion: true,
      conclusion,
    })
    leafMemo.set(key, id)
    return id
  }

  function questionNode(state, q, intro) {
    const yes = applyAnswer(state, q, true)
    const no = applyAnswer(state, q, false)
    const yesId = visit(yes, null)
    const noId = visit(no, null)
    const sep = []
    const sup = Object.entries(q.yes).filter(([, v]) => v > 0).sort((a, b) => b[1] - a[1]).slice(0, 2).map(([k]) => deep.C[k])
    const arg = Object.entries(q.yes).filter(([, v]) => v < 0).sort((a, b) => a[1] - b[1]).slice(0, 2).map(([k]) => deep.C[k])
    if (sup.length) sep.push(arg.length ? `Helps tell ${sup.join(' or ')} apart from ${arg.join(' or ')}.` : `A Yes points toward ${sup.join(' or ')}.`)
    else if (q.esc) sep.push('A safety check: some answers here need prompt medical attention.')
    const why = `Why we ask: ${sep.join(' ')}`
    const id = `${sym}_d${nq++}`
    addNode({
      id,
      category: sym.toUpperCase(),
      question: q.q,
      explanation: intro ? `${intro}\n\n${why}` : why,
      options: [
        { text: 'Yes', nextId: yesId },
        { text: 'No or not sure', nextId: noId },
      ],
    })
    return id
  }

  function visit(state, intro) {
    const key = JSON.stringify([state.s, state.asked.slice().sort(), state.urg, state.notes, state.depth, intro || ''])
    if (memo.has(key)) return memo.get(key)
    const remainingChain = chain.filter((q) => !state.asked.includes(q.id))
    // A raised urgency from an earlier Yes ends the safety chain: it cannot rise further
    // except via a higher-level item, and those are asked first.
    const chainActive = remainingChain.filter((q) => URGENCY_RANK[q.esc] > URGENCY_RANK[state.urg])
    const discBudget = Math.max(3, maxDeep - chainActive.length)
    let id
    if (state.depth < discBudget) {
      const unc = uncovered(state)
      const first = unc[0]?.q || null
      // Stop on a clear leader only once no serious condition and at most MAX_UNCOVERED minor ones
      // remain unscreened. When the budget is nearly spent, uncovered conditions get the last slots.
      const mustScreen = unc.some((u) => u.serious) || unc.length > MAX_UNCOVERED
      const reserve = first && discBudget - state.depth <= Math.min(unc.length, 3)
      const q = reserve ? first : (top2(state.s) < MARGIN ? pick(state) : null) || (mustScreen ? first : null)
      if (q) id = questionNode(state, q, intro)
    }
    if (!id) {
      const next = chainActive.find((q) => state.depth < maxDeep)
      id = next ? questionNode(state, next, intro) : leaf(state)
    }
    memo.set(key, id)
    return id
  }

  return {
    /** Enter stage 2 from a stage-1 leaf. `path` = [{facet, yes}], `intro` = text for the first question. */
    enter(leafProbs, path, intro) {
      return visit(initialState(leafProbs, path), intro)
    },
    byId,
    stats: () => ({ questions: nq, leaves: nl }),
  }
}
