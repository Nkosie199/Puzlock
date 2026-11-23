package puzlock.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GridShapeTest {
    private final GridShape shape = new GridShape(4, 3, 2);

    @Test
    void indexRoundTripsCoordinates() {
        int index = shape.index(3, 2, 1);
        assertEquals(3, shape.x(index));
        assertEquals(2, shape.y(index));
        assertEquals(1, shape.z(index));
        assertEquals(24, shape.cellCount());
    }

    @Test
    void neighbourStepsAlongDirectionAndStopsAtEdges() {
        int corner = shape.index(0, 0, 0);
        assertEquals(shape.index(1, 0, 0), shape.neighbour(corner, Direction.RIGHT));
        assertEquals(shape.index(0, 1, 0), shape.neighbour(corner, Direction.UP));
        assertEquals(shape.index(0, 0, 1), shape.neighbour(corner, Direction.FORWARD));
        assertEquals(GridShape.OUTSIDE, shape.neighbour(corner, Direction.LEFT));
        assertEquals(GridShape.OUTSIDE, shape.neighbour(shape.index(3, 2, 1), Direction.UP));
    }

    @Test
    void rejectsEmptyDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new GridShape(0, 1, 1));
    }

    @Test
    void directionsKnowOppositesAndPerpendiculars() {
        for (Direction direction : Direction.values()) {
            assertEquals(direction, direction.opposite().opposite());
            assertEquals(4, direction.perpendiculars().size());
            assertFalse(direction.perpendiculars().contains(direction.opposite()));
        }
        assertTrue(Direction.UP.isPerpendicularTo(Direction.LEFT));
    }
}
