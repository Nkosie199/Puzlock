package puzlock.algorithm;

import java.util.BitSet;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/**
 * Song et al. §5.1 step 2: a₀(x) is the neighbour count of x in the volume and
 * aⱼ(x) = aⱼ₋₁(x) + wʲ Σ aⱼ₋₁(yᵢ) over its neighbours yᵢ. Low values mark voxels that are easy
 * to cut off, so pieces prefer to take them.
 */
final class Accessibility {
    static final double WEIGHT = 0.1;
    static final int PASSES = 3;

    private Accessibility() {
    }

    /** Values indexed by cell; cells outside the volume are 0. */
    static double[] compute(GridShape shape, BitSet volume) {
        double[] previous = new double[shape.cellCount()];
        for (int cell = volume.nextSetBit(0); cell >= 0; cell = volume.nextSetBit(cell + 1)) {
            previous[cell] = neighbourCount(shape, volume, cell);
        }
        for (int pass = 1; pass <= PASSES; pass++) {
            double weight = Math.pow(WEIGHT, pass);
            double[] current = new double[shape.cellCount()];
            for (int cell = volume.nextSetBit(0); cell >= 0; cell = volume.nextSetBit(cell + 1)) {
                current[cell] = previous[cell] + weight * neighbourSum(shape, volume, cell, previous);
            }
            previous = current;
        }
        return previous;
    }

    static double sum(double[] accessibility, BitSet cells) {
        double total = 0;
        for (int cell = cells.nextSetBit(0); cell >= 0; cell = cells.nextSetBit(cell + 1)) {
            total += accessibility[cell];
        }
        return total;
    }

    private static int neighbourCount(GridShape shape, BitSet volume, int cell) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            int next = shape.neighbour(cell, direction);
            if (next != GridShape.OUTSIDE && volume.get(next)) {
                count++;
            }
        }
        return count;
    }

    private static double neighbourSum(GridShape shape, BitSet volume, int cell, double[] values) {
        double sum = 0;
        for (Direction direction : Direction.values()) {
            int next = shape.neighbour(cell, direction);
            if (next != GridShape.OUTSIDE && volume.get(next)) {
                sum += values[next];
            }
        }
        return sum;
    }
}
