package puzlock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import puzlock.model.Connectivity;
import puzlock.model.Direction;
import puzlock.model.GridShape;
import puzlock.model.Piece;
import puzlock.model.Puzzle;
import puzlock.model.VoxelGrid;

/**
 * Checks a puzzle against Song et al.'s definition, independently of how it was generated.
 * For every disassembly stage [Pᵢ, …, Pₙ, Rₙ]: Pᵢ slides out along dᵢ, and no other group of
 * pieces can move at all. Stages with at most {@value #EXHAUSTIVE_LIMIT} pieces are checked over
 * every subset; larger stages are checked with Song's local rules, proven sufficient in their
 * appendix. The key P1 must also be removable in only one direction.
 */
public final class InterlockingVerifier {
    public static final int EXHAUSTIVE_LIMIT = 16;
    private static final int MAX_REPORTED_PROBLEMS = 10;
    private static final int EMPTY = -1;

    private final GridShape shape;
    private final List<Piece> pieces;
    private final int[] owner;
    private final List<String> problems = new ArrayList<>();
    private boolean exhaustive = true;

    private InterlockingVerifier(Puzzle puzzle) {
        this.shape = puzzle.shape();
        this.pieces = puzzle.pieces();
        this.owner = new int[shape.cellCount()];
        Arrays.fill(owner, EMPTY);
    }

    public static VerificationReport verify(VoxelGrid input, Puzzle puzzle) {
        InterlockingVerifier verifier = new InterlockingVerifier(puzzle);
        verifier.run(input);
        return new VerificationReport(verifier.problems.isEmpty(), verifier.exhaustive, verifier.problems);
    }

    private void run(VoxelGrid input) {
        if (pieces.size() < 2) {
            problems.add("A puzzle needs at least 2 pieces, found " + pieces.size());
            return;
        }
        if (!input.shape().equals(shape) || !checkPartition(input) || !checkDirections()) {
            return;
        }
        for (int stage = 0; stage < pieces.size() - 1 && problems.size() < MAX_REPORTED_PROBLEMS; stage++) {
            checkStage(stage);
        }
    }

    private boolean checkPartition(VoxelGrid input) {
        BitSet covered = new BitSet();
        for (int index = 0; index < pieces.size(); index++) {
            BitSet voxels = pieces.get(index).voxels();
            if (voxels.intersects(covered)) {
                problems.add(pieceName(index) + " overlaps an earlier piece");
            }
            if (!Connectivity.isConnected(shape, voxels)) {
                problems.add(pieceName(index) + " is empty or not in one connected part");
            }
            covered.or(voxels);
            for (int cell = voxels.nextSetBit(0); cell >= 0; cell = voxels.nextSetBit(cell + 1)) {
                owner[cell] = index;
            }
        }
        if (!covered.equals(input.voxels())) {
            problems.add("The pieces do not add up exactly to the input shape");
        }
        return problems.isEmpty();
    }

    private boolean checkDirections() {
        for (int index = 0; index < pieces.size() - 1; index++) {
            if (pieces.get(index).removalDirection().isEmpty()) {
                problems.add(pieceName(index) + " has no removal direction");
            }
        }
        return problems.isEmpty();
    }

    private void checkStage(int stage) {
        Direction direction = pieces.get(stage).removalDirection().orElseThrow();
        Optional<Integer> obstacle = firstObstacleAlong(stage, direction);
        obstacle.ifPresent(other -> problems.add(stageName(stage) + pieceName(stage) + " cannot slide out "
                + direction.label() + ": " + pieceName(other) + " is in the way"));
        if (stage == 0) {
            checkKeyMovesOneWayOnly(direction);
        }
        if (pieces.size() - stage <= EXHAUSTIVE_LIMIT) {
            checkAllGroups(stage);
        } else {
            exhaustive = false;
            checkLocalInterlocking(stage);
        }
    }

    private Optional<Integer> firstObstacleAlong(int stage, Direction direction) {
        BitSet voxels = pieces.get(stage).voxels();
        for (int cell = voxels.nextSetBit(0); cell >= 0; cell = voxels.nextSetBit(cell + 1)) {
            for (int next = shape.neighbour(cell, direction); next != GridShape.OUTSIDE; next = shape.neighbour(next, direction)) {
                if (owner[next] > stage) {
                    return Optional.of(owner[next]);
                }
            }
        }
        return Optional.empty();
    }

    private void checkKeyMovesOneWayOnly(Direction keyDirection) {
        for (Direction direction : Direction.values()) {
            if (direction != keyDirection && canStep(index -> index == 0, index -> index > 0, direction)) {
                problems.add("The key piece can also move " + direction.label() + ", not only " + keyDirection.label());
            }
        }
    }

    /** No subset of the stage's pieces other than {Pᵢ} may move (subsets moving one way equal their complements moving back). */
    private void checkAllGroups(int stage) {
        int count = pieces.size() - stage;
        long[][] contacts = contactMasks(stage, count);
        int last = count - 1;
        for (long group = 2; group < (1L << last); group++) {
            for (Direction direction : Direction.values()) {
                if (isFree(group, contacts[direction.ordinal()])) {
                    problems.add(stageName(stage) + describeGroup(stage, group) + " can slide " + direction.label() + " together");
                    if (problems.size() >= MAX_REPORTED_PROBLEMS) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean isFree(long group, long[] contacts) {
        for (long rest = group; rest != 0; rest &= rest - 1) {
            int member = Long.numberOfTrailingZeros(rest);
            if ((contacts[member] & ~group) != 0) {
                return false;
            }
        }
        return true;
    }

    /** contacts[d][a] has bit b set when some voxel of piece a has a voxel of piece b next to it along d (indices relative to the stage). */
    private long[][] contactMasks(int stage, int count) {
        long[][] contacts = new long[Direction.values().length][count];
        for (int cell = 0; cell < owner.length; cell++) {
            if (owner[cell] < stage) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                int next = shape.neighbour(cell, direction);
                if (next != GridShape.OUTSIDE && owner[next] >= stage && owner[next] != owner[cell]) {
                    contacts[direction.ordinal()][owner[cell] - stage] |= 1L << (owner[next] - stage);
                }
            }
        }
        return contacts;
    }

    /** Song §4.1: Pᵢ₊₁ is immobilised by Pᵢ ∪ Rᵢ₊₁, and Pᵢ ∪ Pᵢ₊₁ cannot move together relative to Rᵢ₊₁. */
    private void checkLocalInterlocking(int stage) {
        int next = stage + 1;
        for (Direction direction : Direction.values()) {
            if (canStep(index -> index == next, index -> index >= stage && index != next, direction)) {
                problems.add(stageName(stage) + pieceName(next) + " can slide " + direction.label());
            }
            if (canStep(index -> index == stage || index == next, index -> index > next, direction)) {
                problems.add(stageName(stage) + pieceName(stage) + " and " + pieceName(next) + " can slide "
                        + direction.label() + " together");
            }
        }
    }

    private boolean canStep(IntPredicate inGroup, IntPredicate isObstacle, Direction direction) {
        for (int cell = 0; cell < owner.length; cell++) {
            if (owner[cell] == EMPTY || !inGroup.test(owner[cell])) {
                continue;
            }
            int next = shape.neighbour(cell, direction);
            if (next != GridShape.OUTSIDE && owner[next] != EMPTY && isObstacle.test(owner[next])) {
                return false;
            }
        }
        return true;
    }

    private String describeGroup(int stage, long group) {
        return "pieces " + IntStream.range(0, 64)
                .filter(bit -> (group & (1L << bit)) != 0)
                .mapToObj(bit -> String.valueOf(stage + bit + 1))
                .collect(Collectors.joining(", "));
    }

    private static String stageName(int stage) {
        return stage == 0 ? "In the assembled puzzle, " : "After removing " + stage + " piece" + (stage == 1 ? "" : "s") + ", ";
    }

    private static String pieceName(int index) {
        return "piece " + (index + 1);
    }
}
