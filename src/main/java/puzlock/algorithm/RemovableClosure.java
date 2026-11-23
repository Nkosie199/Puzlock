package puzlock.algorithm;

import java.util.BitSet;
import java.util.Optional;
import puzlock.model.Connectivity;
import puzlock.model.GridShape;

/**
 * Makes a piece removable along the move direction: every volume voxel in front of a piece
 * voxel joins the piece, and any part this detaches is reconnected by a shortest path, until
 * nothing changes. Song's "include any voxel above the path" is this with the move direction up.
 */
final class RemovableClosure {

    private RemovableClosure() {
    }

    /** Empty when the closure would need a kept-out cell or grow past the size limit. */
    static Optional<BitSet> close(ExtractionContext context, BitSet piece, BitSet keptOut) {
        BitSet closed = (BitSet) piece.clone();
        while (true) {
            BitSet inFront = cellsInFront(context, closed);
            if (inFront.intersects(keptOut)) {
                return Optional.empty();
            }
            closed.or(inFront);
            if (closed.cardinality() > context.sizeLimit()) {
                return Optional.empty();
            }
            Optional<BitSet> bridges = bridgesBetweenParts(context, closed, keptOut);
            if (bridges.isEmpty()) {
                return Optional.empty();
            }
            if (inFront.isEmpty() && bridges.get().isEmpty()) {
                return Optional.of(closed);
            }
            closed.or(bridges.get());
        }
    }

    /** Volume cells in front of {@code piece} along the move direction that are not yet in it. */
    static BitSet cellsInFront(ExtractionContext context, BitSet piece) {
        BitSet inFront = new BitSet();
        for (int cell = piece.nextSetBit(0); cell >= 0; cell = piece.nextSetBit(cell + 1)) {
            inFront.or(Mobility.ray(context.shape(), cell, context.moveDirection(), context.volume()));
        }
        inFront.andNot(piece);
        return inFront;
    }

    /** Shortest-path cells joining every part of {@code piece} to its first part; empty when impossible. */
    private static Optional<BitSet> bridgesBetweenParts(ExtractionContext context, BitSet piece, BitSet keptOut) {
        GridShape shape = context.shape();
        BitSet passable = (BitSet) context.volume().clone();
        passable.andNot(keptOut);
        BitSet whole = (BitSet) piece.clone();
        int first = piece.nextSetBit(0);
        BitSet joined = Connectivity.floodFill(shape, whole, first);
        while (joined.cardinality() < whole.cardinality()) {
            BitSet unjoined = (BitSet) whole.clone();
            unjoined.andNot(joined);
            Optional<BitSet> path = PathFinder.shortestPath(shape, joined, passable, unjoined::get,
                    context.roomLeft(whole) + 1);
            if (path.isEmpty()) {
                return Optional.empty();
            }
            whole.or(path.get());
            joined = Connectivity.floodFill(shape, whole, first);
        }
        whole.andNot(piece);
        return Optional.of(whole);
    }
}
