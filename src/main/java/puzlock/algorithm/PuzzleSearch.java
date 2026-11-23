package puzlock.algorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import puzlock.model.Direction;
import puzlock.model.Piece;
import puzlock.model.Puzzle;
import puzlock.model.VoxelGrid;
import puzlock.verify.InterlockingVerifier;
import puzlock.verify.VerificationReport;

/**
 * One attempt: a depth-first search over piece extractions. Late pieces have little room and
 * often cannot be built from what earlier choices left, so when piece i fails, piece i−1 is
 * re-extracted with fresh random choices (up to {@link #ALTERNATIVES_PER_PIECE} times) before
 * backing up further. A budget caps the total number of extractions per attempt.
 * <p>
 * Each piece aims for an equal share of what is left, where the last piece counts as
 * {@code 1 + remainderReserve} shares. The last piece has to wrap around the two before it, which
 * a piece of only m voxels rarely can when m is small, so later attempts reserve it more room.
 */
final class PuzzleSearch {
    static final int ALTERNATIVES_PER_PIECE = 3;
    static final int EXTRACTIONS_PER_PIECE_BUDGET = 25;

    private final VoxelGrid grid;
    private final GenerationOptions options;
    private final RandomGenerator random;
    private final FailureTally tally;
    private final int remainderReserve;
    private int budget;
    private int furthestPiece;

    record Found(Puzzle puzzle, VerificationReport verification) {
    }

    PuzzleSearch(VoxelGrid grid, GenerationOptions options, RandomGenerator random, FailureTally tally,
                 int remainderReserve) {
        this.grid = grid;
        this.options = options;
        this.random = random;
        this.tally = tally;
        this.remainderReserve = remainderReserve;
        this.budget = EXTRACTIONS_PER_PIECE_BUDGET * options.pieceCount();
    }

    Optional<Found> run() {
        return extendFrom(grid.voxels(), new ArrayList<>());
    }

    /** The highest piece number this search tried to extract. */
    int furthestPiece() {
        return furthestPiece;
    }

    private Optional<Found> extendFrom(BitSet volume, List<Piece> pieces) {
        int number = pieces.size() + 1;
        furthestPiece = Math.max(furthestPiece, number);
        if (number == options.pieceCount()) {
            return verified(volume, pieces);
        }
        for (int alternative = 0; alternative < ALTERNATIVES_PER_PIECE && budget > 0; alternative++) {
            budget--;
            ExtractionResult result = extract(volume, pieces, number);
            if (!result.succeeded()) {
                tally.record(number, result.failure());
                return Optional.empty();
            }
            BitSet cells = result.draft().orElseThrow().cells();
            BitSet rest = (BitSet) volume.clone();
            rest.andNot(cells);
            pieces.add(Piece.extracted(cells, result.direction()));
            Optional<Found> found = extendFrom(rest, pieces);
            if (found.isPresent()) {
                return found;
            }
            pieces.remove(pieces.size() - 1);
        }
        return Optional.empty();
    }

    private ExtractionResult extract(BitSet volume, List<Piece> pieces, int number) {
        int sharesLeft = options.pieceCount() - number + 1 + remainderReserve;
        int targetSize = Math.max(1, volume.cardinality() / sharesLeft);
        if (pieces.isEmpty()) {
            return KeyPieceExtractor.extract(grid.shape(), volume, targetSize, options, random);
        }
        Piece previous = pieces.get(pieces.size() - 1);
        Direction previousDirection = previous.removalDirection().orElseThrow();
        return NextPieceExtractor.extract(grid.shape(), volume, previous.voxels(), previousDirection, targetSize,
                options, random);
    }

    private Optional<Found> verified(BitSet remainder, List<Piece> pieces) {
        List<Piece> all = new ArrayList<>(pieces);
        all.add(Piece.remainder(remainder));
        Puzzle puzzle = new Puzzle(grid.shape(), all);
        VerificationReport report = InterlockingVerifier.verify(grid, puzzle);
        if (!report.valid()) {
            tally.record(options.pieceCount(), FailureReason.VERIFICATION_FAILED);
            return Optional.empty();
        }
        return Optional.of(new Found(puzzle, report));
    }
}
