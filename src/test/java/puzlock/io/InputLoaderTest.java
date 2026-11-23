package puzlock.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import puzlock.model.Connectivity;
import puzlock.model.GridShape;
import puzlock.model.VoxelGrid;

class InputLoaderTest {

    @Test
    void voxelisesAUnitCubeObjIntoAFullBox() {
        LoadedInput input = InputLoader.load("cube.obj", MeshReadersTest.CUBE_OBJ.getBytes(StandardCharsets.UTF_8), 4);
        assertEquals(new GridShape(4, 4, 4), input.grid().shape());
        assertEquals(64, input.grid().voxelCount());
    }

    @Test
    void voxelisesTheSphereSampleCloseToItsTrueVolume() throws IOException {
        VoxelGrid grid = InputLoader.load(Path.of("samples/sphere.stl"), 16).grid();
        double radius = 8;
        double expected = 4.0 / 3.0 * Math.PI * radius * radius * radius;
        assertEquals(expected, grid.voxelCount(), expected * 0.10);
        assertTrue(Connectivity.isConnected(grid.shape(), grid.voxels()));
    }

    @Test
    void dropsLooseCornerVoxelsFromMeshesAndSaysSo() throws IOException {
        LoadedInput input = InputLoader.load(Path.of("samples/bunny.stl"), 16);
        assertTrue(input.note().orElseThrow().startsWith("Dropped 2 loose voxels"));
        assertTrue(Connectivity.isConnected(input.grid().shape(), input.grid().voxels()));
    }

    @Test
    void rejectsAMeshWithAHoleInIt() {
        String openBox = MeshReadersTest.CUBE_OBJ.replace("f 5 6 7 8\n", "");
        InvalidInputException error = assertThrows(InvalidInputException.class,
                () -> InputLoader.load("open.obj", openBox.getBytes(StandardCharsets.UTF_8), 8));
        assertTrue(error.getMessage().startsWith("Mesh is not watertight"), error.getMessage());
    }

    @Test
    void rejectsUnknownFormatsAndEmptyFiles() {
        InvalidInputException prose = assertThrows(InvalidInputException.class,
                () -> InputLoader.load("notes.txt", "Hello, this is not a shape".getBytes(StandardCharsets.UTF_8), 8));
        assertEquals("Unsupported input \"notes.txt\": expected a Puzlock grid, .stl or .obj file", prose.getMessage());
        assertThrows(InvalidInputException.class, () -> InputLoader.load("empty.grid", new byte[0], 8));
    }
}
