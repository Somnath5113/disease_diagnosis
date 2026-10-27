// Usage: node i18n_extract.mjs
// Lists every translatable piece of text in the shipped English tree and writes i18n/en.json:
//   { tmpl: { key: english sentence pattern }, atoms: { id: english text } }
// IDs are permanent: an existing text keeps its ID, new texts are appended, so translation
// files (i18n/<lang>.json, same ids) stay valid when the generator changes.
import { readFileSync, writeFileSync, mkdirSync, existsSync } from 'node:fs'
import { join } from 'node:path'
import { localizeTree, TEMPLATES } from './lib/localize.mjs'
import { KB } from './lib/kb.mjs'
import { KB_DEEP } from './lib/kb_deep.mjs'

const here = import.meta.dirname
const treePath = join(here, '..', '..', 'app', 'src', 'main', 'assets', 'triage_tree.json')
const tree = JSON.parse(readFileSync(treePath, 'utf8'))
const dir = join(here, 'i18n')
mkdirSync(dir, { recursive: true })
const enPath = join(dir, 'en.json')

const PREFIX = { question: 'q', option: 'o', condition: 'c', advice: 'a', care: 'k', remedy: 'r', seek: 's', explanation: 'e', category: 'g', title: 't', note: 'n' }
const registry = existsSync(enPath) ? JSON.parse(readFileSync(enPath, 'utf8')).atoms : {}
const idOf = new Map(Object.entries(registry).map(([id, text]) => [text, id]))
const counters = {}
for (const id of Object.keys(registry)) {
  const p = id[0]
  counters[p] = Math.max(counters[p] || 0, Number(id.slice(1)))
}

const used = new Set()
const names = new Set([...Object.keys(KB), ...Object.keys(KB_DEEP)])
localizeTree(
  tree,
  (kind, text) => {
    used.add(text)
    if (!idOf.has(text)) {
      const p = PREFIX[kind]
      if (!p) throw new Error(`unknown kind ${kind}`)
      counters[p] = (counters[p] || 0) + 1
      const id = `${p}${String(counters[p]).padStart(4, '0')}`
      idOf.set(text, id)
      registry[id] = text
    }
    return undefined
  },
  () => undefined,
  names,
)

const sorted = Object.fromEntries(Object.entries(registry).sort(([a], [b]) => a.localeCompare(b)))
writeFileSync(enPath, JSON.stringify({ tmpl: TEMPLATES, atoms: sorted }, null, 1) + '\n')
const unused = Object.values(registry).filter((t) => !used.has(t)).length
const byKind = {}
for (const id of Object.keys(sorted)) byKind[id[0]] = (byKind[id[0]] || 0) + 1
console.error(`i18n/en.json: ${Object.keys(sorted).length} atoms (${unused} no longer used) + ${Object.keys(TEMPLATES).length} templates`)
console.error(JSON.stringify(byKind))
