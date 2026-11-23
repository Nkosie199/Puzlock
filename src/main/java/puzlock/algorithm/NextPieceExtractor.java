package puzlock.algorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/**
 * Song §5.2: extract Pᵢ₊₁ from Rᵢ so that [Pᵢ, Pᵢ₊₁, Rᵢ₊₁] interlocks with Pᵢ as the key, and
 * Pᵢ₊₁ slides out along dᵢ₊₁ once Pᵢ is gone.
 */
final class NextPieceExtractor {

    private NextPieceExtractor() {
    }

    /** A seed voxel next to Pᵢ and the direction dᵢ₊₁ that points from it into Pᵢ. */
    private record Candidate(int seed, Direction direction) {
    }

    private record InitialPiece(Candidate candidate, Draft draft, double cost) {
    }

    static ExtractionResult extract(GridShape shape, BitSet volume, BitSet previousPiece, Direction previousDirection,
                                    int targetSize, GenerationOptions options, RandomGenerator random) {
        List<Candidate> candidates = candidates(shape, volume, previousPiece, previousDirection);
        if (candidates.isEmpty()) {
            return ExtractionResult.failure(FailureReason.NO_NEXT_SEED);
        }
        ExtractionContext base = ExtractionContext.of(shape, volume, previousDirection, targetSize, options, random);
        List<InitialPiece> initialPieces = shortlist(base, candidates).stream()
                .map(candidate -> initialPiece(base.withMoveDirection(candidate.direction()), candidate))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingDouble(InitialPiece::cost))
                .toList();
        FailureReason lastFailure = FailureReason.CANNOT_BLOCK;
        for (InitialPiece initial : initialPieces) {
            ExtractionContext context = base.withMoveDirection(initial.candidate().direction());
            ExtractionResult result = PieceBuilder.build(context, initial.draft(), requirements(previousPiece));
            if (result.succeeded()) {
                return result;
            }
            lastFailure = result.failure();
        }
        return ExtractionResult.failure(lastFailure);
    }

    /** Step 1: voxels of Rᵢ touching Pᵢ across a face perpendicular to dᵢ. */
    private static List<Candidate> candidates(GridShape shape, BitSet volume, BitSet previousPiece, Direction previousDirection) {
        List<Candidate> candidates = new ArrayList<>();
        BitSet[] seen = new BitSet[Direction.values().length];
        for (Direction outward : previousDirection.perpendiculars()) {
            seen[outward.ordinal()] = new BitSet();
            for (int cell = previousPiece.nextSetBit(0); cell >= 0; cell = previousPiece.nextSetBit(cell + 1)) {
                int seed = shape.neighbour(cell, outward);
                if (seed != GridShape.OUTSIDE && volume.get(seed) && !seen[outward.ordinal()].get(seed)) {
                    seen[outward.ordinal()].set(seed);
                    candidates.add(new Candidate(seed, outward.opposite()));
                }
            }
        }
        return candidates;
    }

    /** Ten candidates by equally weighted ranks: low accessibility, and few Rᵢ voxels ahead along dᵢ₊₁. */
    private static List<Candidate> shortlist(ExtractionContext context, List<Candidate> candidates) {
        Map<Candidate, Integer> rank = new HashMap<>();
        addRanks(rank, candidates, Comparator.comparingDouble(candidate -> context.accessibility()[candidate.seed()]));
        addRanks(rank, candidates, Comparator.comparingInt(candidate -> voxelsAhead(context, candidate)));
        return candidates.stream()
                .sorted(Comparator.comparingInt(rank::get))
                .limit(context.options().seedsPerPiece())
                .toList();
    }

    private static void addRanks(Map<Candidate, Integer> rank, List<Candidate> candidates, Comparator<Candidate> order) {
        List<Candidate> sorted = candidates.stream().sorted(order).toList();
        for (int position = 0; position < sorted.size(); position++) {
            rank.merge(sorted.get(position), position, Integer::sum);
        }
    }

    private static int voxelsAhead(ExtractionContext context, Candidate candidate) {
        return Mobility.ray(context.shape(), candidate.seed(), candidate.direction(), context.volume()).cardinality();
    }

    /** Step 2: the seed plus everything needed to make it removable along dᵢ₊₁, priced by accessibility. */
    private static Optional<InitialPiece> initialPiece(ExtractionContext context, Candidate candidate) {
        BitSet start = new BitSet();
        start.set(candidate.seed());
        return RemovableClosure.close(context, start, new BitSet())
                .map(cells -> new InitialPiece(candidate, Draft.of(cells), context.accessibilityOf(cells)));
    }

    /** Step 3: requirements (a) and (b) for local interlocking, in all six directions. */
    private static List<BlockingRequirement> requirements(BitSet previousPiece) {
        List<BlockingRequirement> requirements = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            requirements.add(BlockingRequirement.byRemainderOrPiece(direction, previousPiece));
            requirements.add(BlockingRequirement.togetherWith(direction, previousPiece));
        }
        return requirements;
    }
}
