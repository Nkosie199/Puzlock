package puzlock.algorithm;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/** Counts failed attempts by the piece they got stuck on, to explain an overall failure. */
final class FailureTally {
    private final TreeMap<Integer, Map<FailureReason, Integer>> byPiece = new TreeMap<>();

    void record(int pieceNumber, FailureReason reason) {
        byPiece.computeIfAbsent(pieceNumber, number -> new EnumMap<>(FailureReason.class)).merge(reason, 1, Integer::sum);
    }

    /** Names the furthest piece any attempt reached and the most frequent reason it failed there. */
    String summary(int pieceCount, int attempts) {
        Map.Entry<Integer, Map<FailureReason, Integer>> furthest = byPiece.lastEntry();
        FailureReason reason = furthest.getValue().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow()
                .getKey();
        return "Could not build piece " + furthest.getKey() + " of " + pieceCount + " in " + attempts + " attempt"
                + (attempts == 1 ? "" : "s") + ": " + reason.description();
    }
}
