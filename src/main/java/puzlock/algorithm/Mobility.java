package puzlock.algorithm;

import java.util.BitSet;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/** Blocking and removal tests for translating a set of voxels along one axis. */
final class Mobility {

    private Mobility() {
    }

    /** True when some voxel of {@code mover} has an {@code obstacles} voxel right next to it along {@code direction}. */
    static boolean isBlocked(GridShape shape, BitSet mover, BitSet obstacles, Direction direction) {
        for (int cell = mover.nextSetBit(0); cell >= 0; cell = mover.nextSetBit(cell + 1)) {
            int next = shape.neighbour(cell, direction);
            if (next != GridShape.OUTSIDE && obstacles.get(next) && !mover.get(next)) {
                return true;
            }
        }
        return false;
    }

    /** True when {@code mover} can slide out of the grid along {@code direction} without touching {@code obstacles}. */
    static boolean isRemovable(GridShape shape, BitSet mover, BitSet obstacles, Direction direction) {
        BitSet others = withoutMover(obstacles, mover);
        for (int cell = mover.nextSetBit(0); cell >= 0; cell = mover.nextSetBit(cell + 1)) {
            if (!ray(shape, cell, direction, others).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** The cells of {@code within} strictly beyond {@code start} along {@code direction}, up to the grid edge. */
    static BitSet ray(GridShape shape, int start, Direction direction, BitSet within) {
        BitSet cells = new BitSet();
        for (int cell = shape.neighbour(start, direction); cell != GridShape.OUTSIDE; cell = shape.neighbour(cell, direction)) {
            if (within.get(cell)) {
                cells.set(cell);
            }
        }
        return cells;
    }

    private static BitSet withoutMover(BitSet obstacles, BitSet mover) {
        BitSet others = (BitSet) obstacles.clone();
        others.andNot(mover);
        return others;
    }
}
