package org.logic.movegen;

import org.logic.BoardState;
import org.logic.MoveGeneratorState;
import org.util.BinUtil;

import static java.lang.Math.pow;
import static org.util.PieceUtil.WHITE;
import static org.util.PrecomputedMoveData.*;

public class PawnGen {

    static void generatePawnMoves(int start, boolean friendly, BoardState boardState, MoveGeneratorState moveGeneratorState) {


        long binStartSquare = (long) pow(2, start);

        // Taboo logic
        if (!friendly) {
            long attacks = boardState.getOpponentColour() == WHITE ? (BinUtil.northEastOne(binStartSquare) | BinUtil.northWestOne(binStartSquare)) : (
                    BinUtil.southEastOne(binStartSquare) | BinUtil.southWestOne(binStartSquare));

            moveGeneratorState.tabooOrEquals(attacks);
            for (int target : BinUtil.getPositionsFromBitboard(attacks)) {
                if (target == boardState.getFriendlyKingPosition()) {
                    moveGeneratorState.addCheckingMove(SQUARE_MAP.get(start) + SQUARE_MAP.get(target));
                }
            }
            return;
        }

        int friendlyColour = boardState.getFriendlyColour();
        long empty = moveGeneratorState.getEmpty();

        long singleTargets = friendlyColour == WHITE ? BinUtil.northOne(binStartSquare) & empty : BinUtil.southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? BinUtil.northOne(singleTargets) & empty & RANK_4 : BinUtil.southOne(singleTargets) & empty & RANK_5;
        long attacks = generatePawnAttacks(binStartSquare, friendlyColour, moveGeneratorState);

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(start, pawnTargets, moveGeneratorState);
    }

    private static long generatePawnAttacks(long binStartSquare, int friendlyColour, MoveGeneratorState moveGeneratorState) {
        if ((binStartSquare & moveGeneratorState.getPinnedPieceBitboard()) > 0) {
            return 0;
        }
        return friendlyColour == WHITE ? (
                BinUtil.northEastOne(binStartSquare) | BinUtil.northWestOne(binStartSquare)) & ~moveGeneratorState.getNotBlack() : (
                BinUtil.southEastOne(binStartSquare) | BinUtil.southWestOne(binStartSquare)) & ~moveGeneratorState.getNotWhite();
    }

    private static void addPawnMoves(int start, long pawnTargets, MoveGeneratorState moveGeneratorState) {
        for (int target = 0; target < 64; target++) {
            // If there is a bit switched on
            if (pawnTargets << ~target < 0) {
                if (target < 8 || target > 55) {
                    for (char c : promotionPieces) {
                        moveGeneratorState.addMove(SQUARE_MAP.get(start) + SQUARE_MAP.get(target) + c);
                    }
                    continue;
                }
                moveGeneratorState.addMove(start, target);
            }
        }
    }
}
