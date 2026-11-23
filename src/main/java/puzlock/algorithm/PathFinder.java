package puzlock.algorithm;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;
import java.util.function.IntPredicate;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/**
 * Iterative breadth-first searches over face-adjacent cells (unit edge weights, so BFS is Dijkstra).
 * Every search is bounded by {@code maxDepth}: a path longer than the room a piece has left can
 * never be used, so this prunes exactly and keeps searches local to the piece, as Song's
 * bounding-box localisation does.
 */
final class PathFinder {

    private PathFinder() {
    }

    /**
     * Cells of {@code passable} (outside {@code from}) within {@code maxDepth} steps, in order of
     * distance, stopping once {@code limit} cells are found.
     */
    static int[] distanceOrder(GridShape shape, BitSet from, BitSet passable, int maxDepth, int limit) {
        Search search = new Search(shape, from, passable, maxDepth);
        return search.run(cell -> false, limit);
    }

    /**
     * The shortest path, of at most {@code maxDepth} cells, from any cell of {@code from} to the nearest
     * cell accepted by {@code isTarget} through {@code passable} cells. The returned cells exclude
     * {@code from} and include the target.
     */
    static Optional<BitSet> shortestPath(GridShape shape, BitSet from, BitSet passable, IntPredicate isTarget, int maxDepth) {
        Search search = new Search(shape, from, passable, maxDepth);
        int[] reached = search.run(isTarget, Integer.MAX_VALUE);
        if (reached.length == 0 || !isTarget.test(reached[reached.length - 1])) {
            return Optional.empty();
        }
        return Optional.of(search.pathTo(reached[reached.length - 1]));
    }

    private static final class Search {
        private final GridShape shape;
        private final BitSet from;
        private final BitSet passable;
        private final int maxDepth;
        private final Scratch scratch;

        Search(GridShape shape, BitSet from, BitSet passable, int maxDepth) {
            this.shape = shape;
            this.from = from;
            this.passable = passable;
            this.maxDepth = maxDepth;
            this.scratch = Scratch.forGrid(shape.cellCount());
        }

        /** Visits cells in BFS order and returns them, ending at the first target if one is found. */
        int[] run(IntPredicate isTarget, int limit) {
            int[] queue = scratch.queue;
            int head = 0;
            int tail = 0;
            for (int cell = from.nextSetBit(0); cell >= 0; cell = from.nextSetBit(cell + 1)) {
                scratch.visit(cell, -1, 0);
                queue[tail++] = cell;
            }
            int firstVisited = tail;
            while (head < tail) {
                int cell = queue[head++];
                int depth = scratch.depth[cell];
                if (depth == maxDepth) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    int next = shape.neighbour(cell, direction);
                    if (next == GridShape.OUTSIDE || scratch.isVisited(next) || !passable.get(next)) {
                        continue;
                    }
                    scratch.visit(next, cell, depth + 1);
                    queue[tail++] = next;
                    if (isTarget.test(next) || tail - firstVisited == limit) {
                        return Arrays.copyOfRange(queue, firstVisited, tail);
                    }
                }
            }
            return Arrays.copyOfRange(queue, firstVisited, tail);
        }

        BitSet pathTo(int target) {
            BitSet path = new BitSet();
            for (int cell = target; cell >= 0 && !from.get(cell); cell = scratch.parent[cell]) {
                path.set(cell);
            }
            return path;
        }
    }

    /** Per-thread search arrays, reused across searches; a stamp marks which cells this search visited. */
    private static final class Scratch {
        private static final ThreadLocal<Scratch> CURRENT = new ThreadLocal<>();

        final int[] queue;
        final int[] parent;
        final int[] depth;
        private final int[] visitedStamp;
        private int stamp;

        private Scratch(int cells) {
            queue = new int[cells];
            parent = new int[cells];
            depth = new int[cells];
            visitedStamp = new int[cells];
        }

        static Scratch forGrid(int cells) {
            Scratch scratch = CURRENT.get();
            if (scratch == null || scratch.queue.length != cells) {
                scratch = new Scratch(cells);
                CURRENT.set(scratch);
            }
            scratch.stamp++;
            return scratch;
        }

        boolean isVisited(int cell) {
            return visitedStamp[cell] == stamp;
        }

        void visit(int cell, int parentCell, int cellDepth) {
            visitedStamp[cell] = stamp;
            parent[cell] = parentCell;
            depth[cell] = cellDepth;
        }
    }
}
