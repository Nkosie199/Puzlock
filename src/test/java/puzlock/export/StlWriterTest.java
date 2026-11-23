package puzlock.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.BitSet;
import org.junit.jupiter.api.Test;
import puzlock.io.InputLoader;
import puzlock.io.StlReader;
import puzlock.model.GridShape;
import puzlock.model.VoxelGrid;

class StlWriterTest {

    @Test
    void writesOnlyExposedFaces() {
        GridShape shape = new GridShape(2, 1, 1);
        BitSet single = new BitSet();
        single.set(0);
        BitSet pair = new BitSet();
        pair.set(0, 2);
        assertEquals(12, StlReader.read(StlWriter.write(shape, single)).triangleCount());
        assertEquals(20, StlReader.read(StlWriter.write(shape, pair)).triangleCount());
    }

    @Test
    void writtenPiecesVoxeliseBackToTheSameShape() {
        GridShape shape = new GridShape(3, 3, 3);
        BitSet lShape = new BitSet();
        lShape.set(shape.index(0, 0, 0));
        lShape.set(shape.index(1, 0, 0));
        lShape.set(shape.index(2, 0, 0));
        lShape.set(shape.index(2, 1, 0));
        lShape.set(shape.index(2, 2, 0));
        VoxelGrid back = InputLoader.load("piece.stl", StlWriter.write(shape, lShape), 3).grid();
        assertEquals(new GridShape(3, 3, 1), back.shape());
        assertEquals(5, back.voxelCount());
    }
}
