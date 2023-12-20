package org.util;

import static org.logic.model.BitboardMask.NOT_A;
import static org.logic.model.BitboardMask.NOT_H;

public class DirectionUtil {
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
