package puzlock.io;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import puzlock.model.GridShape;
import puzlock.model.VoxelGrid;

/**
 * The text grid format shared with the 2019 renderer: one line per z slice, rows (y) separated
 * by commas, one character per x cell, '0' for empty and any other digit for a voxel.
 */
public final class GridFormat {

    private GridFormat() {
    }

    public static VoxelGrid read(String text) {
        List<String> slices = text.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
        if (slices.isEmpty()) {
            throw new InvalidInputException("The grid file is empty");
        }
        String[] firstRows = slices.get(0).split(",", -1);
        int sizeX = firstRows[0].length();
        int sizeY = firstRows.length;
        if (sizeX == 0) {
            throw new InvalidInputException("Malformed grid at line 1: row 1 is empty");
        }
        GridShape shape = new GridShape(sizeX, sizeY, slices.size());
        BitSet voxels = new BitSet(shape.cellCount());
        for (int z = 0; z < slices.size(); z++) {
            readSlice(slices.get(z), z, shape, voxels);
        }
        return new VoxelGrid(shape, voxels);
    }

    private static void readSlice(String slice, int z, GridShape shape, BitSet voxels) {
        String[] rows = slice.split(",", -1);
        if (rows.length != shape.sizeY()) {
            throw new InvalidInputException("Malformed grid at line " + (z + 1) + ": " + rows.length
                    + " rows, expected " + shape.sizeY());
        }
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            if (row.length() != shape.sizeX()) {
                throw new InvalidInputException("Malformed grid at line " + (z + 1) + ": row " + (y + 1) + " has "
                        + row.length() + " cells, expected " + shape.sizeX());
            }
            for (int x = 0; x < row.length(); x++) {
                char cell = row.charAt(x);
                if (!Character.isDigit(cell)) {
                    throw new InvalidInputException("Malformed grid at line " + (z + 1) + ": row " + (y + 1)
                            + " contains '" + cell + "', expected only digits");
                }
                if (cell != '0') {
                    voxels.set(shape.index(x, y, z));
                }
            }
        }
    }

    public static String write(GridShape shape, BitSet voxels) {
        StringBuilder text = new StringBuilder(shape.cellCount() + shape.sizeY() * shape.sizeZ());
        char[] row = new char[shape.sizeX()];
        for (int z = 0; z < shape.sizeZ(); z++) {
            for (int y = 0; y < shape.sizeY(); y++) {
                Arrays.fill(row, '0');
                for (int x = 0; x < shape.sizeX(); x++) {
                    if (voxels.get(shape.index(x, y, z))) {
                        row[x] = '1';
                    }
                }
                text.append(row);
                if (y + 1 < shape.sizeY()) {
                    text.append(',');
                }
            }
            if (z + 1 < shape.sizeZ()) {
                text.append('\n');
            }
        }
        return text.toString();
    }
}
