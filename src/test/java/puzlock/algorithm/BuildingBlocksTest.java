package puzlock.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;
import puzlock.model.Direction;
import puzlock.model.GridShape;

class BuildingBlocksTest {

    @Test
    void accessibilityMatchesHandComputedValuesOnALine() {
        GridShape line = new GridShape(3, 1, 1);
        BitSet all = new BitSet();
        all.set(0, 3);
        double[] values = Accessibility.compute(line, all);
        assertEquals(1.224224, values[0], 1e-9);
        assertEquals(2.226444, values[1], 1e-9);
        assertEquals(1.224224, values[2], 1e-9);
    }

    @Test
    void blockedMeansAnObstacleRightNextToItRemovableMeansTheWholeRayIsClear() {
        GridShape shape = new GridShape(3, 1, 1);
        BitSet mover = cells(shape, 0);
        BitSet far = cells(shape, 2);
        assertFalse(Mobility.isBlocked(shape, mover, far, Direction.RIGHT));
        assertFalse(Mobility.isRemovable(shape, mover, far, Direction.RIGHT));
        assertTrue(Mobility.isRemovable(shape, mover, far, Direction.LEFT));
        assertTrue(Mobility.isBlocked(shape, cells(shape, 1), far, Direction.RIGHT));
    }

    @Test
    void shortestPathAvoidsImpassableCellsAndRespectsItsDepthBound() {
        GridShape shape = new GridShape(3, 3, 1);
        BitSet passable = new BitSet();
        passable.set(0, 9);
        passable.clear(shape.index(1, 0, 0));
        passable.clear(shape.index(1, 1, 0));
        BitSet from = cells(shape, shape.index(0, 0, 0));
        int target = shape.index(2, 0, 0);
        Optional<BitSet> path = PathFinder.shortestPath(shape, from, passable, cell -> cell == target, 10);
        assertEquals(6, path.orElseThrow().cardinality());
        assertFalse(path.get().get(shape.index(1, 0, 0)));
        assertTrue(PathFinder.shortestPath(shape, from, passable, cell -> cell == target, 5).isEmpty());
    }

    @Test
    void removableClosureTakesEverythingInFrontAndReconnects() {
        GridShape shape = new GridShape(2, 3, 1);
        BitSet volume = new BitSet();
        volume.set(0, 6);
        BitSet blocker = cells(shape, shape.index(0, 1, 0));
        volume.andNot(blocker);
        ExtractionContext context = ExtractionContext.of(shape, volume, Direction.UP, 10, GenerationOptions.defaults(2),
                new SplittableRandom(1));
        BitSet closed = RemovableClosure.close(context, cells(shape, shape.index(0, 0, 0)), new BitSet()).orElseThrow();
        assertTrue(closed.get(shape.index(0, 2, 0)));
        assertTrue(Mobility.isRemovable(shape, closed, volume, Direction.UP));
        assertTrue(puzlock.model.Connectivity.isConnected(shape, closed));
    }

    @Test
    void keySeedsHaveAFreeTopAndExactlyOneFreeSide() {
        GridShape cube = new GridShape(3, 3, 3);
        BitSet all = new BitSet();
        all.set(0, cube.cellCount());
        List<Integer> seeds = KeyPieceExtractor.seedCandidates(cube, all);
        assertEquals(4, seeds.size());
        for (int seed : seeds) {
            assertEquals(2, cube.y(seed));
        }
    }

    @Test
    void theKeyPieceSlidesOutUpwardOnlyAndLeavesOneConnectedRemainder() {
        GridShape cube = new GridShape(4, 4, 4);
        BitSet all = new BitSet();
        all.set(0, cube.cellCount());
        for (int seed = 0; seed < 20; seed++) {
            ExtractionResult key = KeyPieceExtractor.extract(cube, all, 8, GenerationOptions.defaults(8), new SplittableRandom(seed));
            BitSet piece = key.draft().orElseThrow().cells();
            BitSet rest = (BitSet) all.clone();
            rest.andNot(piece);
            assertTrue(Mobility.isRemovable(cube, piece, rest, Direction.UP));
            for (Direction direction : Direction.values()) {
                if (direction != Direction.UP) {
                    assertTrue(Mobility.isBlocked(cube, piece, rest, direction), "key free to move " + direction);
                }
            }
            assertTrue(puzlock.model.Connectivity.isConnected(cube, rest));
        }
    }

    private static BitSet cells(GridShape shape, int... indices) {
        BitSet set = new BitSet(shape.cellCount());
        for (int index : indices) {
            set.set(index);
        }
        return set;
    }
}
