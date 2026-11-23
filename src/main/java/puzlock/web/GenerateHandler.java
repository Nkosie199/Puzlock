package puzlock.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import puzlock.algorithm.GenerationOptions;
import puzlock.algorithm.GenerationOutcome;
import puzlock.algorithm.PuzzleGenerator;
import puzlock.export.PuzzleJson;
import puzlock.io.InputLoader;
import puzlock.io.InvalidInputException;
import puzlock.io.LoadedInput;

/**
 * {@code POST /api/generate?name=&pieces=&resolution=&seed=} with the file as the body, or
 * {@code ?sample=<file>} to use a bundled sample. Always answers with the viewer's JSON; failures
 * to build a puzzle are a normal answer ({@code "ok": false}), not an HTTP error.
 */
final class GenerateHandler implements HttpHandler {
    private static final int MAX_UPLOAD_BYTES = 64 * 1024 * 1024;
    private static final int MAX_PIECES = 2000;
    private static final int MAX_RESOLUTION = 64;

    private final Samples samples;
    private final ResultStore results;

    GenerateHandler(Samples samples, ResultStore results) {
        this.samples = samples;
        this.results = results;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            respond(exchange);
        } catch (RuntimeException e) {
            Http.sendError(exchange, 500, "Unexpected error: " + e);
            throw e;
        }
    }

    private void respond(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            Http.sendError(exchange, 405, "Use POST");
            return;
        }
        Map<String, String> query = Http.query(exchange);
        Optional<Upload> upload = readUpload(exchange, query);
        if (upload.isEmpty()) {
            return;
        }
        int pieces = boundedInt(query, "pieces", 8, 0, MAX_PIECES);
        int resolution = boundedInt(query, "resolution", InputLoader.DEFAULT_RESOLUTION, 2, MAX_RESOLUTION);
        long seed = boundedInt(query, "seed", 1, 0, Integer.MAX_VALUE);
        String name = upload.get().name();
        LoadedInput input;
        try {
            input = InputLoader.load(name, upload.get().content(), resolution);
        } catch (InvalidInputException e) {
            Http.sendJson(exchange, 200, PuzzleJson.error(name, e.getMessage()));
            return;
        }
        GenerationOutcome outcome = PuzzleGenerator.generate(input.grid(), GenerationOptions.defaults(pieces).withSeed(seed));
        Optional<String> resultId = outcome instanceof GenerationOutcome.Success success
                ? Optional.of(results.add(success.puzzle()))
                : Optional.empty();
        Http.sendJson(exchange, 200, PuzzleJson.write(name, input, outcome, resultId));
    }

    private record Upload(String name, byte[] content) {
    }

    private Optional<Upload> readUpload(HttpExchange exchange, Map<String, String> query) throws IOException {
        String sample = query.get("sample");
        if (sample != null) {
            Optional<byte[]> content = samples.read(sample);
            if (content.isEmpty()) {
                Http.sendError(exchange, 404, "No sample called " + sample);
                return Optional.empty();
            }
            return Optional.of(new Upload(sample, content.get()));
        }
        byte[] body;
        try (InputStream in = exchange.getRequestBody()) {
            body = in.readNBytes(MAX_UPLOAD_BYTES + 1);
        }
        if (body.length > MAX_UPLOAD_BYTES) {
            Http.sendError(exchange, 413, "File is larger than " + MAX_UPLOAD_BYTES / (1024 * 1024) + " MB");
            return Optional.empty();
        }
        return Optional.of(new Upload(query.getOrDefault("name", "upload"), body));
    }

    private static int boundedInt(Map<String, String> query, String name, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(query.getOrDefault(name, String.valueOf(fallback)));
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
