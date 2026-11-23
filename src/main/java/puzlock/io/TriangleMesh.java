package puzlock.io;

import java.util.List;

/** A triangle soup. Each triangle is nine coordinates: x0 y0 z0 x1 y1 z1 x2 y2 z2. */
public record TriangleMesh(List<double[]> triangles) {

    public TriangleMesh {
        triangles = List.copyOf(triangles);
        if (triangles.isEmpty()) {
            throw new InvalidInputException("The mesh contains no triangles");
        }
    }

    public int triangleCount() {
        return triangles.size();
    }
}
