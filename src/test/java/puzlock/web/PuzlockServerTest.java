package puzlock.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import puzlock.io.StlReader;

class PuzlockServerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    private PuzlockServer server;

    @BeforeEach
    void start() throws IOException {
        server = PuzlockServer.start(0, Path.of("samples"));
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void servesTheViewerAndTheSampleList() throws Exception {
        assertTrue(get("/").body().contains("<title>Puzlock Viewer</title>"));
        assertTrue(get("/viewer.js").body().contains("OrbitControls"));
        assertTrue(get("/api/samples").body().contains("\"cube4.grid\""));
        assertEquals(404, get("/secret.txt").statusCode());
    }

    @Test
    void generatesFromASampleAndServesEachPiece() throws Exception {
        String json = post("/api/generate?sample=cube4.grid&pieces=4&seed=2", "").body();
        assertTrue(json.contains("\"ok\":true"), json);
        assertTrue(json.contains("Recursive interlocking verified"), json);
        Matcher id = Pattern.compile("\"resultId\":\"([^\"]+)\"").matcher(json);
        assertTrue(id.find());
        HttpResponse<byte[]> stl = client.send(request("/api/results/" + id.group(1) + "/piece-1.stl").build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, stl.statusCode());
        assertTrue(StlReader.read(stl.body()).triangleCount() > 0);
        assertEquals(404, get("/api/results/" + id.group(1) + "/piece-9.grid").statusCode());
    }

    @Test
    void reportsUploadsThatCannotBecomePuzzles() throws Exception {
        String prose = post("/api/generate?name=notes.txt&pieces=4", "Hello, this is not a shape").body();
        assertTrue(prose.contains("\"ok\":false") && prose.contains("Unsupported input"), prose);
        String tooMany = post("/api/generate?name=tiny.grid&pieces=8", "11,11\n11,11").body();
        assertTrue(tooMany.contains("each piece would have under 3 voxels") && tooMany.contains("inputVoxels"), tooMany);
    }

    @Test
    void refusesSampleNamesOutsideTheSamplesFolder() throws Exception {
        assertEquals(404, post("/api/generate?sample=..%2Fpom.xml&pieces=4", "").statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(request(path).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(request(path).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path));
    }
}
