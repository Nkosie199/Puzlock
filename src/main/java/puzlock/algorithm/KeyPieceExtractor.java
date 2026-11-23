package puzlock.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/** Song §5.1: extract the key piece P1, which slides out upward and is blocked every other way. */
final class KeyPieceExtractor {
    private static final List<Direction> SIDES =
            List.of(Direction.LEFT, Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD);

    private KeyPieceExtractor() {
    }

    /**
     * Step 1 candidates: voxels whose only exterior faces are the top and one side, with nothing
     * anywhere above them or beyond that side, so they could leave in a single translation.
     */
    static List<Integer> seedCandidates(GridShape shape, BitSet volume) {
        List<Integer> seeds = new ArrayList<>();
        for (int cell = volume.nextSetBit(0); cell >= 0; cell = volume.nextSetBit(cell + 1)) {
            if (isSeed(shape, volume, cell)) {
                seeds.add(cell);
            }
        }
        return seeds;
    }

    static ExtractionResult extract(GridShape shape, BitSet volume, int targetSize, GenerationOptions options,
                                    RandomGenerator random) {
        List<Integer> seeds = new ArrayList<>(seedCandidates(shape, volume));
        if (seeds.isEmpty()) {
            return ExtractionResult.failure(FailureReason.NO_KEY_SEED);
        }
        Collections.shuffle(seeds, Random.from(random));
        ExtractionContext context = ExtractionContext.of(shape, volume, Direction.UP, targetSize, options, random);
        List<BlockingRequirement> requirements = Arrays.stream(Direction.values())
                .filter(direction -> direction != Direction.UP)
                .map(BlockingRequirement::byRemainder)
                .toList();
        FailureReason lastFailure = FailureReason.CANNOT_BLOCK;
        for (int seed : seeds.subList(0, Math.min(seeds.size(), options.seedsPerPiece()))) {
            BitSet start = new BitSet();
            start.set(seed);
            ExtractionResult result = PieceBuilder.build(context, Draft.of(start), requirements);
            if (result.succeeded()) {
                return result;
            }
            lastFailure = result.failure();
        }
        return ExtractionResult.failure(lastFailure);
    }

    private static boolean isSeed(GridShape shape, BitSet volume, int cell) {
        if (!Mobility.ray(shape, cell, Direction.UP, volume).isEmpty() || !hasNeighbour(shape, volume, cell, Direction.DOWN)) {
            return false;
        }
        List<Direction> openSides = SIDES.stream()
                .filter(side -> !hasNeighbour(shape, volume, cell, side))
                .toList();
        return openSides.size() == 1 && Mobility.ray(shape, cell, openSides.get(0), volume).isEmpty();
    }

    private static boolean hasNeighbour(GridShape shape, BitSet volume, int cell, Direction direction) {
        int next = shape.neighbour(cell, direction);
        return next != GridShape.OUTSIDE && volume.get(next);
    }
}
