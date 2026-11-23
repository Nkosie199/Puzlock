package puzlock.export;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import puzlock.model.Direction;
import puzlock.model.GridShape;

/**
 * Writes the outer surface of a voxel set as a binary STL.
 * <p>
 * Printed parts come out slightly larger than modelled, so every surface is moved inward by a
 * clearance; two touching pieces end up 2 × clearance apart. To shrink the piece exactly, each
 * voxel is split along every axis into a thin slab of the clearance's width on both sides and a
 * core. A sub-cell is kept only when every voxel within the clearance of it belongs to the piece,
 * and the surface of the kept sub-cells is written out. Because that surface bounds a union of
 * boxes it is always closed, including at inner corners.
 */
public final class StlWriter {
    /** Edge length of one voxel in the STL's units (millimetres for most slicers). */
    public static final float VOXEL_SIZE_MM = 10f;
    /** Inward offset per face; 0.15 mm leaves a 0.3 mm gap between pieces, typical for FDM printers. */
    public static final float DEFAULT_CLEARANCE_MM = 0.15f;
    private static final int HEADER_BYTES = 80;
    private static final int TRIANGLE_BYTES = 50;

    private final GridShape voxelShape;
    private final BitSet voxels;
    private final float clearance;
    private final int split;
    private final GridShape cellShape;
    private final BitSet solidCells;

    private StlWriter(GridShape voxelShape, BitSet voxels, float clearance) {
        this.voxelShape = voxelShape;
        this.voxels = voxels;
        this.clearance = clearance;
        this.split = clearance > 0 ? 3 : 1;
        this.cellShape = new GridShape(voxelShape.sizeX() * split, voxelShape.sizeY() * split, voxelShape.sizeZ() * split);
        this.solidCells = solidCells();
    }

    public static byte[] write(GridShape shape, BitSet voxels) {
        return write(shape, voxels, DEFAULT_CLEARANCE_MM);
    }

    /** @param clearanceMm inward offset per face, from 0 up to (not including) half a voxel */
    public static byte[] write(GridShape shape, BitSet voxels, float clearanceMm) {
        if (!(clearanceMm >= 0 && clearanceMm < VOXEL_SIZE_MM / 2)) {
            throw new IllegalArgumentException("Clearance must be at least 0 and under " + VOXEL_SIZE_MM / 2 + " mm, got "
                    + clearanceMm);
        }
        return new StlWriter(shape, voxels, clearanceMm).encode();
    }

    /** Sub-cells whose clearance-wide neighbourhood lies entirely inside the piece. */
    private BitSet solidCells() {
        BitSet solid = new BitSet(cellShape.cellCount());
        for (int voxel = voxels.nextSetBit(0); voxel >= 0; voxel = voxels.nextSetBit(voxel + 1)) {
            int[] origin = {voxelShape.x(voxel) * split, voxelShape.y(voxel) * split, voxelShape.z(voxel) * split};
            for (int dz = 0; dz < split; dz++) {
                for (int dy = 0; dy < split; dy++) {
                    for (int dx = 0; dx < split; dx++) {
                        int[] cell = {origin[0] + dx, origin[1] + dy, origin[2] + dz};
                        if (surroundedByPiece(cell)) {
                            solid.set(cellShape.index(cell[0], cell[1], cell[2]));
                        }
                    }
                }
            }
        }
        return solid;
    }

    /** A border slab touches the next voxel along its axis; every combination of those voxels must be solid. */
    private boolean surroundedByPiece(int[] cell) {
        int[][] reach = new int[3][];
        int[] voxel = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            voxel[axis] = cell[axis] / split;
            int slab = cell[axis] % split;
            reach[axis] = split == 1 || slab == 1 ? new int[] {0} : new int[] {0, slab == 0 ? -1 : 1};
        }
        for (int ox : reach[0]) {
            for (int oy : reach[1]) {
                for (int oz : reach[2]) {
                    int x = voxel[0] + ox, y = voxel[1] + oy, z = voxel[2] + oz;
                    if (!voxelShape.inBounds(x, y, z) || !voxels.get(voxelShape.index(x, y, z))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private byte[] encode() {
        List<float[]> triangles = new ArrayList<>();
        for (int cell = solidCells.nextSetBit(0); cell >= 0; cell = solidCells.nextSetBit(cell + 1)) {
            for (Direction face : Direction.values()) {
                int next = cellShape.neighbour(cell, face);
                if (next == GridShape.OUTSIDE || !solidCells.get(next)) {
                    addFace(triangles, cellShape.x(cell), cellShape.y(cell), cellShape.z(cell), face);
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
    private void addFace(List<float[]> triangles, int x, int y, int z, Direction face) {
        float x0 = edge(x), x1 = edge(x + 1), y0 = edge(y), y1 = edge(y + 1), z0 = edge(z), z1 = edge(z + 1);
        float[][] corners = switch (face) {
            case RIGHT -> new float[][] {{x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}, {x1, y0, z1}};
            case LEFT -> new float[][] {{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}};
            case UP -> new float[][] {{x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}};
            case DOWN -> new float[][] {{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}};
            case FORWARD -> new float[][] {{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
            case BACKWARD -> new float[][] {{x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}, {x1, y0, z0}};
        };
        float[] normal = {face.dx(), face.dy(), face.dz()};
        triangles.add(triangle(normal, corners[0], corners[1], corners[2]));
        triangles.add(triangle(normal, corners[0], corners[2], corners[3]));
    }

    /** Position of the lower boundary of sub-cell {@code index} along any axis, in millimetres. */
    private float edge(int index) {
        int voxel = index / split;
        int slab = index % split;
        float start = voxel * VOXEL_SIZE_MM;
        return switch (slab) {
            case 0 -> start;
            case 1 -> start + clearance;
            default -> start + VOXEL_SIZE_MM - clearance;
        };
    }

    private static float[] triangle(float[] normal, float[] a, float[] b, float[] c) {
        return new float[] {normal[0], normal[1], normal[2], a[0], a[1], a[2], b[0], b[1], b[2], c[0], c[1], c[2]};
    }
}
