package puzlock.io;

import java.util.ArrayList;
import java.util.List;

/** Reads the geometry of Wavefront OBJ files ({@code v} and {@code f} records; polygons are fanned). */
public final class ObjReader {

    private ObjReader() {
    }

    public static TriangleMesh read(String text) {
        List<double[]> vertices = new ArrayList<>();
        List<double[]> triangles = new ArrayList<>();
        int lineNumber = 0;
        for (String line : text.lines().toList()) {
            lineNumber++;
            String[] tokens = line.strip().split("\\s+");
            switch (tokens[0]) {
                case "v" -> vertices.add(parseVertex(tokens, lineNumber));
                case "f" -> addFace(tokens, vertices, triangles, lineNumber);
                default -> { }
            }
        }
        return new TriangleMesh(triangles);
    }

    private static double[] parseVertex(String[] tokens, int lineNumber) {
        if (tokens.length < 4) {
            throw new InvalidInputException("Malformed OBJ at line " + lineNumber + ": a vertex needs 3 coordinates");
        }
        try {
            return new double[] {
                Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])
            };
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Malformed OBJ at line " + lineNumber + ": vertex coordinates must be numbers");
        }
    }

    private static void addFace(String[] tokens, List<double[]> vertices, List<double[]> triangles, int lineNumber) {
        if (tokens.length < 4) {
            throw new InvalidInputException("Malformed OBJ at line " + lineNumber + ": a face needs at least 3 vertices");
        }
        double[] first = vertex(tokens[1], vertices, lineNumber);
        for (int corner = 2; corner + 1 < tokens.length; corner++) {
            double[] second = vertex(tokens[corner], vertices, lineNumber);
            double[] third = vertex(tokens[corner + 1], vertices, lineNumber);
            triangles.add(new double[] {
                first[0], first[1], first[2], second[0], second[1], second[2], third[0], third[1], third[2]
            });
        }
    }

    private static double[] vertex(String reference, List<double[]> vertices, int lineNumber) {
        try {
            int index = Integer.parseInt(reference.split("/")[0]);
            int resolved = index < 0 ? vertices.size() + index : index - 1;
            return vertices.get(resolved);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new InvalidInputException("Malformed OBJ at line " + lineNumber + ": bad vertex reference '" + reference + "'");
        }
    }
}
