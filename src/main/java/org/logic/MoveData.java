package org.logic;

import static org.util.PieceUtil.getOppositeColour;

public class MoveData {
    private final int[] squares;
    private int colourToPlay;
    private final boolean[] castlingRights;
    private final String enPassantTarget;

    public MoveData(int[] squares, int colourToPlay, boolean[] castlingRights, String enPassantTarget) {
        this.squares = squares;
        this.colourToPlay = colourToPlay;
        this.castlingRights = castlingRights;
        this.enPassantTarget = enPassantTarget;
    }

    public void flipColourToPlay() {
        colourToPlay = getOppositeColour(colourToPlay);
    }

    public int[] getSquares() {
        return squares;
    }

    public int getColourToPlay() {
        return colourToPlay;
    }

    public boolean[] getCastlingRights() {
        return castlingRights;
    }

    public String getEnPassantTarget() {
        return enPassantTarget;
    }
}
