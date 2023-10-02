package org.logic.movegen;

import org.logic.model.BoardState;
import org.logic.model.MoveGeneratorState;

import static org.util.PieceUtil.*;
import static org.util.PieceUtil.isColour;
import static org.util.PrecomputedMoveData.*;
import static org.util.PrecomputedMoveData.SQUARE_MAP;

public class SlidingGen {
    public static void generateOpponentSlidingMoves(int start, int piece, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int startDirIndex = isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = isType(piece, ROOK) ? 4 : 8;

        boolean moveBlocked;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            moveBlocked = false;
            for (int n = 0; n < NUM_SQUARES_TO_EDGE[start][directionIndex]; n++) {
                int target = start + DIRECTION_OFFSETS[directionIndex] * (n + 1);
                int pieceOnTargetSquare = boardState.getSquare(target);

                if (!moveBlocked) {
                    moveGeneratorState.addTabooBit(target);
                    if (target == boardState.getFriendlyKingPosition()) {
                        moveGeneratorState.addCheckingMove(SQUARE_MAP.get(start) + SQUARE_MAP.get(target));
                    }
                }
                if (pieceOnTargetSquare > 0 && !(isType(pieceOnTargetSquare, KING) && isColour(pieceOnTargetSquare, boardState.getFriendlyColour()))) {
                    moveBlocked = true;
                }
                moveGeneratorState.addTabooXRayBit(target);
            }
        }
    }

    public static void generateFriendlySlidingMoves(int start, int piece, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int startDirIndex = isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = isType(piece, ROOK) ? 4 : 8;

        boolean moveBlocked;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            moveBlocked = false;
            for (int n = 0; n < NUM_SQUARES_TO_EDGE[start][directionIndex]; n++) {
                int target = start + DIRECTION_OFFSETS[directionIndex] * (n + 1);
                int pieceOnTargetSquare = boardState.getSquare(target);

                if (isColour(pieceOnTargetSquare, boardState.getFriendlyColour())) {
                    moveBlocked = true;
                }
                if (!moveBlocked) {
                    moveGeneratorState.addMove(start, target);
                }
                if (isColour(pieceOnTargetSquare, boardState.getOpponentColour())) {
                    moveBlocked = true;
                }
            }
        }
    }
}
