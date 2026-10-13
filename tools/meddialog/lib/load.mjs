import { asyncBufferFromFile, parquetReadObjects } from 'hyparquet'
import { compressors } from 'hyparquet-compressors'
import { readdirSync } from 'node:fs'
import { join } from 'node:path'

/**
 * Loads every MedDialog parquet file in `dir` (lighteval/med_dialog layout:
 * `<subset>_<split>-*.parquet`, columns: id, src, tgt) into
 * { id, subset, split, title, patient, doctor } records.
 *
 * `src` is "Patient: ... Doctor: ..."; `tgt` is a short title of the chief concern.
 */
export async function loadDialogs(dir) {
  const files = readdirSync(dir).filter((f) => f.endsWith('.parquet')).sort()
  const out = []
  for (const f of files) {
    const [subset, rest] = f.split('_')
    const split = rest.split('-')[0]
    const file = await asyncBufferFromFile(join(dir, f))
    const rows = await parquetReadObjects({ file, compressors })
    for (const r of rows) {
      const src = String(r.src ?? '')
      const cut = src.search(/\bDoctor:/)
      if (cut < 0) continue
      const patient = src.slice(0, cut).replace(/^\s*Patient:\s*/i, '').trim()
      const doctor = src.slice(cut).replace(/^\s*Doctor:\s*/i, '').trim()
      if (!patient || !doctor) continue
      out.push({
        id: `${subset}:${r.id}`,
        subset,
        split,
        title: String(r.tgt ?? ''),
        patient: patient.toLowerCase(),
        doctor: doctor.toLowerCase(),
        titleLc: String(r.tgt ?? '').toLowerCase(),
      })
    }
    console.error(`loaded ${f}: ${rows.length} rows (total kept ${out.length})`)
  }
  return out
}

/** Deterministic 0..1 hash so train/validation splits are stable across runs. */
export function hash01(s) {
  let h = 2166136261
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return ((h >>> 0) % 100000) / 100000
}
