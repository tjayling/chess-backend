package org.logic.model;

import java.util.ArrayList;
import java.util.List;

import static org.util.BinUtil.addBit;
import static org.util.MoveUtil.getStartSquare;
import static org.util.PieceUtil.*;
import static org.util.PrecomputedMoveData.SQUARE_MAP;

public class MoveGeneratorState {
    private final long empty;
    private final long notWhite;
    private final long notBlack;
    private final long notFriendlyPieces;
    private long pinnedPieceBitboard = 0;
    private long taboo;
    private long tabooXRay;
    private List<String> moves;
    private final List<String> kingMoves;
    private final List<String> checkingMoves;

    public MoveGeneratorState(BoardState boardState) {
        moves = new ArrayList<>();
        kingMoves = new ArrayList<>();
        checkingMoves = new ArrayList<>();

//        long tempNotWhite = new Bitboard();
        long tempNotWhite = 0L;
        long tempNotBlack = 0L;
        for (int i = 0; i < 64; i++) {
            int piece = boardState.getSquare(i);
            if (!isColour(piece, WHITE)) {
                tempNotWhite = addBit(tempNotWhite, i);
            }
            if (!isColour(piece, BLACK)) {
                tempNotBlack = addBit(tempNotBlack, i);
            }
        }
        notWhite = tempNotWhite;
        notBlack = tempNotBlack;
        notFriendlyPieces = boardState.getFriendlyColour() == WHITE ? notWhite : notBlack;
        empty = notWhite & notBlack;
    }

    /**
     * Adds a move to the moves list from the start and target squares.
     *
     * @param start  the start square of the move
     * @param target the target square of the move=
     */
    public void addMove(int start, int target) {
        if (target < 0 || target > 63) {
            return;
        }
        moves.add(SQUARE_MAP.get(start) + SQUARE_MAP.get(target));
    }

    public void addKingMove(int start, int target) {
        if (target < 0 || target > 63) {
            return;
        }
        String startSquare = SQUARE_MAP.get(start);
        String targetSquare = SQUARE_MAP.get(target);
        moves.add(startSquare + targetSquare);
        kingMoves.add(startSquare + targetSquare);
    }

    public void removeAllFromMoves(List<String> movesToRemove) {
        moves.removeAll(movesToRemove);
    }

    public void removeMovesFromStartSquare(int position) {
        List<String> movesToRemove = new ArrayList<>();

        for (String move : moves) {
            if (getStartSquare(move) == position) {
                movesToRemove.add(move);
            }
        }
        removeAllFromMoves(movesToRemove);
    }

    public long getPinnedPieceBitboard() {
        return pinnedPieceBitboard;
    }

    public long getEmpty() {
        return empty;
    }

    public long getNotWhite() {
        return notWhite;
    }

    public long getNotBlack() {
        return notBlack;
    }

    public long getNotFriendlyPieces() {
        return notFriendlyPieces;
    }

    public long getTaboo() {
        return taboo;
    }

    public long getTabooXRay() {
        return tabooXRay;
    }

    public List<String> getMoves() {
        return moves;
    }

    public List<String> getKingMoves() {
        return kingMoves;
    }

    public List<String> getCheckingMoves() {
        return checkingMoves;
    }

    public void setTaboo(long taboo) {
        this.taboo = taboo;
    }

    public void setTabooXRay(long tabooXRay) {
        this.tabooXRay = tabooXRay;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public void setTabooOrEquals(long bitboard) {
        taboo |= bitboard;
    }

    public void addMove(String move) {
        moves.add(move);
    }

    public void addCheckingMove(String move) {
        checkingMoves.add(move);
    }

    public void addCheckingMove(int startSquare, int targetSquare) {
        checkingMoves.add(SQUARE_MAP.get(startSquare) + SQUARE_MAP.get(targetSquare));
    }

    public void addTabooBit(int target) {
        taboo = addBit(taboo, target);
    }

    public void addTabooXRayBit(int target) {
        tabooXRay = addBit(tabooXRay, target);
    }

    public void addPinnedPieceBit(int position) {
        pinnedPieceBitboard |= (1L << position);
    }
}
