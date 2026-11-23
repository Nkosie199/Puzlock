package puzlock.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import puzlock.model.GridShape;
import puzlock.model.VoxelGrid;

class GridFormatTest {

    @Test
    void readsOneLinePerZRowsPerCommaCellsPerCharacter() {
        VoxelGrid grid = GridFormat.read("10,00\n00,01\n");
        assertEquals(new GridShape(2, 2, 2), grid.shape());
        assertEquals(2, grid.voxelCount());
        assertTrue(grid.isOccupied(0, 0, 0));
        assertTrue(grid.isOccupied(1, 1, 1));
    }

    @Test
    void roundTripsPaddedOffCentreNonCubicGrids() {
        String text = "00000,00000,00000\n00110,00110,00000\n00000,00100,00000\n00000,00000,00000";
        VoxelGrid grid = GridFormat.read(text);
        assertEquals(new GridShape(5, 3, 4), grid.shape());
        assertEquals(5, grid.voxelCount());
        assertEquals(text, GridFormat.write(grid.shape(), grid.voxels()));
    }

    @Test
    void readsTheTwentyNineteenSampleFormat() {
        VoxelGrid grid = GridFormat.read("1111,1111,1111,1111\n".repeat(4));
        assertEquals(64, grid.voxelCount());
    }

    @Test
    void reportsRaggedRowsWithLineNumbers() {
        InvalidInputException error = assertThrows(InvalidInputException.class, () -> GridFormat.read("11,11\n11,1\n"));
        assertEquals("Malformed grid at line 2: row 2 has 1 cells, expected 2", error.getMessage());
    }

    @Test
    void rejectsWrongRowCountsNonDigitsAndEmptyText() {
        assertThrows(InvalidInputException.class, () -> GridFormat.read("11,11\n11\n"));
        assertThrows(InvalidInputException.class, () -> GridFormat.read("1x,11\n"));
        assertThrows(InvalidInputException.class, () -> GridFormat.read("\n\n"));
    }
}
