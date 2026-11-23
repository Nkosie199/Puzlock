package puzlock.algorithm;

import java.util.BitSet;
import puzlock.model.GridShape;

/**
 * Song's anchor voxels. Once a requirement is met through a contact with the remaining volume,
 * the furthest directly-connected remaining voxel along the blocked direction becomes an anchor
 * and is kept out of the piece, together with everything behind it (which would drag it in via
 * the removable closure). Whatever the piece later absorbs along that run, the anchor still
 * blocks it, so no mobility re-test is needed while expanding.
 */
final class Anchors {

    private Anchors() {
    }

    static Draft protect(ExtractionContext context, Draft draft, BlockingRequirement requirement) {
        if (requirement.isPermanentlyMetBy(context, draft.cells())) {
            return draft;
        }
        GridShape shape = context.shape();
        BitSet remainder = draft.remainderOf(context.volume());
        BitSet mover = requirement.mover(draft.cells());
        BitSet smallestZone = null;
        for (int cell = mover.nextSetBit(0); cell >= 0; cell = mover.nextSetBit(cell + 1)) {
            int contact = shape.neighbour(cell, requirement.direction());
            if (contact == GridShape.OUTSIDE || !remainder.get(contact)) {
                continue;
            }
            if (draft.keptOut().get(contact)) {
                return draft;
            }
            BitSet zone = context.cellAndBehind(furthestAlong(shape, contact, requirement, remainder));
            boolean usable = !zone.intersects(draft.cells());
            if (usable && (smallestZone == null || zone.cardinality() < smallestZone.cardinality())) {
                smallestZone = zone;
            }
        }
        return smallestZone == null ? draft : draft.keepingOut(smallestZone);
    }

    private static int furthestAlong(GridShape shape, int start, BlockingRequirement requirement, BitSet remainder) {
        int anchor = start;
        for (int next = shape.neighbour(anchor, requirement.direction());
             next != GridShape.OUTSIDE && remainder.get(next);
             next = shape.neighbour(anchor, requirement.direction())) {
            anchor = next;
        }
        return anchor;
    }
}
