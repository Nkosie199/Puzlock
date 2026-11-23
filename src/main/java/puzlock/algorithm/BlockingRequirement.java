package puzlock.algorithm;

import java.util.BitSet;
import puzlock.model.Direction;

/**
 * "The piece (moving together with {@code companion}) must be blocked along {@code direction}
 * by the remaining volume or by {@code fixedBlockers}".
 * <ul>
 *   <li>Key piece: piece alone, blocked by the remainder, for every direction but up.</li>
 *   <li>Next piece Pᵢ₊₁, Song §5.2 step 3: (a) blocked by Pᵢ ∪ Rᵢ₊₁ for every direction but dᵢ₊₁;
 *       (b) Pᵢ ∪ Pᵢ₊₁ blocked by Rᵢ₊₁ in every direction, so the two cannot move together.</li>
 * </ul>
 */
record BlockingRequirement(Direction direction, BitSet companion, BitSet fixedBlockers) {

    static BlockingRequirement byRemainder(Direction direction) {
        return new BlockingRequirement(direction, new BitSet(), new BitSet());
    }

    static BlockingRequirement byRemainderOrPiece(Direction direction, BitSet previousPiece) {
        return new BlockingRequirement(direction, new BitSet(), previousPiece);
    }

    static BlockingRequirement togetherWith(Direction direction, BitSet previousPiece) {
        return new BlockingRequirement(direction, previousPiece, new BitSet());
    }

    BitSet mover(BitSet pieceCells) {
        BitSet mover = (BitSet) pieceCells.clone();
        mover.or(companion);
        return mover;
    }

    boolean isMetBy(ExtractionContext context, BitSet pieceCells) {
        BitSet obstacles = new Draft(pieceCells, new BitSet()).remainderOf(context.volume());
        obstacles.or(fixedBlockers);
        return Mobility.isBlocked(context.shape(), mover(pieceCells), obstacles, direction);
    }

    /** Met through a fixed blocker, which can never be taken into the piece, so no anchor is needed. */
    boolean isPermanentlyMetBy(ExtractionContext context, BitSet pieceCells) {
        return Mobility.isBlocked(context.shape(), pieceCells, fixedBlockers, direction);
    }
}
