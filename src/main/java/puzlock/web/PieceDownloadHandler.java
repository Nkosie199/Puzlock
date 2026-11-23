package puzlock.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import puzlock.export.StlWriter;
import puzlock.io.GridFormat;
import puzlock.model.Piece;
import puzlock.model.Puzzle;

/**
 * {@code GET /api/results/<id>/piece-<n>.stl[?clearance=mm]} or {@code .grid}: one piece of a
 * generated puzzle.
 */
final class PieceDownloadHandler implements HttpHandler {
    static final String PREFIX = "/api/results/";
    private static final Pattern PATH = Pattern.compile("/api/results/([0-9a-f-]+)/piece-(\\d+)\\.(stl|grid)");

    private final ResultStore results;

    PieceDownloadHandler(ResultStore results) {
        this.results = results;
    }

    private static Optional<Float> clearance(HttpExchange exchange) {
        String value = Http.query(exchange).get("clearance");
        if (value == null) {
            return Optional.of(StlWriter.DEFAULT_CLEARANCE_MM);
        }
        try {
            float clearance = Float.parseFloat(value);
            return clearance >= 0 && clearance < StlWriter.VOXEL_SIZE_MM / 2 ? Optional.of(clearance) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Matcher match = PATH.matcher(exchange.getRequestURI().getPath());
        Optional<Puzzle> puzzle = match.matches() ? results.find(match.group(1)) : Optional.empty();
        if (puzzle.isEmpty()) {
            Http.sendError(exchange, 404, "Unknown result; generate the puzzle again");
            return;
        }
        int number = Integer.parseInt(match.group(2));
        if (number < 1 || number > puzzle.get().pieceCount()) {
            Http.sendError(exchange, 404, "No piece " + number);
            return;
        }
        Piece piece = puzzle.get().pieces().get(number - 1);
        String fileName = "piece-" + number + "." + match.group(3);
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        if (match.group(3).equals("stl")) {
            Optional<Float> clearance = clearance(exchange);
            if (clearance.isEmpty()) {
                Http.sendError(exchange, 400, "clearance must be a number of millimetres from 0 to under "
                        + StlWriter.VOXEL_SIZE_MM / 2);
                return;
            }
            Http.send(exchange, 200, "model/stl", StlWriter.write(puzzle.get().shape(), piece.voxels(), clearance.get()));
        } else {
            byte[] grid = GridFormat.write(puzzle.get().shape(), piece.voxels()).getBytes(StandardCharsets.US_ASCII);
            Http.send(exchange, 200, "text/plain; charset=us-ascii", grid);
        }
    }
}
