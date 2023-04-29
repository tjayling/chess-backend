package org.util;


import static org.logic.PrecomputedMoveData.SQUARE_MAP;

public class MoveUtil {

    public static int getStartSquare(String move) {
        return SQUARE_MAP.indexOf(move.substring(0, 2));
    }

    public static int getTargetSquare(String move) {
        return SQUARE_MAP.indexOf(move.substring(2, 4));
    }
}