package puzlock.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads binary and ASCII STL files. */
public final class StlReader {
    private static final int HEADER_BYTES = 80;
    private static final int TRIANGLE_BYTES = 50;
    private static final Pattern VERTEX = Pattern.compile(
            "vertex\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

    private StlReader() {
    }

    public static TriangleMesh read(byte[] content) {
        if (isBinary(content)) {
            return readBinary(content);
        }
        return readAscii(new String(content, StandardCharsets.US_ASCII));
    }

    private static boolean isBinary(byte[] content) {
        if (content.length < HEADER_BYTES + 4) {
            return false;
        }
        long triangles = Integer.toUnsignedLong(littleEndian(content).getInt(HEADER_BYTES));
        return content.length == HEADER_BYTES + 4 + triangles * TRIANGLE_BYTES;
    }

    private static TriangleMesh readBinary(byte[] content) {
        ByteBuffer buffer = littleEndian(content);
        int count = buffer.getInt(HEADER_BYTES);
        List<double[]> triangles = new ArrayList<>(count);
        for (int t = 0; t < count; t++) {
            int vertexStart = HEADER_BYTES + 4 + t * TRIANGLE_BYTES + 12;
            double[] triangle = new double[9];
            for (int c = 0; c < 9; c++) {
                triangle[c] = buffer.getFloat(vertexStart + c * 4);
            }
            triangles.add(triangle);
        }
        return new TriangleMesh(triangles);
    }

    private static TriangleMesh readAscii(String text) {
        if (!text.stripLeading().regionMatches(true, 0, "solid", 0, 5)) {
            throw new InvalidInputException("Not a valid STL file: it is neither binary STL nor ASCII STL");
        }
        List<Double> coordinates = new ArrayList<>();
        Matcher vertex = VERTEX.matcher(text);
        while (vertex.find()) {
            for (int group = 1; group <= 3; group++) {
                coordinates.add(parseCoordinate(vertex.group(group)));
            }
        }
        if (coordinates.size() % 9 != 0) {
            throw new InvalidInputException("Malformed ASCII STL: a facet does not have exactly 3 vertices");
        }
        List<double[]> triangles = new ArrayList<>(coordinates.size() / 9);
        for (int start = 0; start < coordinates.size(); start += 9) {
            double[] triangle = new double[9];
            for (int c = 0; c < 9; c++) {
                triangle[c] = coordinates.get(start + c);
            }
            triangles.add(triangle);
        }
        return new TriangleMesh(triangles);
    }

    private static double parseCoordinate(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Malformed ASCII STL: '" + token + "' is not a number");
        }
    }

    private static ByteBuffer littleEndian(byte[] content) {
        return ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN);
    }
}
