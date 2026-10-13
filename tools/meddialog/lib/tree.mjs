/**
 * Tiny multi-label decision-tree trainer.
 *
 * Rows: dialogs of ONE chief complaint. Features: binary facets present in the
 * patient's text. Targets: conditions the doctor's reply discussed (multi-label).
 * Split score = sum over candidate conditions of the binary-entropy reduction.
 */

const H = (p) => (p <= 0 || p >= 1 ? 0 : -p * Math.log2(p) - (1 - p) * Math.log2(1 - p))

/**
 * @param {number[]} idx  row indices at this node
 * @param {Uint8Array[]} feats  feats[f][row] in {0,1}
 * @param {Uint8Array[]} labels labels[c][row] in {0,1}
 */
function bestSplit(idx, feats, labels, minLeaf, usedFacets) {
  const n = idx.length
  const C = labels.length
  const base = new Array(C)
  for (let c = 0; c < C; c++) {
    let s = 0
    for (const i of idx) s += labels[c][i]
    base[c] = s
  }
  let best = null
  for (let f = 0; f < feats.length; f++) {
    if (usedFacets.has(f)) continue
    let n1 = 0
    const pos = new Array(C).fill(0)
    for (const i of idx) {
      if (!feats[f][i]) continue
      n1++
      for (let c = 0; c < C; c++) pos[c] += labels[c][i]
    }
    const n0 = n - n1
    if (n1 < minLeaf || n0 < minLeaf) continue
    let gain = 0
    for (let c = 0; c < C; c++) {
      const p = base[c] / n
      const p1 = pos[c] / n1
      const p0 = (base[c] - pos[c]) / n0
      gain += H(p) - (n1 / n) * H(p1) - (n0 / n) * H(p0)
    }
    if (!best || gain > best.gain) best = { f, gain, n1, n0 }
  }
  return best
}

function leafStats(idx, labels) {
  const n = idx.length
  return {
    n,
    probs: labels.map((lab) => {
      let s = 0
      for (const i of idx) s += lab[i]
      return n ? s / n : 0
    }),
  }
}

export function trainTree(idx, feats, labels, opts) {
  const { maxDepth, minLeaf, minGain } = opts
  function grow(rows, depth, used) {
    const stats = leafStats(rows, labels)
    if (depth >= maxDepth) return { leaf: true, ...stats }
    const sp = bestSplit(rows, feats, labels, minLeaf, used)
    if (!sp || sp.gain < minGain) return { leaf: true, ...stats }
    const yes = []
    const no = []
    for (const i of rows) (feats[sp.f][i] ? yes : no).push(i)
    const nextUsed = new Set(used).add(sp.f)
    return {
      leaf: false,
      f: sp.f,
      gain: sp.gain,
      ...stats,
      yesShare: yes.length / rows.length,
      yes: grow(yes, depth + 1, nextUsed),
      no: grow(no, depth + 1, nextUsed),
    }
  }
  return grow(idx, 0, new Set())
}

/** Bernoulli log-likelihood of one row's labels under a node's (smoothed) probabilities. */
function rowLogLik(node, row, labels) {
  let ll = 0
  for (let c = 0; c < labels.length; c++) {
    const p = (node.probs[c] * node.n + 0.5) / (node.n + 1)
    ll += labels[c][row] ? Math.log(p) : Math.log(1 - p)
  }
  return ll
}

/**
 * Reduced-error pruning on a validation slice: an internal node is collapsed into a
 * leaf unless its subtree predicts the validation labels strictly better (log-likelihood).
 * Returns the pruned tree. Mutates nodes in place.
 */
export function pruneTree(node, valRows, feats, labels, minImprovement = 0) {
  if (node.leaf) return node
  const yes = []
  const no = []
  for (const i of valRows) (feats[node.f][i] ? yes : no).push(i)
  node.yes = pruneTree(node.yes, yes, feats, labels, minImprovement)
  node.no = pruneTree(node.no, no, feats, labels, minImprovement)
  let asLeaf = 0
  let asTree = 0
  for (const i of valRows) {
    asLeaf += rowLogLik(node, i, labels)
    asTree += rowLogLik(feats[node.f][i] ? node.yes : node.no, i, labels)
  }
  if (asTree - asLeaf <= minImprovement * valRows.length) {
    return { leaf: true, n: node.n, probs: node.probs }
  }
  node.valGain = (asTree - asLeaf) / Math.max(1, valRows.length)
  return node
}

/** Mean per-row log-likelihood of `rows` under the tree. */
export function meanLogLik(tree, rows, feats, labels) {
  let s = 0
  for (const i of rows) s += rowLogLik(route(tree, i, feats), i, labels)
  return s / Math.max(1, rows.length)
}

export function route(tree, row, feats) {
  let node = tree
  while (!node.leaf) node = feats[node.f][row] ? node.yes : node.no
  return node
}

export function topK(probs, k) {
  return probs
    .map((p, c) => [c, p])
    .sort((a, b) => b[1] - a[1])
    .slice(0, k)
    .map(([c]) => c)
}
