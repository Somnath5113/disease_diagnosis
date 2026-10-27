// Usage: node i18n_build.mjs [lang ...]     (no args = every folder in i18n/)
//        node i18n_build.mjs --pseudo       (self-test: marks every translated atom, then checks no English leaks)
// Reads i18n/en.json (ids -> English) and i18n/<lang>/*.json ({ tmpl?, atoms? } keyed by the same ids),
// and writes app/src/main/assets/triage_tree_<lang>.json. Missing translations stay English.
import { readFileSync, writeFileSync, readdirSync, existsSync, statSync, rmSync } from 'node:fs'
import { join } from 'node:path'
import { localizeTree, TEMPLATES } from './lib/localize.mjs'
import { KB } from './lib/kb.mjs'
import { KB_DEEP } from './lib/kb_deep.mjs'

const here = import.meta.dirname
const assets = join(here, '..', '..', 'app', 'src', 'main', 'assets')
const tree = JSON.parse(readFileSync(join(assets, 'triage_tree.json'), 'utf8'))
const en = JSON.parse(readFileSync(join(here, 'i18n', 'en.json'), 'utf8'))
const idOf = new Map(Object.entries(en.atoms).map(([id, text]) => [text, id]))
const names = new Set([...Object.keys(KB), ...Object.keys(KB_DEEP)])
const placeholders = (s) => [...s.matchAll(/\{(\w+)\}/g)].map((m) => m[1]).sort().join(',')

function load(lang) {
  const dir = join(here, 'i18n', lang)
  const atoms = {}
  const tmpl = {}
  for (const f of readdirSync(dir).filter((f) => f.endsWith('.json')).sort()) {
    const part = JSON.parse(readFileSync(join(dir, f), 'utf8'))
    Object.assign(atoms, part.atoms || {})
    Object.assign(tmpl, part.tmpl || {})
  }
  return { atoms, tmpl }
}

function problems(lang, { atoms, tmpl }) {
  const out = []
  for (const id of Object.keys(atoms)) {
    if (!en.atoms[id]) out.push(`${lang}: unknown atom id ${id}`)
    // Condition names are joined with ", " in saved history and with "; " in advice, so they may not contain either.
    else if (id[0] === 'c' && /, |; /.test(atoms[id])) out.push(`${lang}: condition ${id} may not contain ", " or "; ": ${atoms[id]}`)
  }
  for (const [k, v] of Object.entries(tmpl)) {
    if (!TEMPLATES[k]) out.push(`${lang}: unknown template ${k}`)
    else if (placeholders(v) !== placeholders(TEMPLATES[k])) out.push(`${lang}: template "${k}" must keep placeholders {${placeholders(TEMPLATES[k])}}`)
  }
  return out
}

function build(lang, dict, { pseudo = false } = {}) {
  const { atoms, tmpl } = dict
  const missed = new Set()
  const out = localizeTree(
    tree,
    (_kind, text) => {
      const id = idOf.get(text)
      const t = pseudo ? `§${text}` : id && atoms[id]
      if (!t) missed.add(text)
      return t || undefined
    },
    (k) => (pseudo ? `§${TEMPLATES[k]}` : tmpl[k]),
    names,
  )
  return { out, missed }
}

const args = process.argv.slice(2)
if (args.includes('--pseudo')) {
  const { out, missed } = build('pseudo', { atoms: {}, tmpl: {} }, { pseudo: true })
  // Every atom position must now carry the marker; anything else is a string the parser did not take apart.
  const leaks = []
  const check = (where, s) => {
    if (s && typeof s === 'string' && !s.includes('§')) leaks.push(`${where}: ${s.slice(0, 90)}`)
  }
  for (const c of out.categories) { check('cat.name', c.name); check('cat.desc', c.description); check('cat.group', c.groupName) }
  for (const [id, n] of Object.entries(out.nodes)) {
    check(id + '.question', n.question)
    check(id + '.explanation', n.explanation)
    for (const o of n.options || []) { check(id + '.opt', o.text); check(id + '.sub', o.subtitle) }
    const c = n.conclusion
    if (c) {
      c.likelyConditions.forEach((x) => check(id + '.cond', x))
      check(id + '.advice', c.clinicalAdvice)
      c.homeCareInstructions.forEach((x) => check(id + '.care', x))
      ;(c.indianHomeRemedies || []).forEach((x) => check(id + '.remedy', x))
      check(id + '.seek', c.whenToSeekImmediateHelp)
    }
  }
  const uniq = [...new Set(leaks)]
  console.error(`pseudo build: ${uniq.length} unmarked strings, ${missed.size} lookups missed`)
  uniq.slice(0, 25).forEach((l) => console.error('  LEAK', l))
  process.exit(uniq.length ? 1 : 0)
}

const langs = args.length ? args : readdirSync(join(here, 'i18n')).filter((d) => statSync(join(here, 'i18n', d)).isDirectory())
const total = Object.keys(en.atoms).length
let failed = false
for (const lang of langs) {
  if (!existsSync(join(here, 'i18n', lang))) { console.error(`no i18n/${lang}/ folder`); failed = true; continue }
  const dict = load(lang)
  const errs = problems(lang, dict)
  if (errs.length) { errs.slice(0, 20).forEach((e) => console.error(e)); failed = true; continue }
  const done = Object.keys(dict.atoms).filter((id) => dict.atoms[id]).length
  const target = join(assets, `triage_tree_${lang}.json`)
  if (done === 0) { rmSync(target, { force: true }); console.error(`${lang}: no translations yet, skipped`); continue }
  const { out, missed } = build(lang, dict)
  writeFileSync(target, JSON.stringify(out) + '\n')
  const tcount = Object.keys(dict.tmpl).length
  console.error(`${lang}: ${done}/${total} atoms (${Math.round((done / total) * 100)}%), ${tcount}/${Object.keys(TEMPLATES).length} templates, ${missed.size} texts still English -> triage_tree_${lang}.json (${(JSON.stringify(out).length / 1024).toFixed(0)} KB)`)
}
if (failed) process.exit(1)
