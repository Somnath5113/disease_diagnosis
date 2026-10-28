/**
 * Rebuilds the (English) triage tree in another language.
 *
 * Every string in the tree is either a plain "atom" (a question, an option, a condition
 * name, one piece of advice...) or a sentence assembled from atoms and numbers by the
 * generator (emit.mjs / policy.mjs). This module takes the English tree apart again, looks
 * each atom and template up in a translation, and puts the sentence back together.
 *
 *   atoms     english text -> translated text (one entry per reusable piece)
 *   templates sentence patterns with {placeholders}, see TEMPLATES below
 *
 * `translate(kind, english)` returns the translation, or undefined when there is none.
 * Anything without a translation stays English, so a partial translation is still usable.
 */
import { DEEP } from './deep.mjs'
import { KB, FALLBACK } from './kb.mjs'
import { KB_DEEP } from './kb_deep.mjs'
import { EMERGENCY_KINDS } from './safety.mjs'

/** English sentence patterns. Placeholders in braces; the translated pattern must keep them. */
export const TEMPLATES = {
  or: ' or ',
  why: 'Why we ask: {text}',
  why_stage1: 'this was among the strongest clues separating the conditions doctors discussed in {n} online doctor consultations about “{name}”. {p}% of patients mentioned it.',
  why_stage1_asked: ' Doctors asked about it in {a}% of these consultations.',
  why_tell_apart: 'Helps tell {sup} apart from {arg}.',
  why_points_toward: 'A Yes points toward {sup}.',
  why_safety: 'A safety check: some answers here need prompt medical attention.',
  intro_stats: 'So far: In {n} similar online doctor consultations, doctors most often discussed: {list}. These are mention rates, not diagnoses. The next questions follow standard clinical history-taking for “{name}” to narrow this down.',
  intro_plain: 'So far: The next questions follow standard clinical history-taking for “{name}” to narrow this down.',
  fits_best: 'Your answers fit best with: {list}.',
  also_rule_out: ' Also worth ruling out: {list}.',
  important: 'Important: {notes}',
  emergency: 'Emergency: {title}',
  urgent: 'Urgent: {title}',
  assessment: 'Assessment: {title}',
}

const esc = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
/** English template -> RegExp with a capture group per placeholder (named). */
const toRegex = (tpl) =>
  new RegExp('^' + esc(tpl).replace(/\\\{(\w+)\\\}/g, (_, k) => `(?<${k}>[\\s\\S]*?)`) + '$')
const RX = Object.fromEntries(Object.entries(TEMPLATES).map(([k, v]) => [k, toRegex(v)]))
const fill = (tpl, vars) => tpl.replace(/\{(\w+)\}/g, (_, k) => vars[k] ?? '')

/** Split `s` on `sep` into pieces that all belong to `known` (longest match first); null if impossible. */
function splitKnown(s, sep, known) {
  const names = [...known].sort((a, b) => b.length - a.length)
  const go = (rest) => {
    if (rest === '') return []
    for (const n of names) {
      if (rest === n) return [n]
      if (rest.startsWith(n + sep)) {
        const tail = go(rest.slice(n.length + sep.length))
        if (tail) return [n, ...tail]
      }
    }
    return null
  }
  return go(s)
}

// Every single "seek help" sentence the generator can produce (two may be joined with a space).
const SEEK = new Set([
  ...Object.values({ ...KB, ...KB_DEEP }).map((k) => k.seek),
  ...Object.values(FALLBACK).map((k) => k.seek),
  ...Object.values(EMERGENCY_KINDS).map((k) => k.seek),
])

const NOTES = new Set()
for (const d of Object.values(DEEP)) for (const q of d.Q) if (q.note) NOTES.add(q.note)

/**
 * @param tree      the English tree ({ categories, nodes })
 * @param tr        (kind, english) => translated | undefined
 * @param tmpl      (key) => translated template (falls back to English)
 * @param names     set of all condition names (needed to take "A or B" lists apart)
 */
export function localizeTree(tree, tr, tmpl, names) {
  const T = (k) => tmpl(k) ?? TEMPLATES[k]
  const A = (kind, s) => (s == null || s === '' ? s : tr(kind, s) ?? s)
  const orWord = T('or')
  const list = (kind, items) => items.map((x) => A(kind, x))

  /** "A or B" -> translated, joined with the language's own "or". */
  const orList = (s) => {
    const parts = splitKnown(s, TEMPLATES.or, names)
    return parts ? list('condition', parts).join(orWord) : A('condition', s)
  }

  function explanation(e) {
    if (!e) return e
    // Fixed sentences (emergency screens) are plain atoms; the rest are assembled from templates.
    if (!e.startsWith('Why we ask:') && !e.startsWith('So far:')) return A('explanation', e)
    const intro = e.indexOf('\n\n') >= 0 ? e.slice(0, e.indexOf('\n\n')) : null
    if (intro !== null && intro.startsWith('So far:')) {
      return `${introText(intro)}\n\n${explanation(e.slice(intro.length + 2))}`
    }
    if (e.startsWith('So far:')) return introText(e)
    let m = e.match(RX.why)
    if (!m) return e
    const text = m.groups.text
    // Stage-1 question: "this was among the strongest clues ... mentioned it. [Doctors asked ...]"
    const asked = text.match(/^(?<main>.*? of patients mentioned it\.)(?<asked> Doctors asked about it in [\d.]+% of these consultations\.)?$/s)
    if (asked) {
      const base = asked.groups.main.match(toRegex(TEMPLATES.why_stage1))
      if (base) {
        let out = fill(T('why_stage1'), { n: base.groups.n, name: A('category', base.groups.name), p: base.groups.p })
        if (asked.groups.asked) out += fill(T('why_stage1_asked'), { a: asked.groups.asked.match(/([\d.]+)%/)[1] })
        return fill(T('why'), { text: out })
      }
    }
    // Stage-2 question
    let body
    if ((m = text.match(RX.why_tell_apart))) body = fill(T('why_tell_apart'), { sup: orList(m.groups.sup), arg: orList(m.groups.arg) })
    else if ((m = text.match(RX.why_points_toward))) body = fill(T('why_points_toward'), { sup: orList(m.groups.sup) })
    else if (text === TEMPLATES.why_safety) body = T('why_safety')
    else return e
    return fill(T('why'), { text: body })
  }

  function introText(s) {
    let m = s.match(RX.intro_plain)
    if (m) return fill(T('intro_plain'), { name: A('category', m.groups.name) })
    m = s.match(RX.intro_stats)
    if (!m) return s
    const items = m.groups.list.split(', ').map((it) => {
      const x = it.match(/^(.*) \((\d+)%\)$/)
      return x ? `${A('condition', x[1])} (${x[2]}%)` : it
    })
    return fill(T('intro_stats'), { n: m.groups.n, list: items.join(', '), name: A('category', m.groups.name) })
  }

  function advice(s) {
    const parts = s.split('\n\n')
    const out = [A('advice', parts[0])]
    for (const part of parts.slice(1)) {
      const fits = part.match(/^Your answers fit best with: (?<l>.*?)\.(?<x> Also worth ruling out: (?<e>.*)\.)?$/s)
      if (fits) {
        let t = fill(T('fits_best'), { list: list('condition', fits.groups.l.split('; ')).join('; ') })
        if (fits.groups.x) t += fill(T('also_rule_out'), { list: list('condition', fits.groups.e.split('; ')).join('; ') })
        out.push(t)
        continue
      }
      const imp = part.match(/^Important: (?<n>.*)$/s)
      if (imp) {
        const notes = splitKnown(imp.groups.n, ' ', NOTES)
        out.push(fill(T('important'), { notes: (notes ? list('note', notes) : [imp.groups.n]).join(' ') }))
        continue
      }
      out.push(part)
    }
    return out.join('\n\n')
  }

  /** `seek` is either one atom, or two atoms joined by a space (kb.seek + fallback.seek). */
  function seek(s) {
    if (SEEK.has(s)) return A('seek', s)
    for (let i = s.indexOf(' '); i >= 0; i = s.indexOf(' ', i + 1)) {
      if (SEEK.has(s.slice(0, i)) && SEEK.has(s.slice(i + 1))) return `${A('seek', s.slice(0, i))} ${A('seek', s.slice(i + 1))}`
    }
    return s
  }

  function question(n) {
    const q = n.question
    let m
    if ((m = q.match(RX.emergency))) return fill(T('emergency'), { title: A('title', m.groups.title) })
    if ((m = q.match(RX.urgent))) return fill(T('urgent'), { title: A('title', m.groups.title) })
    if ((m = q.match(RX.assessment))) return fill(T('assessment'), { title: A('condition', m.groups.title) })
    return A('question', q)
  }

  const out = { ...tree }
  out.categories = tree.categories.map((c) => ({
    ...c,
    name: A('category', c.name),
    description: A('category', c.description),
    groupName: A('category', c.groupName),
  }))
  out.nodes = {}
  for (const [id, n] of Object.entries(tree.nodes)) {
    const o = { ...n, question: question(n) }
    if (n.explanation) o.explanation = explanation(n.explanation)
    if (n.options) {
      o.options = n.options.map((p) => ({
        ...p,
        text: A('option', p.text),
        ...(p.subtitle ? { subtitle: A('option', p.subtitle) } : {}),
      }))
    }
    if (n.conclusion) {
      const c = n.conclusion
      o.conclusion = {
        ...c,
        likelyConditions: list('condition', c.likelyConditions),
        clinicalAdvice: advice(c.clinicalAdvice),
        homeCareInstructions: list('care', c.homeCareInstructions),
        ...(c.indianHomeRemedies ? { indianHomeRemedies: list('remedy', c.indianHomeRemedies) } : {}),
        whenToSeekImmediateHelp: seek(c.whenToSeekImmediateHelp),
      }
    }
    out.nodes[id] = o
  }
  return out
}
