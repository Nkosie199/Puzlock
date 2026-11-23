# Results

Measured on 2026-11-23 on the development laptop (Windows 10, JDK 23, single-threaded),
`feature/complete-puzlock`. Every puzzle below passed the independent verifier.

## Acceptance tests (`mvn test`)

51 tests, all passing. The acceptance set generates each input with seeds 1–20 and requires a
verified puzzle every time, using the exhaustive subset check:

| Input | Pieces | Result |
|---|---|---|
| 3×3×3 cube | 4 | 20/20 verified |
| 4×4×4 cube (the 2019 test case, Song Fig. 10) | 8 | 20/20 verified |
| 5×5×5 cube | 10 | 20/20 verified |
| 4×4×4 cube padded with 0s, off-centre in a 6×6×7 grid | 8 | 20/20 verified |
| `sphere.stl` at resolution 12 (912 voxels) | 6 | 20/20 verified |
| `bunny.stl` at resolution 16 (815 voxels) | 8 | 20/20 verified |
| two separate blocks | 4 | "Input has 2 separate parts…" |
| 2×2×2 cube | 8 | "…each piece would have under 3 voxels" |
| a text file of prose | 4 | "Unsupported input…" |
| a column one voxel wide | 2 | "Cannot build the key piece…" |

Also run by hand through the CLI and the viewer: the 2019 renderer's 32×32×32 `input-grid.grid`
(1,846 voxels, 10 pieces, 0.6 s) and `agent-hat.stl`, which voxelises into 35 loose bits and is
rejected with that reason.

## Timings

Solid cubes, seeds 1–5, time to the first verified puzzle (includes failed attempts).

| Cube | Pieces K | Voxels per piece | Mean | Range | Attempts used |
|---|---|---|---|---|---|
| 5×5×5 | 10 | 12.5 | 0.24 s | 0.02–0.39 s | 1–2 |
| 8×8×8 | 20 | 25.6 | 0.80 s | 0.14–2.56 s | 1–3 |
| 10×10×10 | 30 | 33.3 | 1.69 s | 0.38–3.63 s | 1–3 |
| 12×12×12 | 50 | 34.6 | 18.8 s | 8.5–40.2 s | 3–10 |
| 15×15×15 | 100 | 33.8 | no puzzle (seed 1) | gave up after 2,414 s | 200 |

## Compared with the paper's Table 1

| Cube | Song et al. 2012 | 2019 implementation | This implementation |
|---|---|---|---|
| 5×5×5, K=10 | 0.7 min | 0.0167 min, key piece only, not valid | 0.24 s, complete and verified |
| 15×15×15, K=100 | 3.03 min | `StackOverflowError` | no puzzle in 200 attempts (40 min); reaches piece 97 of 100 |
| 25×25×25, K=500 | 110.7 min | `StackOverflowError` | not attempted (see below) |

The machines differ, so these are indications, not a fair speed comparison.

For 15×15×15 into 100 pieces the furthest any attempt got was piece 97: the last few pieces
could not be split off without cutting the remainder apart. The run ended with "Could not build piece 97 of
100 in 200 attempts: every candidate split the remaining volume into separate parts". Giving the
last piece more room (up to 8 extra shares instead of 3) made no measurable difference on
12×12×12 into 50 pieces.

## What this shows

- The whole method works: key piece, every later piece, and global recursive interlocking
  confirmed by a checker that does not trust the generator.
- Small and medium puzzles are fast. The hard cases are many small pieces in a large shape:
  success per attempt falls as K grows, which matches Song et al.'s remark that small m makes
  generation harder. The 25×25×25 / 500-piece case was not attempted because the 100-piece
  case already needs many attempts; making large K reliable (smarter look-ahead for the last
  pieces, or Song's bounding-box localisation for speed) is the main open improvement.
