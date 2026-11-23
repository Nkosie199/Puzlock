package puzlock.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import puzlock.algorithm.GenerationOutcome;
import puzlock.io.GridFormat;
import puzlock.io.LoadedInput;
import puzlock.model.GridShape;
import puzlock.model.Piece;

/** Writes a result folder: piece-N.grid (renderer format), piece-N.stl (for printing) and puzzle.json. */
public final class PuzzleExporter {

    private PuzzleExporter() {
    }

    public static void export(Path folder, String inputName, LoadedInput input, GenerationOutcome outcome) throws IOException {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("puzzle.json"), PuzzleJson.write(inputName, input, outcome), StandardCharsets.UTF_8);
        if (outcome instanceof GenerationOutcome.Success success) {
            GridShape shape = success.puzzle().shape();
            List<Piece> pieces = success.puzzle().pieces();
            for (int index = 0; index < pieces.size(); index++) {
                String name = "piece-" + (index + 1);
                Files.writeString(folder.resolve(name + ".grid"), GridFormat.write(shape, pieces.get(index).voxels()));
                Files.write(folder.resolve(name + ".stl"), StlWriter.write(shape, pieces.get(index).voxels()));
            }
        }
    }
}
