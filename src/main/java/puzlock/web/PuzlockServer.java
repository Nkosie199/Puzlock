package puzlock.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** The local viewer: static page, sample list, puzzle generation and piece downloads, on 127.0.0.1 only. */
public final class PuzlockServer {
    private static final Map<String, String> STATIC_TYPES = Map.of(
            "/index.html", "text/html; charset=utf-8",
            "/viewer.js", "text/javascript; charset=utf-8",
            "/style.css", "text/css; charset=utf-8");

    private final HttpServer server;

    private PuzlockServer(HttpServer server) {
        this.server = server;
    }

    public static PuzlockServer start(int port) throws IOException {
        return start(port, Path.of("samples"));
    }

    public static PuzlockServer start(int port, Path samplesFolder) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        Samples samples = new Samples(samplesFolder);
        ResultStore results = new ResultStore();
        server.createContext("/", PuzlockServer::serveStatic);
        server.createContext("/api/samples", exchange -> Http.sendJson(exchange, 200, samplesJson(samples)));
        server.createContext("/api/generate", new GenerateHandler(samples, results));
        server.createContext(PieceDownloadHandler.PREFIX, new PieceDownloadHandler(results));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        return new PuzlockServer(server);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }

    private static void serveStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String resource = path.equals("/") ? "/index.html" : path;
        String type = STATIC_TYPES.get(resource);
        if (type == null) {
            Http.sendError(exchange, 404, "Not found");
            return;
        }
        try (InputStream in = PuzlockServer.class.getResourceAsStream("/web" + resource)) {
            if (in == null) {
                Http.sendError(exchange, 404, "Not found");
                return;
            }
            Http.send(exchange, 200, type, in.readAllBytes());
        }
    }

    private static String samplesJson(Samples samples) throws IOException {
        return samples.names().stream()
                .map(name -> "\"" + name.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }
}
