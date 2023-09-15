package org.logic.movegen;

public enum Direction {
    NORTH(0),
    SOUTH(1),
    EAST(2),
    WEST(3),
    SOUTH_EAST(4),
    NORTH_WEST(5),
    SOUTH_WEST(6),
    NORTH_EAST(7);

    private final int value;

    Direction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}




