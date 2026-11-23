package puzlock.model;

import java.util.List;

/** Pieces in disassembly order: P1 (the key) first, the remaining piece last. */
public record Puzzle(GridShape shape, List<Piece> pieces) {

    public Puzzle {
        pieces = List.copyOf(pieces);
    }

    public int pieceCount() {
        return pieces.size();
    }
}
