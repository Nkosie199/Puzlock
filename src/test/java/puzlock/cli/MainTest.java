package puzlock.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @TempDir
    Path folder;

    @Test
    void generateWritesEveryPieceAndReportsTheVerdict() throws Exception {
        Path result = folder.resolve("cube4");
        int code = run("generate", "samples/cube4.grid", "--pieces", "8", "--seed", "4", "--out", result.toString());
        assertEquals(Main.OK, code, text(err));
        assertTrue(text(out).contains("Recursive interlocking verified"), text(out));
        for (int piece = 1; piece <= 8; piece++) {
            assertTrue(Files.exists(result.resolve("piece-" + piece + ".grid")));
            assertTrue(Files.exists(result.resolve("piece-" + piece + ".stl")));
        }
        assertTrue(Files.readString(result.resolve("puzzle.json")).contains("\"ok\":true"));
    }

    @Test
    void impossibleRequestsExitWithASpecificMessage() throws Exception {
        Path tiny = Files.writeString(folder.resolve("tiny.grid"), "11,11\n11,11");
        int code = run("generate", tiny.toString(), "--pieces", "8", "--out", folder.resolve("tiny").toString());
        assertEquals(Main.NO_PUZZLE, code);
        assertTrue(text(err).contains("each piece would have under 3 voxels"), text(err));
    }

    @Test
    void unreadableInputsExitWithTheReason() throws Exception {
        Path prose = Files.writeString(folder.resolve("notes.txt"), "Not a shape at all");
        assertEquals(Main.NO_PUZZLE, run("generate", prose.toString(), "--pieces", "4"));
        assertTrue(text(err).contains("Unsupported input"), text(err));
    }

    @Test
    void badUsageShowsHelp() throws Exception {
        assertEquals(Main.USAGE_ERROR, run("generate", "samples/cube4.grid"));
        assertTrue(text(err).contains("--pieces is required") && text(err).contains("Usage:"), text(err));
        assertEquals(Main.USAGE_ERROR, run("explode"));
    }

    private int run(String... args) throws Exception {
        return Main.run(args, new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private static String text(ByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8);
    }
}
