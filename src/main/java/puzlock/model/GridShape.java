package puzlock.model;

/**
 * Dimensions of a voxel grid and the mapping between (x, y, z) cells and flat indices.
 * Voxel sets throughout Puzlock are {@link java.util.BitSet}s over these indices.
 */
public record GridShape(int sizeX, int sizeY, int sizeZ) {

    public static final int OUTSIDE = -1;

    public GridShape {
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive: " + sizeX + "x" + sizeY + "x" + sizeZ);
        }
    }

    public int cellCount() {
        return sizeX * sizeY * sizeZ;
    }

    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ;
    }

    public int index(int x, int y, int z) {
        return x + sizeX * (y + sizeY * z);
    }

    public int x(int index) {
        return index % sizeX;
    }

    public int y(int index) {
        return (index / sizeX) % sizeY;
    }

    public int z(int index) {
        return index / (sizeX * sizeY);
    }

    /** The cell one step from {@code index} along {@code direction}, or {@link #OUTSIDE}. */
    public int neighbour(int index, Direction direction) {
        int nx = x(index) + direction.dx();
        int ny = y(index) + direction.dy();
        int nz = z(index) + direction.dz();
        return inBounds(nx, ny, nz) ? index(nx, ny, nz) : OUTSIDE;
    }

    @Override
    public String toString() {
        return sizeX + "x" + sizeY + "x" + sizeZ;
    }
}
