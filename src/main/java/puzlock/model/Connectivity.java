package puzlock.model;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;

/** Face-connectivity (6-neighbourhood) queries on voxel sets. */
public final class Connectivity {

    private Connectivity() {
    }

    /** The cells of {@code set} reachable from {@code start} through face-adjacent cells of {@code set}. */
    public static BitSet floodFill(GridShape shape, BitSet set, int start) {
        BitSet reached = new BitSet(shape.cellCount());
        if (!set.get(start)) {
            return reached;
        }
        Deque<Integer> queue = new ArrayDeque<>();
        reached.set(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            int cell = queue.poll();
            for (Direction direction : Direction.values()) {
                int next = shape.neighbour(cell, direction);
                if (next != GridShape.OUTSIDE && set.get(next) && !reached.get(next)) {
                    reached.set(next);
                    queue.add(next);
                }
            }
        }
        return reached;
    }

    /** True when {@code set} is non-empty and forms a single connected component. */
    public static boolean isConnected(GridShape shape, BitSet set) {
        int first = set.nextSetBit(0);
        return first >= 0 && floodFill(shape, set, first).cardinality() == set.cardinality();
    }

    public static int componentCount(GridShape shape, BitSet set) {
        BitSet unvisited = (BitSet) set.clone();
        int components = 0;
        for (int cell = unvisited.nextSetBit(0); cell >= 0; cell = unvisited.nextSetBit(cell + 1)) {
            unvisited.andNot(floodFill(shape, unvisited, cell));
            components++;
        }
        return components;
    }
}
