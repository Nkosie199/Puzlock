package puzlock.export;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/** Writes the outer surface of a voxel set as a binary STL, two triangles per exposed voxel face. */
public final class StlWriter {
    /** Edge length of one voxel in the STL's units (millimetres for most slicers). */
    public static final float VOXEL_SIZE_MM = 10f;
    private static final int HEADER_BYTES = 80;
    private static final int TRIANGLE_BYTES = 50;

    private StlWriter() {
    }

    public static byte[] write(GridShape shape, BitSet voxels) {
        List<float[]> triangles = new ArrayList<>();
        for (int cell = voxels.nextSetBit(0); cell >= 0; cell = voxels.nextSetBit(cell + 1)) {
            for (Direction face : Direction.values()) {
                int next = shape.neighbour(cell, face);
                if (next == GridShape.OUTSIDE || !voxels.get(next)) {
                    addFace(triangles, shape.x(cell), shape.y(cell), shape.z(cell), face);
                }
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + 4 + triangles.size() * TRIANGLE_BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        byte[] header = new byte[HEADER_BYTES];
        byte[] label = "Puzlock puzzle piece".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(label, 0, header, 0, label.length);
        buffer.put(header).putInt(triangles.size());
        for (float[] triangle : triangles) {
            for (float value : triangle) {
                buffer.putFloat(value);
            }
            buffer.putShort((short) 0);
        }
        return buffer.array();
    }

    /** Adds the two triangles of one face, wound counter-clockwise seen from outside, normal first. */
    private static void addFace(List<float[]> triangles, int x, int y, int z, Direction face) {
        float[][] corners = faceCorners(x, y, z, face);
        float[] normal = {face.dx(), face.dy(), face.dz()};
        triangles.add(triangle(normal, corners[0], corners[1], corners[2]));
        triangles.add(triangle(normal, corners[0], corners[2], corners[3]));
    }

    private static float[][] faceCorners(int x, int y, int z, Direction face) {
        float s = VOXEL_SIZE_MM;
        float x0 = x * s, x1 = (x + 1) * s, y0 = y * s, y1 = (y + 1) * s, z0 = z * s, z1 = (z + 1) * s;
        return switch (face) {
            case RIGHT -> new float[][] {{x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}, {x1, y0, z1}};
            case LEFT -> new float[][] {{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}};
            case UP -> new float[][] {{x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}};
            case DOWN -> new float[][] {{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}};
            case FORWARD -> new float[][] {{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
            case BACKWARD -> new float[][] {{x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}, {x1, y0, z0}};
        };
    }

    private static float[] triangle(float[] normal, float[] a, float[] b, float[] c) {
        return new float[] {normal[0], normal[1], normal[2], a[0], a[1], a[2], b[0], b[1], b[2], c[0], c[1], c[2]};
    }
}
