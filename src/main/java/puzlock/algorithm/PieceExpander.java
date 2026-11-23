package puzlock.algorithm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import puzlock.model.Connectivity;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/**
 * Song §5.1 step 4: grow a piece towards the target size. Each round, every neighbouring voxel
 * that is not kept out is a candidate together with the voxels in front of it; one is picked at
 * random with probability proportional to (accessibility sum)^-β, favouring hard-to-reach voxels.
 * A pick that would split the remaining volume or undo required blockage is discarded and another
 * one drawn. Song relies on anchors alone and tests connectivity once at the end; at piece scale
 * the explicit tests are cheap and stop most wasted attempts on small pieces.
 */
final class PieceExpander {
    private static final double MIN_ACCESSIBILITY_SUM = 1e-9;

    private PieceExpander() {
    }

    private record Candidate(BitSet cells, double weight) {
    }

    static Draft expand(ExtractionContext context, Draft draft, List<BlockingRequirement> requirements) {
        BitSet piece = (BitSet) draft.cells().clone();
        while (piece.cardinality() < context.targetSize()) {
            List<Candidate> candidates = new ArrayList<>(candidates(context, piece, draft.keptOut()));
            Candidate accepted = null;
            while (accepted == null && !candidates.isEmpty()) {
                Candidate picked = pick(context, candidates);
                candidates.remove(picked);
                if (isSafe(context, piece, picked.cells(), requirements)) {
                    accepted = picked;
                }
            }
            if (accepted == null) {
                break;
            }
            piece.or(accepted.cells());
        }
        return draft.withCells(piece);
    }

    private static boolean isSafe(ExtractionContext context, BitSet piece, BitSet added, List<BlockingRequirement> requirements) {
        BitSet grown = (BitSet) piece.clone();
        grown.or(added);
        BitSet remainder = (BitSet) context.volume().clone();
        remainder.andNot(grown);
        return Connectivity.isConnected(context.shape(), remainder)
                && requirements.stream().allMatch(requirement -> requirement.isMetBy(context, grown));
    }

    private static List<Candidate> candidates(ExtractionContext context, BitSet piece, BitSet keptOut) {
        GridShape shape = context.shape();
        BitSet free = (BitSet) context.volume().clone();
        free.andNot(piece);
        free.andNot(keptOut);
        int room = context.targetSize() - piece.cardinality();
        List<Candidate> candidates = new ArrayList<>();
        for (int seed : neighbours(shape, piece, free)) {
            BitSet cells = Mobility.ray(shape, seed, context.moveDirection(), context.volume());
            cells.andNot(piece);
            cells.set(seed);
            if (cells.cardinality() <= room && !cells.intersects(keptOut) && staysConnected(context, piece, seed, cells)) {
                double sum = Math.max(context.accessibilityOf(cells), MIN_ACCESSIBILITY_SUM);
                candidates.add(new Candidate(cells, Math.pow(sum, -context.options().beta())));
            }
        }
        return candidates;
    }

    private static List<Integer> neighbours(GridShape shape, BitSet piece, BitSet free) {
        BitSet found = new BitSet();
        for (int cell = piece.nextSetBit(0); cell >= 0; cell = piece.nextSetBit(cell + 1)) {
            for (Direction direction : Direction.values()) {
                int next = shape.neighbour(cell, direction);
                if (next != GridShape.OUTSIDE && free.get(next)) {
                    found.set(next);
                }
            }
        }
        return found.stream().boxed().toList();
    }

    /** Cheap when the added cells form one straight run from the seed; a full check otherwise. */
    private static boolean staysConnected(ExtractionContext context, BitSet piece, int seed, BitSet added) {
        GridShape shape = context.shape();
        int run = 1;
        for (int cell = shape.neighbour(seed, context.moveDirection());
             cell != GridShape.OUTSIDE && added.get(cell);
             cell = shape.neighbour(cell, context.moveDirection())) {
            run++;
        }
        if (run == added.cardinality()) {
            return true;
        }
        BitSet union = (BitSet) piece.clone();
        union.or(added);
        return Connectivity.isConnected(shape, union);
    }

    private static Candidate pick(ExtractionContext context, List<Candidate> candidates) {
        double total = candidates.stream().mapToDouble(Candidate::weight).sum();
        double target = context.random().nextDouble() * total;
        for (Candidate candidate : candidates) {
            target -= candidate.weight();
            if (target <= 0) {
                return candidate;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
