package org.logic.movegen;

import org.logic.model.BoardState;
import org.logic.model.MoveGeneratorState;
import org.util.BinUtil;

import static org.logic.model.BitboardMask.RANK_4;
import static org.logic.model.BitboardMask.RANK_5;
import static org.util.PieceUtil.WHITE;
import static org.util.PrecomputedMoveData.*;

public class PawnGen {

    static void generatePawnMoves(int startSquare, boolean friendly, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        long binStartSquare = BinUtil.bitboardFromPosition(startSquare);

        // Taboo logic
        if (!friendly) {
            long attacks = boardState.getOpponentColour() == WHITE
                           ? NORTH_EAST_ONE[startSquare] | NORTH_WEST_ONE[startSquare]
                           : SOUTH_EAST_ONE[startSquare] | SOUTH_WEST_ONE[startSquare];

            moveGeneratorState.setTabooOrEquals(attacks);
            for (int target : BinUtil.getPositions(attacks)) {
                if (target == boardState.getFriendlyKingPosition()) {
                    moveGeneratorState.addCheckingMove(SQUARE_MAP.get(startSquare) + SQUARE_MAP.get(target));
                }
            }
            return;
        }

        int friendlyColour = boardState.getFriendlyColour();
        long empty = moveGeneratorState.getEmpty();

        long singleTargets = friendlyColour == WHITE ? NORTH_ONE[startSquare] & empty : SOUTH_ONE[startSquare] & empty;
        long doubleTargets = friendlyColour == WHITE ? (singleTargets << 8) & empty & RANK_4 : (singleTargets >>> 8) & empty & RANK_5;
        long attacks = generatePawnAttacks(startSquare, binStartSquare, friendlyColour, moveGeneratorState);

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(startSquare, pawnTargets, moveGeneratorState);
    }

    private static long generatePawnAttacks(int startSquare, long binStartSquare, int friendlyColour, MoveGeneratorState moveGeneratorState) {
        if ((binStartSquare & moveGeneratorState.getPinnedPieceBitboard()) > 0) {
            return 0;
        }
        return friendlyColour == WHITE
               ? (NORTH_EAST_ONE[startSquare] | NORTH_WEST_ONE[startSquare]) & ~moveGeneratorState.getNotBlack()
               : (SOUTH_EAST_ONE[startSquare] | SOUTH_WEST_ONE[startSquare]) & ~moveGeneratorState.getNotWhite();
    }

    private static void addPawnMoves(int start, long pawnTargets, MoveGeneratorState moveGeneratorState) {
        for (int target = 0; target < 64; target++) {
            // If there is a bit switched on
            if (pawnTargets << ~target < 0) {
                if (target < 8 || target > 55) {
                    for (char c : PROMOTION_PIECES) {
                        moveGeneratorState.addMove(SQUARE_MAP.get(start) + SQUARE_MAP.get(target) + c);
                    }
                    continue;
                }
                moveGeneratorState.addMove(start, target);
            }
        }
    }
}
