package org.logic.movegen;

import org.logic.model.BoardState;
import org.logic.model.MoveGeneratorState;
import org.util.BinUtil;

import static org.util.PrecomputedMoveData.*;

public class KingGen {

    public static void generateOpponentKingMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        long[] kingTargets = KING_MOVES[boardState.getOpponentKingPosition()];
        for (long targetSquareBitboard : kingTargets) {
            moveGeneratorState.setTabooOrEquals(targetSquareBitboard);
        }
    }

    public static void generateFriendlyKingMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int kingPosition = boardState.getFriendlyKingPosition();
        long[] kingTargets = KING_MOVES[kingPosition];

        for (long targetSquareBitboard : kingTargets) {
            targetSquareBitboard &= ~moveGeneratorState.getTaboo() & moveGeneratorState.getNotFriendlyPieces();
            int targetSquare = BinUtil.getPositionFromBitboard(targetSquareBitboard);
            addKingMove(kingPosition, targetSquare, moveGeneratorState);
        }
    }


    private static void addKingMove(int start, int target, MoveGeneratorState moveGeneratorState) {
        moveGeneratorState.addKingMove(start, target);
    }
}
