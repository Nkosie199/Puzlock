package puzlock.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import puzlock.model.GridShape;
import puzlock.model.VoxelGrid;

/**
 * Converts a closed triangle mesh into voxels. A cell is solid when its centre is inside the
 * mesh. Inside-ness is decided by ray parity along each of the three axes and a majority vote,
 * which tolerates small holes; rays that cross an odd number of faces leak and do not vote.
 */
public final class Voxeliser {
    /** Above this share of leaking rays the mesh is too open to voxelise reliably. */
    static final double MAX_LEAK_RATIO = 0.10;
    private static final double RAY_JITTER = 1.234567e-4;

    private final TriangleMesh mesh;
    private final double[] origin = new double[3];
    private final int[] size = new int[3];
    private double cellSize;
    private int raysWithHits;
    private int leakingRays;

    private Voxeliser(TriangleMesh mesh, int resolution) {
        this.mesh = mesh;
        fitGrid(resolution);
    }

    /** @param resolution number of cells along the mesh's longest side */
    public static VoxelGrid voxelise(TriangleMesh mesh, int resolution) {
        if (resolution < 2) {
            throw new InvalidInputException("Voxel resolution must be at least 2, got " + resolution);
        }
        return new Voxeliser(mesh, resolution).run();
    }

    private void fitGrid(int resolution) {
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (double[] triangle : mesh.triangles()) {
            for (int i = 0; i < 9; i++) {
                min[i % 3] = Math.min(min[i % 3], triangle[i]);
                max[i % 3] = Math.max(max[i % 3], triangle[i]);
            }
        }
        double longest = Math.max(max[0] - min[0], Math.max(max[1] - min[1], max[2] - min[2]));
        if (!(longest > 0) || !Double.isFinite(longest)) {
            throw new InvalidInputException("The mesh has no volume (all vertices coincide or are not finite)");
        }
        cellSize = longest / resolution;
        for (int axis = 0; axis < 3; axis++) {
            double extent = max[axis] - min[axis];
            size[axis] = Math.max(1, (int) Math.ceil(extent / cellSize - 1e-9));
            origin[axis] = min[axis] - (size[axis] * cellSize - extent) / 2;
        }
    }

    private VoxelGrid run() {
        GridShape shape = new GridShape(size[0], size[1], size[2]);
        int[] insideVotes = new int[shape.cellCount()];
        int[] validVotes = new int[shape.cellCount()];
        for (int axis = 0; axis < 3; axis++) {
            castRays(axis, shape, insideVotes, validVotes);
        }
        if (raysWithHits > 0 && (double) leakingRays / raysWithHits > MAX_LEAK_RATIO) {
            throw new InvalidInputException(String.format(
                    "Mesh is not watertight (%.0f%% of scan rays leak); repair it or use a grid file",
                    100.0 * leakingRays / raysWithHits));
        }
        BitSet voxels = new BitSet(shape.cellCount());
        for (int cell = 0; cell < insideVotes.length; cell++) {
            if (insideVotes[cell] * 2 > validVotes[cell]) {
                voxels.set(cell);
            }
        }
        if (voxels.isEmpty()) {
            throw new InvalidInputException("The mesh produced no voxels at this resolution; try a higher resolution");
        }
        return new VoxelGrid(shape, voxels);
    }

    private void castRays(int axis, GridShape shape, int[] insideVotes, int[] validVotes) {
        int axisB = (axis + 1) % 3;
        int axisC = (axis + 2) % 3;
        List<List<double[]>> buckets = bucketTriangles(axisB, axisC);
        int[] cell = new int[3];
        for (int c = 0; c < size[axisC]; c++) {
            for (int b = 0; b < size[axisB]; b++) {
                double rayB = centre(axisB, b) + RAY_JITTER * cellSize;
                double rayC = centre(axisC, c) + 2 * RAY_JITTER * cellSize;
                double[] hits = intersections(buckets.get(b + c * size[axisB]), axis, axisB, axisC, rayB, rayC);
                if (hits.length == 0) {
                    continue;
                }
                raysWithHits++;
                boolean leaks = hits.length % 2 != 0;
                if (leaks) {
                    leakingRays++;
                }
                cell[axisB] = b;
                cell[axisC] = c;
                for (int a = 0; a < size[axis]; a++) {
                    cell[axis] = a;
                    int index = shape.index(cell[0], cell[1], cell[2]);
                    if (!leaks) {
                        validVotes[index]++;
                        if (crossingsBefore(hits, centre(axis, a)) % 2 == 1) {
                            insideVotes[index]++;
                        }
                    }
                }
            }
        }
    }

    private List<List<double[]>> bucketTriangles(int axisB, int axisC) {
        List<List<double[]>> buckets = new ArrayList<>(size[axisB] * size[axisC]);
        for (int i = 0; i < size[axisB] * size[axisC]; i++) {
            buckets.add(new ArrayList<>());
        }
        for (double[] triangle : mesh.triangles()) {
            int fromB = firstCellAtOrAbove(axisB, min(triangle, axisB));
            int toB = lastCellAtOrBelow(axisB, max(triangle, axisB));
            int fromC = firstCellAtOrAbove(axisC, min(triangle, axisC));
            int toC = lastCellAtOrBelow(axisC, max(triangle, axisC));
            for (int c = fromC; c <= toC; c++) {
                for (int b = fromB; b <= toB; b++) {
                    buckets.get(b + c * size[axisB]).add(triangle);
                }
            }
        }
        return buckets;
    }

    private static double[] intersections(List<double[]> triangles, int axis, int axisB, int axisC,
                                          double rayB, double rayC) {
        double[] hits = new double[triangles.size()];
        int count = 0;
        for (double[] t : triangles) {
            double b0 = t[axisB], c0 = t[axisC];
            double b1 = t[3 + axisB], c1 = t[3 + axisC];
            double b2 = t[6 + axisB], c2 = t[6 + axisC];
            double area = (b1 - b0) * (c2 - c0) - (b2 - b0) * (c1 - c0);
            if (Math.abs(area) < 1e-18) {
                continue;
            }
            double w0 = ((b1 - rayB) * (c2 - rayC) - (b2 - rayB) * (c1 - rayC)) / area;
            double w1 = ((b2 - rayB) * (c0 - rayC) - (b0 - rayB) * (c2 - rayC)) / area;
            double w2 = 1 - w0 - w1;
            if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                hits[count++] = w0 * t[axis] + w1 * t[3 + axis] + w2 * t[6 + axis];
            }
        }
        double[] result = Arrays.copyOf(hits, count);
        Arrays.sort(result);
        return result;
    }

    private static int crossingsBefore(double[] sortedHits, double position) {
        int index = Arrays.binarySearch(sortedHits, position);
        return index >= 0 ? index : -index - 1;
    }

    private double centre(int axis, int cellIndex) {
        return origin[axis] + (cellIndex + 0.5) * cellSize;
    }

    private int firstCellAtOrAbove(int axis, double value) {
        return Math.max(0, (int) Math.ceil((value - origin[axis]) / cellSize - 0.5 - 1e-3));
    }

    private int lastCellAtOrBelow(int axis, double value) {
        return Math.min(size[axis] - 1, (int) Math.floor((value - origin[axis]) / cellSize - 0.5 + 1e-3));
    }

    private static double min(double[] triangle, int axis) {
        return Math.min(triangle[axis], Math.min(triangle[3 + axis], triangle[6 + axis]));
    }

    private static double max(double[] triangle, int axis) {
        return Math.max(triangle[axis], Math.max(triangle[3 + axis], triangle[6 + axis]));
    }
}
