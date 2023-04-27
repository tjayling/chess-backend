package org.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.util.BinUtil.addBit;
import static org.util.PieceUtil.*;

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
        notFriendlyPieces = boardState.getColourToPlay() == WHITE ? notWhite : notBlack;
        empty = notWhite & notBlack;
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

    public void tabooOrEquals(long bitboard) {
        taboo |= bitboard;
    }

    public void addMove(String move) {
        moves.add(move);
    }

    public void removeAllFromMoves(List<String> movesToRemove) {
        moves.removeAll(movesToRemove);
    }

    public void addCheckingMove(String move) {
        checkingMoves.add(move);
    }

    public void addKingMove(String move) {
        kingMoves.add(move);
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
