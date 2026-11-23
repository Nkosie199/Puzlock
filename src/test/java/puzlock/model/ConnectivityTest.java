package puzlock.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import org.junit.jupiter.api.Test;

class ConnectivityTest {
    private final GridShape shape = new GridShape(3, 3, 1);

    @Test
    void faceNeighboursAreConnectedButDiagonalsAreNot() {
        BitSet line = cells(0, 0, 1, 0, 2, 0);
        BitSet diagonal = cells(0, 0, 1, 1);
        assertTrue(Connectivity.isConnected(shape, line));
        assertFalse(Connectivity.isConnected(shape, diagonal));
        assertEquals(2, Connectivity.componentCount(shape, diagonal));
    }

    @Test
    void emptySetIsNotConnected() {
        assertFalse(Connectivity.isConnected(shape, new BitSet()));
        assertEquals(0, Connectivity.componentCount(shape, new BitSet()));
    }

    private BitSet cells(int... xy) {
        BitSet set = new BitSet();
        for (int i = 0; i < xy.length; i += 2) {
            set.set(shape.index(xy[i], xy[i + 1], 0));
        }
        return set;
    }
}
