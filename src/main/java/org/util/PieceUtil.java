package org.util;

public class PieceUtil {
    public static final int NONE = 0;
    public static final int KING = 1;
    public static final int PAWN = 2;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 4;
    public static final int ROOK = 5;
    public static final int QUEEN = 6;

    public static final int WHITE = 16;
    public static final int BLACK = 32;

    public static boolean isColour(int piece, int colour) {
        return BinUtil.getMsb(piece) == colour;
    }

    public static boolean isType(int piece, int type) {
        return BinUtil.getLsb(piece) == type;
    }

    public static int getType(int piece) {
        return BinUtil.getLsb(piece);
    }

    public static String getTypeString(int piece) {
        switch (BinUtil.getLsb(piece)) {
            case (1) -> {
                return "King";
            }
            case (2) -> {
                return "Pawn";
            }
            case (3) -> {
                return "Knight";
            }
            case (4) -> {
                return "Bishop";
            }
            case (5) -> {
                return "Rook";
            }
            case (6) -> {
                return "Queen";
            }
            default -> {
                return "Empty";
            }
        }
    }

    public static boolean isSlidingPiece(int piece) {
        int pieceType = BinUtil.getLsb(piece);
        return pieceType == 4 || pieceType == 5 || pieceType == 6;
    }

    public static boolean isPawn(int piece) {
        return BinUtil.getLsb(piece) == 2;
    }

    public static boolean isKnight(int piece) {
        return BinUtil.getLsb(piece) == 3;
    }

    public static int getOppositeColour(int colour) {
        return BinUtil.getMsb(colour) == WHITE ? BLACK : WHITE;
    }

    public static String getColour(int colour) {
        return BinUtil.getMsb(colour) == WHITE ? "WHITE" : "BLACK";
    }
}


