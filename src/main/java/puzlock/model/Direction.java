package puzlock.model;

import java.util.Arrays;
import java.util.List;

/** The six axis-aligned translation directions. Up is +y. */
public enum Direction {
    RIGHT(1, 0, 0),
    LEFT(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    FORWARD(0, 0, 1),
    BACKWARD(0, 0, -1);

    private final int dx;
    private final int dy;
    private final int dz;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public int dz() {
        return dz;
    }

    public Direction opposite() {
        return switch (this) {
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
            case UP -> DOWN;
            case DOWN -> UP;
            case FORWARD -> BACKWARD;
            case BACKWARD -> FORWARD;
        };
    }

    public boolean isPerpendicularTo(Direction other) {
        return dx * other.dx + dy * other.dy + dz * other.dz == 0;
    }

    public List<Direction> perpendiculars() {
        return Arrays.stream(values()).filter(this::isPerpendicularTo).toList();
    }

    public String label() {
        return name().toLowerCase();
    }
}
