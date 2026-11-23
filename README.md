# Puzlock

Generates **recursive interlocking 3D puzzles** from any voxel grid, STL or OBJ file, following
Song, Fu & Cohen-Or, *Recursive Interlocking Puzzles* (ACM TOG 2012). Every result is checked by
an independent verifier; when a shape can't be made into a puzzle, Puzlock says why.

This completes my 2019 UCT CS honours project (the submitted version is tagged
`honours-2019-submission`).

## Run it

Requires Java 21+ and Maven.

```
mvn package
java -jar target/puzlock.jar serve
```

Open http://localhost:8080, pick a sample (or upload a file), choose the number of pieces and
press Generate. Drag to rotate, scroll to zoom, use Explode and Remove / Put back to take the
puzzle apart, and download any piece as STL or grid.

Command line:

```
java -jar target/puzlock.jar generate samples/cube4.grid --pieces 8
java -jar target/puzlock.jar generate samples/bunny.stl --resolution 16 --pieces 8 --out out/bunny
```

This writes `piece-N.grid` (the 2019 renderer's format), `piece-N.stl` (10 mm voxels, ready to
slice) and `puzzle.json` to the output folder. Options: `--resolution` (mesh voxels along the
longest side, default 16), `--seed`, `--attempts` (default 40).

## Inputs

- `.stl` (binary or ASCII) and `.obj`: closed meshes, voxelised at `--resolution`.
- Grid text: one line per z slice, comma-separated rows, one `0`/`1` per cell (see `samples/`).

Inputs are rejected with a specific message when they are unreadable, not watertight, in several
separate parts, too small for the number of pieces, have no possible key piece, or when no
verified puzzle is found in the allowed attempts.

## Docs

- [docs/design.md](docs/design.md): architecture, algorithm, verifier, how the 2019 limitations were resolved.
- [docs/results.md](docs/results.md): test results and timings compared with Song et al. and the 2019 version.
- `bin/docs/` (not in git): the 2019 paper, proposal, poster and readings.

## Tests

```
mvn test
```

51 tests, including the acceptance set: 3³/4, 4³/8, 5³/10 cubes, a padded off-centre cube, the
sphere and the bunny, each over 20 random seeds with the exhaustive interlocking check.
