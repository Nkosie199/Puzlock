package puzlock.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import puzlock.algorithm.GenerationOptions;
import puzlock.algorithm.GenerationOutcome;
import puzlock.algorithm.PuzzleGenerator;
import puzlock.io.InputLoader;
import puzlock.io.StlReader;
import puzlock.io.TriangleMesh;
import puzlock.model.GridShape;
import puzlock.model.Piece;
import puzlock.model.VoxelGrid;

class StlWriterTest {
    private static final double TOLERANCE = 1e-4;

    @Test
    void writesOnlyExposedFaces() {
        GridShape shape = new GridShape(2, 1, 1);
        assertEquals(12, mesh(shape, cells(shape, 0), 0).triangleCount());
        assertEquals(20, mesh(shape, cells(shape, 0, 1), 0).triangleCount());
    }

    @Test
    void clearanceMovesEveryFaceInward() {
        GridShape shape = new GridShape(1, 1, 1);
        double[][] bounds = bounds(mesh(shape, cells(shape, 0), 0.15f));
        for (int axis = 0; axis < 3; axis++) {
            assertEquals(0.15, bounds[0][axis], TOLERANCE);
            assertEquals(9.85, bounds[1][axis], TOLERANCE);
        }
    }

    @Test
    void touchingPiecesEndUpTwiceTheClearanceApart() {
        GridShape shape = new GridShape(2, 1, 1);
        double leftEnd = bounds(mesh(shape, cells(shape, 0), 0.2f))[1][0];
        double rightStart = bounds(mesh(shape, cells(shape, 1), 0.2f))[0][0];
        assertEquals(0.4, rightStart - leftEnd, TOLERANCE);
    }

    @Test
    void innerCornersMoveIntoTheMaterial() {
        GridShape shape = new GridShape(2, 2, 1);
        BitSet lShape = cells(shape, shape.index(0, 0, 0), shape.index(1, 0, 0), shape.index(0, 1, 0));
        TriangleMesh mesh = mesh(shape, lShape, 0.5f);
        boolean innerCornerFound = false;
        boolean oldCornerKept = false;
        for (double[] triangle : mesh.triangles()) {
            for (int v = 0; v < 9; v += 3) {
                innerCornerFound |= Math.abs(triangle[v] - 9.5) < TOLERANCE && Math.abs(triangle[v + 1] - 9.5) < TOLERANCE;
                oldCornerKept |= Math.abs(triangle[v] - 10) < TOLERANCE && Math.abs(triangle[v + 1] - 10) < TOLERANCE;
            }
        }
        assertTrue(innerCornerFound, "the inner corner at (10, 10) should move to (9.5, 9.5)");
        assertTrue(!oldCornerKept, "nothing should remain at the unshrunk inner corner");
        assertClosed(mesh);
    }

    @Test
    void everyGeneratedPieceStaysAClosedSurface() {
        GridShape shape = new GridShape(4, 4, 4);
        BitSet all = new BitSet();
        all.set(0, shape.cellCount());
        GenerationOutcome outcome = PuzzleGenerator.generate(new VoxelGrid(shape, all), GenerationOptions.defaults(8));
        for (Piece piece : ((GenerationOutcome.Success) outcome).puzzle().pieces()) {
            assertClosed(mesh(shape, piece.voxels(), StlWriter.DEFAULT_CLEARANCE_MM));
        }
    }

    @Test
    void writtenPiecesVoxeliseBackToTheSameShape() {
        GridShape shape = new GridShape(3, 3, 3);
        BitSet lShape = cells(shape, shape.index(0, 0, 0), shape.index(1, 0, 0), shape.index(2, 0, 0),
                shape.index(2, 1, 0), shape.index(2, 2, 0));
        VoxelGrid back = InputLoader.load("piece.stl", StlWriter.write(shape, lShape), 3).grid();
        assertEquals(new GridShape(3, 3, 1), back.shape());
        assertEquals(5, back.voxelCount());
    }

    @Test
    void rejectsClearancesThatWouldInvertTheVoxels() {
        GridShape shape = new GridShape(1, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> StlWriter.write(shape, cells(shape, 0), -0.1f));
        assertThrows(IllegalArgumentException.class, () -> StlWriter.write(shape, cells(shape, 0), 5f));
    }

    /** In a closed, consistently wound surface every edge a→b is matched by an edge b→a. */
    private static void assertClosed(TriangleMesh mesh) {
        Map<String, Integer> balance = new HashMap<>();
        for (double[] t : mesh.triangles()) {
            for (int corner = 0; corner < 3; corner++) {
                String from = point(t, corner);
                String to = point(t, (corner + 1) % 3);
                balance.merge(from + ">" + to, 1, Integer::sum);
                balance.merge(to + ">" + from, -1, Integer::sum);
            }
        }
        assertTrue(balance.values().stream().allMatch(count -> count == 0), "surface has open edges");
    }

    private static String point(double[] triangle, int corner) {
        return String.format("%.3f,%.3f,%.3f", triangle[corner * 3], triangle[corner * 3 + 1], triangle[corner * 3 + 2]);
    }

    private static double[][] bounds(TriangleMesh mesh) {
        double[][] bounds = {{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE},
                {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE}};
        for (double[] triangle : mesh.triangles()) {
            for (int i = 0; i < 9; i++) {
                bounds[0][i % 3] = Math.min(bounds[0][i % 3], triangle[i]);
                bounds[1][i % 3] = Math.max(bounds[1][i % 3], triangle[i]);
            }
        }
        return bounds;
    }

    private static TriangleMesh mesh(GridShape shape, BitSet voxels, float clearance) {
        return StlReader.read(StlWriter.write(shape, voxels, clearance));
    }

    private static BitSet cells(GridShape shape, int... indices) {
        BitSet set = new BitSet(shape.cellCount());
        for (int index : indices) {
            set.set(index);
        }
        return set;
    }
}
