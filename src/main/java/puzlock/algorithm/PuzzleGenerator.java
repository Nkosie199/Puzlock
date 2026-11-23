package puzlock.algorithm;

import java.time.Duration;
import java.util.Optional;
import java.util.SplittableRandom;
import puzlock.model.VoxelGrid;

/**
 * Song et al.'s constructive method: extract the key P1, then P2 … Pₖ₋₁ one by one; the rest
 * is the last piece. Each attempt is a fresh {@link PuzzleSearch}; the first puzzle that passes
 * the verifier wins. When an attempt fails near the end, the next one gives the last piece more
 * room.
 */
public final class PuzzleGenerator {
    /** The most extra room, in piece shares, reserved for the last piece (see {@link PuzzleSearch}); less for few pieces. */
    static final int MAX_REMAINDER_RESERVE = 3;
    /** Failing on one of this many final extractions means the end of the puzzle is too cramped. */
    static final int LATE_PIECES = 3;

    private PuzzleGenerator() {
    }

    public static GenerationOutcome generate(VoxelGrid grid, GenerationOptions options) {
        long start = System.nanoTime();
        Optional<String> problem = InputChecks.problemWith(grid, options.pieceCount());
        if (problem.isPresent()) {
            return new GenerationOutcome.Failure(problem.get(), since(start));
        }
        FailureTally tally = new FailureTally();
        SplittableRandom seeds = new SplittableRandom(options.seed());
        int maxReserve = Math.min(MAX_REMAINDER_RESERVE, (options.pieceCount() - 2) / 2);
        int reserve = 0;
        for (int attempt = 1; attempt <= options.attempts(); attempt++) {
            PuzzleSearch search = new PuzzleSearch(grid, options, seeds.split(), tally, reserve);
            Optional<PuzzleSearch.Found> found = search.run();
            if (found.isPresent()) {
                return new GenerationOutcome.Success(found.get().puzzle(), found.get().verification(), attempt, since(start));
            }
            if (search.furthestPiece() >= options.pieceCount() - LATE_PIECES) {
                reserve = Math.min(reserve + 1, maxReserve);
            }
        }
        return new GenerationOutcome.Failure(tally.summary(options.pieceCount(), options.attempts()), since(start));
    }

    private static Duration since(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}
