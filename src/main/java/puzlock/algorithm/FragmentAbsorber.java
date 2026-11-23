package puzlock.algorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import puzlock.model.Connectivity;

/**
 * Blocking paths can cut small parts off the remaining volume. Rather than discard the piece,
 * take those parts into it when the piece stays removable, within its size limit and blocked as
 * required; the largest part stays as the remainder.
 */
final class FragmentAbsorber {

    private FragmentAbsorber() {
    }

    static Optional<Draft> absorb(ExtractionContext context, Draft draft, List<BlockingRequirement> requirements) {
        List<BitSet> parts = parts(context, draft.remainderOf(context.volume()));
        BitSet piece = (BitSet) draft.cells().clone();
        for (BitSet fragment : parts.subList(1, parts.size())) {
            BitSet grown = (BitSet) piece.clone();
            grown.or(fragment);
            Optional<BitSet> closed = RemovableClosure.close(context, grown, draft.keptOut());
            if (closed.isEmpty()) {
                return Optional.empty();
            }
            piece = closed.get();
        }
        BitSet absorbed = piece;
        boolean valid = Connectivity.isConnected(context.shape(), draft.withCells(absorbed).remainderOf(context.volume()))
                && requirements.stream().allMatch(requirement -> requirement.isMetBy(context, absorbed));
        return valid ? Optional.of(draft.withCells(absorbed)) : Optional.empty();
    }

    /** Connected parts of {@code remainder}, largest first. */
    private static List<BitSet> parts(ExtractionContext context, BitSet remainder) {
        List<BitSet> parts = new ArrayList<>();
        BitSet unvisited = (BitSet) remainder.clone();
        for (int cell = unvisited.nextSetBit(0); cell >= 0; cell = unvisited.nextSetBit(cell + 1)) {
            BitSet part = Connectivity.floodFill(context.shape(), unvisited, cell);
            unvisited.andNot(part);
            parts.add(part);
        }
        parts.sort(Comparator.comparingInt(BitSet::cardinality).reversed());
        return parts;
    }
}
