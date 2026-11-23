package puzlock.algorithm;

import java.util.BitSet;
import java.util.random.RandomGenerator;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/**
 * Everything fixed while one piece is cut out of a volume: the volume itself (Rᵢ, which still
 * contains the piece being built), its accessibility, the piece's move direction and its size.
 * Expansion aims for {@code targetSize}; blocking may overshoot it up to {@code sizeLimit},
 * since Song only asks for "roughly m" voxels and later targets rebalance what is left.
 */
record ExtractionContext(
        GridShape shape,
        BitSet volume,
        double[] accessibility,
        Direction moveDirection,
        int targetSize,
        GenerationOptions options,
        RandomGenerator random) {

    static final double SIZE_LIMIT_FACTOR = 1.5;

    static ExtractionContext of(GridShape shape, BitSet volume, Direction moveDirection, int targetSize,
                                GenerationOptions options, RandomGenerator random) {
        return new ExtractionContext(shape, volume, Accessibility.compute(shape, volume), moveDirection,
                targetSize, options, random);
    }

    ExtractionContext withMoveDirection(Direction direction) {
        return new ExtractionContext(shape, volume, accessibility, direction, targetSize, options, random);
    }

    int sizeLimit() {
        return (int) Math.ceil(targetSize * SIZE_LIMIT_FACTOR);
    }

    /** How many more voxels a piece of these cells may take before hitting the size limit. */
    int roomLeft(BitSet pieceCells) {
        return Math.max(0, sizeLimit() - pieceCells.cardinality());
    }

    double accessibilityOf(BitSet cells) {
        return Accessibility.sum(accessibility, cells);
    }

    /** {@code cell} plus every volume cell behind it, opposite the move direction. */
    BitSet cellAndBehind(int cell) {
        BitSet zone = Mobility.ray(shape, cell, moveDirection.opposite(), volume);
        zone.set(cell);
        return zone;
    }
}
