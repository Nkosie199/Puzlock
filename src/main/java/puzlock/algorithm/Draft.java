package puzlock.algorithm;

import java.util.BitSet;

/**
 * A piece under construction: its cells, and the cells that must stay in the remaining volume
 * (blocking voxels, anchors and everything behind them) so blockage already built is kept.
 */
record Draft(BitSet cells, BitSet keptOut) {

    Draft {
        cells = (BitSet) cells.clone();
        keptOut = (BitSet) keptOut.clone();
    }

    static Draft of(BitSet cells) {
        return new Draft(cells, new BitSet());
    }

    int size() {
        return cells.cardinality();
    }

    Draft withCells(BitSet newCells) {
        return new Draft(newCells, keptOut);
    }

    Draft keepingOut(BitSet more) {
        BitSet combined = (BitSet) keptOut.clone();
        combined.or(more);
        return new Draft(cells, combined);
    }

    /** Cells of the volume that are not in this piece: the remaining volume if the piece were cut now. */
    BitSet remainderOf(BitSet volume) {
        BitSet remainder = (BitSet) volume.clone();
        remainder.andNot(cells);
        return remainder;
    }
}
