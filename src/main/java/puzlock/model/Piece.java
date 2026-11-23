package puzlock.model;

import java.util.BitSet;
import java.util.Optional;

/**
 * One puzzle piece. Extracted pieces have the direction they are removed in; the final
 * remaining piece has none, because it is what is left once every other piece is out.
 */
public final class Piece {
    private final BitSet voxels;
    private final Direction removalDirection;

    private Piece(BitSet voxels, Direction removalDirection) {
        this.voxels = (BitSet) voxels.clone();
        this.removalDirection = removalDirection;
    }

    public static Piece extracted(BitSet voxels, Direction removalDirection) {
        return new Piece(voxels, removalDirection);
    }

    public static Piece remainder(BitSet voxels) {
        return new Piece(voxels, null);
    }

    public BitSet voxels() {
        return (BitSet) voxels.clone();
    }

    public int size() {
        return voxels.cardinality();
    }

    public Optional<Direction> removalDirection() {
        return Optional.ofNullable(removalDirection);
    }
}
