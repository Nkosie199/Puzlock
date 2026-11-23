package puzlock.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import puzlock.model.Puzzle;

/** Keeps the most recent generated puzzles in memory so their pieces can be downloaded. */
final class ResultStore {
    private static final int CAPACITY = 20;

    private final Map<String, Puzzle> puzzles = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Puzzle> eldest) {
            return size() > CAPACITY;
        }
    };

    synchronized String add(Puzzle puzzle) {
        String id = UUID.randomUUID().toString();
        puzzles.put(id, puzzle);
        return id;
    }

    synchronized Optional<Puzzle> find(String id) {
        return Optional.ofNullable(puzzles.get(id));
    }
}
