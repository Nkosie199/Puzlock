package puzlock.algorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import puzlock.model.GridShape;

/**
 * Song §5.1 step 3, generalised to any direction a piece still moves in: find the nearest
 * blockee/blocking voxel pairs along that direction, extend the piece to a blockee by a shortest
 * path that avoids the blocking voxel and everything behind it, make the result removable, and
 * keep the cheapest option by accessibility.
 */
final class BlockingStrategy {

    private BlockingStrategy() {
    }

    private record Pair(int blockee, int blocking) {
    }

    private record Option(Draft draft, double cost) {
    }

    static Optional<Draft> block(ExtractionContext context, Draft draft, BlockingRequirement requirement) {
        return nearestPairs(context, draft, requirement).stream()
                .map(pair -> extendTo(context, draft, requirement, pair))
                .flatMap(Optional::stream)
                .min(Comparator.comparingDouble(Option::cost))
                .map(Option::draft);
    }

    private static List<Pair> nearestPairs(ExtractionContext context, Draft draft, BlockingRequirement requirement) {
        GridShape shape = context.shape();
        BitSet passable = draft.remainderOf(context.volume());
        passable.andNot(draft.keptOut());
        BitSet blockers = draft.remainderOf(context.volume());
        blockers.or(requirement.fixedBlockers());
        List<Pair> pairs = new ArrayList<>();
        for (int blockee : PathFinder.distanceOrder(shape, draft.cells(), passable,
                context.roomLeft(draft.cells()), shape.cellCount())) {
            int blocking = shape.neighbour(blockee, requirement.direction());
            if (blocking != GridShape.OUTSIDE && blockers.get(blocking)) {
                pairs.add(new Pair(blockee, blocking));
                if (pairs.size() == context.options().nearestPairs()) {
                    break;
                }
            }
        }
        return pairs.stream()
                .sorted(Comparator.comparingDouble(pair -> context.accessibility()[pair.blockee()]))
                .limit(context.options().candidatePairs())
                .toList();
    }

    private static Optional<Option> extendTo(ExtractionContext context, Draft draft, BlockingRequirement requirement,
                                             Pair pair) {
        BitSet keptOut = (BitSet) draft.keptOut().clone();
        if (!requirement.fixedBlockers().get(pair.blocking())) {
            keptOut.or(context.cellAndBehind(pair.blocking()));
        }
        if (keptOut.get(pair.blockee())) {
            return Optional.empty();
        }
        BitSet passable = (BitSet) context.volume().clone();
        passable.andNot(keptOut);
        return PathFinder.shortestPath(context.shape(), draft.cells(), passable, cell -> cell == pair.blockee(),
                        context.roomLeft(draft.cells()))
                .flatMap(path -> {
                    BitSet grown = (BitSet) draft.cells().clone();
                    grown.or(path);
                    return RemovableClosure.close(context, grown, keptOut);
                })
                .filter(closed -> requirement.isMetBy(context, closed))
                .map(closed -> {
                    BitSet added = (BitSet) closed.clone();
                    added.andNot(draft.cells());
                    return new Option(new Draft(closed, keptOut), context.accessibilityOf(added));
                });
    }
}
