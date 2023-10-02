package org.logic.movegen;

import static org.util.PrecomputedMoveData.NOT_AB_FILE;
import static org.util.PrecomputedMoveData.NOT_A_FILE;
import static org.util.PrecomputedMoveData.NOT_GH_FILE;
import static org.util.PrecomputedMoveData.NOT_H_FILE;
import static org.util.PrecomputedMoveData.SQUARE_MAP;

import org.logic.model.MoveGeneratorState;

public class KnightGen {
  protected static void generateKnightMoves(int start, boolean friendly, int friendlyKingPosition, MoveGeneratorState moveGeneratorState) {
    long binStartSquare = 1L << start;

    long knightMoves = 0L;
    knightMoves |= ((binStartSquare << 17) & NOT_A_FILE);
    knightMoves |= ((binStartSquare << 10) & NOT_AB_FILE);
    knightMoves |= ((binStartSquare >>> 6) & NOT_AB_FILE);
    knightMoves |= ((binStartSquare >>> 15) & NOT_A_FILE);
    knightMoves |= ((binStartSquare << 15) & NOT_H_FILE);
    knightMoves |= ((binStartSquare << 6) & NOT_GH_FILE);
    knightMoves |= ((binStartSquare >>> 10) & NOT_GH_FILE);
    knightMoves |= ((binStartSquare >>> 17) & NOT_H_FILE);


//        long knightMoves = KNIGHT_MOVES[start];

    if (friendly) {
      knightMoves &= moveGeneratorState.getNotFriendlyPieces();
    }

    while (knightMoves != 0) {
      int target = Long.numberOfTrailingZeros(knightMoves);
      long targetBit = 1L << target;
      knightMoves ^= targetBit;
      if (friendly) {
        moveGeneratorState.addMove(start, target);
        continue;
      }

      moveGeneratorState.setTabooOrEquals(targetBit);

      if (target == friendlyKingPosition) {
        moveGeneratorState.addCheckingMove(SQUARE_MAP.get(start) + SQUARE_MAP.get(target));
      }
    }
  }
}
