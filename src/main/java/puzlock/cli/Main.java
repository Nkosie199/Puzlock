package puzlock.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import puzlock.algorithm.GenerationOptions;
import puzlock.algorithm.GenerationOutcome;
import puzlock.algorithm.PuzzleGenerator;
import puzlock.export.PuzzleExporter;
import puzlock.io.InputLoader;
import puzlock.io.InvalidInputException;
import puzlock.io.LoadedInput;
import puzlock.model.Piece;
import puzlock.web.PuzlockServer;

/** Command-line entry point: {@code generate} a puzzle from a file, or {@code serve} the viewer. */
public final class Main {
    static final int OK = 0;
    static final int USAGE_ERROR = 1;
    static final int NO_PUZZLE = 2;
    private static final int DEFAULT_PORT = 8080;

    private static final String USAGE = """
            Usage:
              java -jar puzlock.jar generate <input.grid|.stl|.obj> --pieces K [--resolution 16] [--seed 1]
                                             [--attempts 40] [--out out/<input name>]
              java -jar puzlock.jar serve [--port 8080]
            """;

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        int exitCode = run(args, System.out, System.err);
        if (exitCode != OK) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) throws IOException {
        try {
            Arguments arguments = new Arguments(args);
            String command = arguments.positional(0).orElseThrow(() -> new Arguments.UsageException("No command given"));
            return switch (command) {
                case "generate" -> generate(arguments, out, err);
                case "serve" -> serve(arguments, out);
                default -> throw new Arguments.UsageException("Unknown command '" + command + "'");
            };
        } catch (Arguments.UsageException e) {
            err.println(e.getMessage());
            err.print(USAGE);
            return USAGE_ERROR;
        }
    }

    private static int generate(Arguments arguments, PrintStream out, PrintStream err) throws IOException {
        Path input = Path.of(arguments.positional(1).orElseThrow(() -> new Arguments.UsageException("No input file given")));
        int pieces = arguments.intOption("pieces", -1);
        if (pieces < 0) {
            throw new Arguments.UsageException("--pieces is required");
        }
        if (!Files.isRegularFile(input)) {
            err.println("Input file not found: " + input);
            return NO_PUZZLE;
        }
        LoadedInput loaded;
        try {
            loaded = InputLoader.load(input, arguments.intOption("resolution", InputLoader.DEFAULT_RESOLUTION));
        } catch (InvalidInputException e) {
            err.println("Cannot use " + input.getFileName() + ": " + e.getMessage());
            return NO_PUZZLE;
        }
        loaded.note().ifPresent(note -> out.println("Note: " + note));
        out.println("Loaded " + input.getFileName() + ": " + loaded.grid().voxelCount() + " voxels in a "
                + loaded.grid().shape() + " grid; making " + pieces + " pieces...");
        GenerationOptions options = GenerationOptions.defaults(pieces)
                .withSeed(arguments.intOption("seed", 1))
                .withAttempts(arguments.intOption("attempts", GenerationOptions.DEFAULT_ATTEMPTS));
        GenerationOutcome outcome = PuzzleGenerator.generate(loaded.grid(), options);
        Path folder = Path.of(arguments.option("out").orElse("out/" + baseName(input)));
        PuzzleExporter.export(folder, input.getFileName().toString(), loaded, outcome);
        return report(outcome, folder, out, err);
    }

    private static int report(GenerationOutcome outcome, Path folder, PrintStream out, PrintStream err) {
        double seconds = outcome.elapsed().toMillis() / 1000.0;
        if (outcome instanceof GenerationOutcome.Failure failure) {
            err.printf("No puzzle after %.1f s: %s%n", seconds, failure.message());
            return NO_PUZZLE;
        }
        GenerationOutcome.Success success = (GenerationOutcome.Success) outcome;
        List<Piece> pieces = success.puzzle().pieces();
        for (int index = 0; index < pieces.size(); index++) {
            Piece piece = pieces.get(index);
            String direction = piece.removalDirection().map(d -> "removed " + d.label()).orElse("last piece");
            out.printf("  piece %d: %d voxels, %s%n", index + 1, piece.size(), direction);
        }
        out.printf("%s in %.1f s (attempt %d)%n", success.verification().summary(), seconds, success.attemptsUsed());
        out.println("Wrote piece-N.grid, piece-N.stl and puzzle.json to " + folder.toAbsolutePath());
        return OK;
    }

    private static int serve(Arguments arguments, PrintStream out) throws IOException {
        int port = arguments.intOption("port", DEFAULT_PORT);
        PuzlockServer server = PuzlockServer.start(port);
        out.println("Puzlock viewer running at http://localhost:" + server.port() + " (Ctrl+C to stop)");
        return OK;
    }

    private static String baseName(Path input) {
        String name = input.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
