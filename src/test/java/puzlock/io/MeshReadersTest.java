package puzlock.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MeshReadersTest {

    static final String CUBE_OBJ = """
            v 0 0 0
            v 1 0 0
            v 1 1 0
            v 0 1 0
            v 0 0 1
            v 1 0 1
            v 1 1 1
            v 0 1 1
            f 1 4 3 2
            f 5 6 7 8
            f 1 2 6 5
            f 2 3 7 6
            f 3 4 8 7
            f 4 1 5 8
            """;

    @Test
    void readsAsciiStl() {
        String stl = """
                solid t
                  facet normal 0 0 1
                    outer loop
                      vertex 0 0 0
                      vertex 1 0 0
                      vertex 0 1.5 0
                    endloop
                  endfacet
                endsolid t
                """;
        TriangleMesh mesh = StlReader.read(stl.getBytes(StandardCharsets.US_ASCII));
        assertEquals(1, mesh.triangleCount());
        assertArrayEquals(new double[] {0, 0, 0, 1, 0, 0, 0, 1.5, 0}, mesh.triangles().get(0));
    }

    @Test
    void rejectsFilesThatAreNotStl() {
        assertThrows(InvalidInputException.class, () -> StlReader.read("hello world".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void readsObjFacesAndFansPolygons() {
        assertEquals(12, ObjReader.read(CUBE_OBJ).triangleCount());
    }

    @Test
    void readsObjReferencesWithTexturesAndNegativeIndices() {
        TriangleMesh mesh = ObjReader.read("v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1/1/1 2/2/2 -1\n");
        assertArrayEquals(new double[] {0, 0, 0, 1, 0, 0, 0, 1, 0}, mesh.triangles().get(0));
    }

    @Test
    void reportsBadObjReferences() {
        InvalidInputException error = assertThrows(InvalidInputException.class, () -> ObjReader.read("v 0 0 0\nf 1 2 3\n"));
        assertEquals("Malformed OBJ at line 2: bad vertex reference '2'", error.getMessage());
    }
}
