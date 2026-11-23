package puzlock.model;

import java.util.BitSet;

/** An immutable occupancy grid: the voxelised input shape. */
public final class VoxelGrid {
    private final GridShape shape;
    private final BitSet voxels;

    public VoxelGrid(GridShape shape, BitSet voxels) {
        if (voxels.length() > shape.cellCount()) {
            throw new IllegalArgumentException("Voxel set exceeds grid " + shape);
        }
        this.shape = shape;
        this.voxels = (BitSet) voxels.clone();
    }

    public GridShape shape() {
        return shape;
    }

    /** A copy of the occupied cells. */
    public BitSet voxels() {
        return (BitSet) voxels.clone();
    }

    public int voxelCount() {
        return voxels.cardinality();
    }

    public boolean isOccupied(int x, int y, int z) {
        return shape.inBounds(x, y, z) && voxels.get(shape.index(x, y, z));
    }
}
