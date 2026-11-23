package puzlock.algorithm;

import java.util.Optional;
import puzlock.model.Connectivity;
import puzlock.model.VoxelGrid;

/** Problems that make generation impossible whatever the random choices, reported before any work. */
final class InputChecks {
    static final int MIN_PIECE_SIZE = 3;

    private InputChecks() {
    }

    static Optional<String> problemWith(VoxelGrid grid, int pieceCount) {
        int voxels = grid.voxelCount();
        if (voxels == 0) {
            return Optional.of("The input contains no voxels");
        }
        int parts = Connectivity.componentCount(grid.shape(), grid.voxels());
        if (parts > 1) {
            return Optional.of("Input has " + parts + " separate parts; a puzzle needs one connected shape");
        }
        if (pieceCount < 2) {
            return Optional.of("A puzzle needs at least 2 pieces, got " + pieceCount);
        }
        if (voxels / pieceCount < MIN_PIECE_SIZE) {
            return Optional.of("Cannot make " + pieceCount + " pieces from " + voxels + " voxels: each piece would have under "
                    + MIN_PIECE_SIZE + " voxels");
        }
        if (KeyPieceExtractor.seedCandidates(grid.shape(), grid.voxels()).isEmpty()) {
            return Optional.of("Cannot build the key piece: " + FailureReason.NO_KEY_SEED.description());
        }
        return Optional.empty();
    }
}
