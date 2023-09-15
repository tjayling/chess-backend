package org.logic;

import java.util.List;

import static org.util.PieceUtil.*;

public class BoardState {
    private final int[] squares;
    private final int friendlyColour;
    private final int opponentColour;
    private final boolean[] castlingRights;
    private final List<String> possibleEnPassantMoves;
    private final int friendlyKingPosition;
    private final int opponentKingPosition;

    public BoardState(int[] squares, int friendlyColour, boolean[] castlingRights, List<String> possibleEnPassantMoves, int friendlyKingPosition, int opponentKingPosition) {
        this.squares = squares;
        this.friendlyColour = friendlyColour;
        this.castlingRights = castlingRights;
        this.possibleEnPassantMoves = possibleEnPassantMoves;
        this.opponentKingPosition = opponentKingPosition;
        this.friendlyKingPosition = friendlyKingPosition;
        opponentColour = getOppositeColour(friendlyColour);
    }

    public int[] getSquares() {
        return squares;
    }

    public int getFriendlyColour() {
        return friendlyColour;
    }

    public int getOpponentColour() {
        return opponentColour;
    }

    public List<String> getPossibleEnPassantMoves() {
        return possibleEnPassantMoves;
    }

    public boolean[] getCastlingRights() {
        return castlingRights;
    }

    public int getFriendlyKingPosition() {
        return friendlyKingPosition;
    }

    public int getOpponentKingPosition() {
        return opponentKingPosition;
    }

    public int getSquare(int index) {
        return squares[index];
    }
}
