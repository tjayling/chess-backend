package org.util;


import static org.logic.PrecomputedMoveData.squareMap;

public class MoveUtil {

    public static int getStartSquare(String move) {
        return (squareMap.indexOf(move.substring(0, 2)));
    }

    public static int getTargetSquare(String move) {
        return (squareMap.indexOf(move.substring(2, 4)));
    }
}