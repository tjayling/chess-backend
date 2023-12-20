package org.logic.movegen;

import org.logic.model.BoardState;
import org.logic.model.MoveGeneratorState;
import org.util.BinUtil;

import static org.util.PrecomputedMoveData.*;

public class KnightGen {
    protected static void generateFriendlyKnightMoves(int startSquare, MoveGeneratorState moveGeneratorState) {
        long[] knightTargets = KNIGHT_TARGETS[startSquare];

        for (long target : knightTargets) {
            target &= moveGeneratorState.getNotFriendlyPieces();
            int targetSquare = BinUtil.getLastPosition(target);
            moveGeneratorState.addMove(startSquare, targetSquare);
        }
    }

    protected static void generateOpponentKnightMoves(int startSquare, MoveGeneratorState moveGeneratorState, BoardState boardState) {
        long[] knightTargets = KNIGHT_TARGETS[startSquare];

        for (long target : knightTargets) {
            moveGeneratorState.setTabooOrEquals(target);

            int targetSquare = BinUtil.getLastPosition(target);
            if (targetSquare == boardState.getFriendlyKingPosition()) {
                moveGeneratorState.addCheckingMove(startSquare, targetSquare);
            }
        }
    }
}
