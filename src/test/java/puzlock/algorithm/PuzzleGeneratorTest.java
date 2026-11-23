package puzlock.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.stream.LongStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import puzlock.io.GridFormat;
import puzlock.io.InputLoader;
import puzlock.model.GridShape;
import puzlock.model.VoxelGrid;

/** The spec's acceptance table: every listed input yields a verified puzzle for 20 seeds, or the stated failure. */
class PuzzleGeneratorTest {
    private static final int SEEDS = 20;

    @ParameterizedTest(name = "{0}x{0}x{0} cube into {1} pieces")
    @CsvSource({"3, 4", "4, 8", "5, 10"})
    void cubesBecomeVerifiedPuzzlesForEverySeed(int size, int pieces) {
        assertValidForEverySeed(cube(size), pieces);
    }

    @Test
    void paddedOffCentreCubeWorks() {
        StringBuilder text = new StringBuilder();
        for (int z = 0; z < 7; z++) {
            for (int y = 0; y < 6; y++) {
                for (int x = 0; x < 6; x++) {
                    boolean inside = x >= 2 && x < 6 && y < 4 && z >= 1 && z < 5;
                    text.append(inside ? '1' : '0');
                }
                text.append(y < 5 ? "," : "");
            }
            text.append('\n');
        }
        VoxelGrid grid = GridFormat.read(text.toString());
        assertEquals(64, grid.voxelCount());
        assertValidForEverySeed(grid, 8);
    }

    @ParameterizedTest(name = "{0} at resolution {1} into {2} pieces")
    @CsvSource({"samples/sphere.stl, 12, 6", "samples/bunny.stl, 16, 8"})
    void meshesBecomeVerifiedPuzzlesForEverySeed(String file, int resolution, int pieces) throws IOException {
        assertValidForEverySeed(InputLoader.load(Path.of(file), resolution).grid(), pieces);
    }

    @Test
    void separatePartsAreReported() {
        GridShape shape = new GridShape(5, 2, 2);
        BitSet voxels = new BitSet();
        for (int z = 0; z < 2; z++) {
            for (int y = 0; y < 2; y++) {
                voxels.set(shape.index(0, y, z));
                voxels.set(shape.index(1, y, z));
                voxels.set(shape.index(3, y, z));
                voxels.set(shape.index(4, y, z));
            }
        }
        assertFailure(new VoxelGrid(shape, voxels), 4, "Input has 2 separate parts; a puzzle needs one connected shape");
    }

    @Test
    void tooManyPiecesForTheVoxelsAreReported() {
        assertFailure(cube(2), 8, "Cannot make 8 pieces from 8 voxels: each piece would have under 3 voxels");
    }

    @Test
    void fewerThanTwoPiecesAreReported() {
        assertFailure(cube(3), 1, "A puzzle needs at least 2 pieces, got 1");
    }

    @Test
    void shapesWithoutAKeySeedAreReported() {
        GridShape shape = new GridShape(1, 6, 1);
        BitSet column = new BitSet();
        column.set(0, 6);
        assertFailure(new VoxelGrid(shape, column), 2, "Cannot build the key piece: no voxel has a free top face plus "
                + "exactly one free side face, so the key piece has no starting point");
    }

    private static void assertValidForEverySeed(VoxelGrid grid, int pieces) {
        LongStream.rangeClosed(1, SEEDS).forEach(seed -> {
            GenerationOutcome outcome = PuzzleGenerator.generate(grid, GenerationOptions.defaults(pieces).withSeed(seed));
            GenerationOutcome.Success success = assertInstanceOf(GenerationOutcome.Success.class, outcome,
                    () -> "seed " + seed + ": " + outcome);
            assertTrue(success.verification().valid());
            assertTrue(success.verification().exhaustive());
            assertEquals(pieces, success.puzzle().pieceCount());
        });
    }

    private static void assertFailure(VoxelGrid grid, int pieces, String message) {
        GenerationOutcome outcome = PuzzleGenerator.generate(grid, GenerationOptions.defaults(pieces));
        assertEquals(message, assertInstanceOf(GenerationOutcome.Failure.class, outcome).message());
    }

    private static VoxelGrid cube(int size) {
        GridShape shape = new GridShape(size, size, size);
        BitSet all = new BitSet();
        all.set(0, shape.cellCount());
        return new VoxelGrid(shape, all);
    }
}
