# Puzlock

Puzlock turns a 3D shape into a **recursive interlocking puzzle**: a set of pieces that hold
each other in place so that only one piece (the *key*) can move at any time. Remove the key and
exactly one other piece becomes free; remove that and the next one frees up, and so on until the
shape is fully taken apart. Assembly is the same sequence in reverse.

You give Puzlock a shape (a voxel grid, or an STL / OBJ mesh) and the number of pieces you want.
It returns the pieces, ready to view in the browser or 3D print, or a plain message explaining
why that shape can't be made into such a puzzle.

## Background

The method is from Song, Fu & Cohen-Or, *Recursive Interlocking Puzzles* (ACM Transactions on
Graphics 31(6), 2012), included as [Recursive-Interlocking-Puzzles.pdf](Recursive-Interlocking-Puzzles.pdf).
Puzlock started as my 2019 UCT Computer Science honours project, which implemented only part of
the key-piece step (the submitted code is tagged `honours-2019-submission`). This version
implements the whole method and adds a verifier, mesh input, a CLI and a viewer.

## How it works

1. **Read the shape.** Grid files are read as they are. STL and OBJ meshes are *voxelised*: the
   mesh is scaled so its longest side spans `--resolution` cubes (voxels), and each voxel whose
   centre is inside the mesh becomes solid.
2. **Check it can work at all.** The shape must be one connected piece, have enough voxels for
   the number of pieces (at least 3 each), and have somewhere the key piece can start.
3. **Cut out the key piece** (P1). Starting from a voxel on the top edge, Puzlock grows a piece
   that can slide straight up and out, but is blocked in the other five directions.
4. **Cut out the next pieces** (P2, P3, …), one at a time from what is left. Each new piece is
   blocked by the previous one, so it can only move once that piece is gone, and it is chosen so
   that it and the previous piece can't slide out together. Song et al. prove that these local
   rules make the whole puzzle interlock.
5. **Search.** Choices are partly random (pieces prefer exposed, easily cut-off voxels, so the
   leftover stays solid). If a piece can't be built, Puzlock backs up and redoes the previous one,
   and if needed restarts with new random choices (40 attempts by default).
6. **Verify.** An independent checker confirms the finished puzzle: the pieces cover the shape
   exactly, each piece slides out in its turn, the key moves in one direction only, and no other
   group of pieces can move at any stage (checked for every subset of pieces when there are up to
   16 pieces).

The details, diagrams and the differences from the paper are in [docs/design.md](docs/design.md).

## Requirements

- Java 21 or newer (`java -version`)
- Maven 3.9 or newer (`mvn -v`)
- A web browser with WebGL for the viewer (it loads three.js from the jsDelivr CDN, so it needs
  internet access)

## Build

```
mvn package
```

This compiles the code, runs all tests and produces `target/puzlock.jar`. Add `-DskipTests` to
build without running the tests.

## Test

```
mvn test
```

51 tests run in about half a minute. Besides unit tests for every part, they include the
acceptance set: a 3×3×3 cube in 4 pieces, 4×4×4 in 8, 5×5×5 in 10, a padded off-centre cube, the
sphere and the bunny meshes, each generated with 20 different random seeds and each result
checked by the verifier, plus every failure message.

## Run

### In the browser

```
java -jar target/puzlock.jar serve
```

Then open http://localhost:8080. Use `--port 9000` (any free port) if 8080 is taken. The server
only listens on your own machine. Stop it with Ctrl+C.

### From the command line

```
java -jar target/puzlock.jar generate <file> --pieces <K> [options]
```

| Option | Meaning | Default |
|---|---|---|
| `--pieces K` | number of pieces (required) | |
| `--resolution N` | voxels along the longest side of a mesh (ignored for grids) | 16 |
| `--seed S` | random seed; the same seed gives the same puzzle | 1 |
| `--attempts A` | how many fresh attempts before giving up | 40 |
| `--out DIR` | where to write the pieces | `out/<file name>` |

Exit code 0 means a verified puzzle was written, 2 means no puzzle (the reason is printed),
1 means the command was used wrongly.

## End-to-end example

### 1. Build

```
mvn package
```

### 2. Generate a puzzle from the command line

The sample `samples/cube4.grid` is a solid 4×4×4 cube, the same shape the 2019 project tested
and the one shown in Song et al.'s figure 10. Ask for 8 pieces:

```
java -jar target/puzlock.jar generate samples/cube4.grid --pieces 8 --out out/cube4
```

Output:

```
Loaded cube4.grid: 64 voxels in a 4x4x4 grid; making 8 pieces...
  piece 1: 5 voxels, removed up
  piece 2: 8 voxels, removed left
  piece 3: 7 voxels, removed backward
  piece 4: 5 voxels, removed up
  piece 5: 8 voxels, removed forward
  piece 6: 8 voxels, removed left
  piece 7: 4 voxels, removed forward
  piece 8: 19 voxels, last piece
Recursive interlocking verified (exhaustive subset check) in 0.7 s (attempt 6)
Wrote piece-N.grid, piece-N.stl and puzzle.json to ...\out\cube4
```

Read it as the disassembly instructions: lift piece 1 up, then slide piece 2 left, piece 3
backward, and so on. Piece 8 is what remains at the end.

`out/cube4/` now holds, for each piece:

- `piece-N.stl`: the piece as a printable mesh (each voxel is a 10 mm cube);
- `piece-N.grid`: the piece in the grid format below, in the same position as in the input;

plus `puzzle.json` with everything (pieces, directions, verification) for other tools.

### 3. See it in the viewer

```
java -jar target/puzlock.jar serve
```

Open http://localhost:8080. In the left panel:

1. **Sample**: `cube4.grid` is already selected. Or choose your own file under **Or your own file**.
2. **Pieces** `8`, **Resolution** (meshes only) and **Seed**, then **Generate**.
3. The status line turns green with the verifier's verdict, and the cube appears in colour: red is
   the key (piece 1), then blue, yellow, orange, …, and green is the last piece.
4. Drag to rotate, scroll to zoom. Move **Explode** to spread the pieces apart.
5. Press **Remove ▶** repeatedly: each press slides the next piece out along its direction and the
   text tells you which piece can move now. **◀ Put back** reverses it.
6. In the piece list, click a colour square to hide or show that piece; **STL** / **GRID**
   download it.

Try the other samples too: `bunny.stl` (resolution 16, 8 pieces) and `sphere.stl` are meshes;
`input-grid.grid` is a 32×32×32 grid saved by the 2019 renderer.

### 4. See it refuse what it can't do

```
java -jar target/puzlock.jar generate samples/cube4.grid --pieces 40
```

```
No puzzle after 0.0 s: Cannot make 40 pieces from 64 voxels: each piece would have under 3 voxels
```

In the viewer, `agent-hat.stl` shows the same behaviour: the hat is a thin shell that falls apart
into 35 separate bits when voxelised, so it is drawn in grey with the message *Input has 35
separate parts; a puzzle needs one connected shape*. Other messages cover unreadable files,
meshes with holes ("not watertight"), shapes with no possible key piece, and running out of
attempts (which names the piece it got stuck on).

## Input formats

**STL** (binary or ASCII) and **OBJ** (`v` and `f` lines): closed meshes. Raise `--resolution`
for more detail (and more voxels); thin parts need a high enough resolution to stay connected.

**Grid text**: one line per z layer; within a line, rows (y) separated by commas; within a row,
one digit per x position, `0` for empty and `1` for solid. Up is +y (later rows are higher). A
2×2×2 cube with the top-right voxel of the first layer missing:

```
11,10
11,11
```

## Project layout

| Path | Contents |
|---|---|
| `src/main/java/puzlock/model` | grid, directions, pieces, connectivity |
| `src/main/java/puzlock/io` | grid / STL / OBJ readers, voxeliser |
| `src/main/java/puzlock/algorithm` | the puzzle generator (Song et al. §5) |
| `src/main/java/puzlock/verify` | the independent interlocking checker |
| `src/main/java/puzlock/export` | STL, grid and JSON output |
| `src/main/java/puzlock/cli`, `web` | command line and local web server |
| `src/main/resources/web` | the browser viewer |
| `samples/` | example inputs |
| `docs/` | [design](docs/design.md) and [results](docs/results.md) |

## Limitations

- Pieces only translate along the three axes (no rotations), as in the paper.
- Piece sizes are roughly equal, not exact; the last piece is often larger, because it has to
  wrap around the two pieces before it.
- Many small pieces is hard (Song et al. note the same). Puzzles up to about 50 pieces take
  under a second to under a minute; 15×15×15 into 100 pieces currently fails (it gets to piece 97
  and reports that). Timings are in [docs/results.md](docs/results.md).
- The output pieces are blocky; adding a smooth outer surface was out of scope.
