package org.util;

import static org.logic.model.BitboardMask.NOT_A;
import static org.logic.model.BitboardMask.NOT_H;
import static org.logic.movegen.Direction.EAST;
import static org.logic.movegen.Direction.NORTH;
import static org.logic.movegen.Direction.NORTH_EAST;
import static org.logic.movegen.Direction.NORTH_WEST;
import static org.logic.movegen.Direction.SOUTH;
import static org.logic.movegen.Direction.SOUTH_EAST;
import static org.logic.movegen.Direction.SOUTH_WEST;
import static org.logic.movegen.Direction.WEST;

import org.logic.movegen.Direction;

public class DirectionUtil {


  /**
   * @param xDist the x distance between pinned piece and king.
   * @param yDist the y distance between pinned piece and king.
   * @return an index referring to the direction offsets as defined in PrecomputedMoveData.
   * @see PrecomputedMoveData
   */
  public static Direction getDirectionToTarget(int xDist, int yDist) {
    if (xDist == 0 && yDist < 0) {
      return Direction.NORTH; // North
    }
    if (xDist == 0 && yDist > 0) {
      return SOUTH; // South
    }
    if (xDist > 0 && yDist == 0) {
      return EAST; // East
    }
    if (xDist < 0 && yDist == 0) {
      return WEST; // West
    }
    if (xDist > 0 && yDist < 0) {
      return SOUTH_EAST; // South East
    }
    if (xDist < 0 && yDist > 0) {
      return NORTH_WEST; // North West
    }
    if (xDist < 0) {
      return SOUTH_WEST; // South West
    }
    if (xDist > 0) {
      return NORTH_EAST; // North East
    }
    throw new RuntimeException(String.format("There was an error in direction to king method. xDist: %s, yDist: %s", xDist, yDist));
  }

  public static Direction getOppositeDirection(Direction direction) {
    return switch (direction) {
      case NORTH -> SOUTH;
      case SOUTH -> NORTH;
      case EAST -> WEST;
      case WEST -> EAST;
      case SOUTH_EAST -> NORTH_WEST;
      case NORTH_WEST -> SOUTH_EAST;
      case SOUTH_WEST -> NORTH_EAST;
      case NORTH_EAST -> SOUTH_WEST;
    };
  }

  protected static long northOne(long bit) {
    return bit << 8;
  }

  protected static long northEastOne(long bit) {
    return (bit << 9) & NOT_A;
  }

  protected static long eastOne(long bit) {
    return (bit << 1) & NOT_A;
  }

  protected static long southEastOne(long bit) {
    return (bit >>> 7) & NOT_A;
  }

  protected static long southOne(long bit) {
    return bit >>> 8;
  }

  protected static long southWestOne(long bit) {
    return (bit >>> 9) & NOT_H;
  }

  protected static long westOne(long bit) {
    return (bit >>> 1) & NOT_H;
  }

  protected static long northWestOne(long bit) {
    return (bit << 7) & NOT_H;
  }
}
