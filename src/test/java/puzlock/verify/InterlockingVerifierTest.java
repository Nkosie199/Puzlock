package puzlock.verify;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import puzlock.algorithm.GenerationOptions;
import puzlock.algorithm.GenerationOutcome;
import puzlock.algorithm.PuzzleGenerator;
import puzlock.model.Direction;
import puzlock.model.GridShape;
import puzlock.model.Piece;
import puzlock.model.Puzzle;
import puzlock.model.VoxelGrid;

class InterlockingVerifierTest {

    @Test
    void rejectsAKeyThatCanMoveInSeveralDirections() {
        GridShape shape = new GridShape(2, 2, 1);
        VoxelGrid grid = full(shape);
        Puzzle slabs = new Puzzle(shape, List.of(
                Piece.extracted(row(shape, 1), Direction.UP),
                Piece.remainder(row(shape, 0))));
        VerificationReport report = InterlockingVerifier.verify(grid, slabs);
        assertFalse(report.valid());
        assertTrue(report.problems().get(0).startsWith("The key piece can also move"), report.summary());
    }

    @Test
    void rejectsAPieceThatIsStuckInItsOwnDirection() {
        GridShape shape = new GridShape(1, 2, 1);
        Puzzle stuck = new Puzzle(shape, List.of(
                Piece.extracted(row(shape, 0), Direction.UP),
                Piece.remainder(row(shape, 1))));
        VerificationReport report = InterlockingVerifier.verify(full(shape), stuck);
        assertFalse(report.valid());
        assertTrue(report.problems().get(0).contains("cannot slide out up"), report.summary());
    }

    @Test
    void rejectsPiecesThatDoNotCoverTheInput() {
        GridShape shape = new GridShape(1, 2, 1);
        Puzzle partial = new Puzzle(shape, List.of(
                Piece.extracted(row(shape, 1), Direction.UP),
                Piece.remainder(new BitSet())));
        assertFalse(InterlockingVerifier.verify(full(shape), partial).valid());
    }

    @Test
    void acceptsGeneratedPuzzlesButNotTheirPiecesInAnotherOrder() {
        GridShape shape = new GridShape(4, 4, 4);
        VoxelGrid grid = full(shape);
        GenerationOutcome outcome = PuzzleGenerator.generate(grid, GenerationOptions.defaults(6).withSeed(3));
        Puzzle puzzle = ((GenerationOutcome.Success) outcome).puzzle();
        assertTrue(InterlockingVerifier.verify(grid, puzzle).valid());

        List<Piece> swapped = new ArrayList<>(puzzle.pieces());
        swapped.set(0, puzzle.pieces().get(1));
        swapped.set(1, puzzle.pieces().get(0));
        assertFalse(InterlockingVerifier.verify(grid, new Puzzle(shape, swapped)).valid());

        List<Piece> wrongWay = new ArrayList<>(puzzle.pieces());
        Piece key = puzzle.pieces().get(0);
        wrongWay.set(0, Piece.extracted(key.voxels(), key.removalDirection().orElseThrow().opposite()));
        assertFalse(InterlockingVerifier.verify(grid, new Puzzle(shape, wrongWay)).valid());
    }

    private static VoxelGrid full(GridShape shape) {
        BitSet all = new BitSet();
        all.set(0, shape.cellCount());
        return new VoxelGrid(shape, all);
    }

    private static BitSet row(GridShape shape, int y) {
        BitSet row = new BitSet();
        for (int x = 0; x < shape.sizeX(); x++) {
            row.set(shape.index(x, y, 0));
        }
        return row;
    }
}
