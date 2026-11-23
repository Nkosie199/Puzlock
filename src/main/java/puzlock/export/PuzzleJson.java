package puzlock.export;

import java.util.BitSet;
import java.util.Optional;
import java.util.StringJoiner;
import puzlock.algorithm.GenerationOutcome;
import puzlock.io.LoadedInput;
import puzlock.model.GridShape;
import puzlock.model.Piece;

/**
 * The JSON the viewer reads. Success: the pieces in disassembly order with their directions.
 * Failure: the message and the input voxels, so the user still sees what was loaded.
 */
public final class PuzzleJson {

    private PuzzleJson() {
    }

    public static String write(String inputName, LoadedInput input, GenerationOutcome outcome) {
        return write(inputName, input, outcome, Optional.empty());
    }

    /** @param resultId where the viewer can download the pieces from, when they are kept on a server */
    public static String write(String inputName, LoadedInput input, GenerationOutcome outcome, Optional<String> resultId) {
        StringJoiner fields = new StringJoiner(",", "{", "}");
        fields.add(field("input", string(inputName)));
        resultId.ifPresent(id -> fields.add(field("resultId", string(id))));
        input.note().ifPresent(note -> fields.add(field("note", string(note))));
        fields.add(field("shape", shape(input.grid().shape())));
        fields.add(field("voxelCount", String.valueOf(input.grid().voxelCount())));
        fields.add(field("elapsedMs", String.valueOf(outcome.elapsed().toMillis())));
        switch (outcome) {
            case GenerationOutcome.Success success -> {
                fields.add(field("ok", "true"));
                fields.add(field("attempts", String.valueOf(success.attemptsUsed())));
                fields.add(field("verification", string(success.verification().summary())));
                fields.add(field("pieces", pieces(input.grid().shape(), success)));
            }
            case GenerationOutcome.Failure failure -> {
                fields.add(field("ok", "false"));
                fields.add(field("error", string(failure.message())));
                fields.add(field("inputVoxels", voxels(input.grid().shape(), input.grid().voxels())));
            }
        }
        return fields.toString();
    }

    /** For inputs that could not even be read: just the message. */
    public static String error(String inputName, String message) {
        return "{" + field("input", string(inputName)) + "," + field("ok", "false") + "," + field("error", string(message)) + "}";
    }

    private static String pieces(GridShape shape, GenerationOutcome.Success success) {
        StringJoiner pieces = new StringJoiner(",", "[", "]");
        int number = 1;
        for (Piece piece : success.puzzle().pieces()) {
            StringJoiner fields = new StringJoiner(",", "{", "}");
            fields.add(field("number", String.valueOf(number++)));
            Optional<String> direction = piece.removalDirection().map(d -> string(d.label()));
            fields.add(field("direction", direction.orElse("null")));
            fields.add(field("size", String.valueOf(piece.size())));
            fields.add(field("voxels", voxels(shape, piece.voxels())));
            pieces.add(fields.toString());
        }
        return pieces.toString();
    }

    private static String voxels(GridShape shape, BitSet cells) {
        StringJoiner voxels = new StringJoiner(",", "[", "]");
        for (int cell = cells.nextSetBit(0); cell >= 0; cell = cells.nextSetBit(cell + 1)) {
            voxels.add("[" + shape.x(cell) + "," + shape.y(cell) + "," + shape.z(cell) + "]");
        }
        return voxels.toString();
    }

    private static String shape(GridShape shape) {
        return "[" + shape.sizeX() + "," + shape.sizeY() + "," + shape.sizeZ() + "]";
    }

    private static String field(String name, String jsonValue) {
        return string(name) + ":" + jsonValue;
    }

    private static String string(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }
}
