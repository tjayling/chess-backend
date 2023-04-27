package org.util;

import org.gui.exception.InvalidFenFileException;
import org.logic.MoveData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.util.PieceUtil.*;

public class FenUtil {
    public static String getFenFromSquares(int[] board) {
        StringBuilder encodedBord = new StringBuilder();
        StringBuilder currentRank = new StringBuilder();
        int i = 0;
        for (int piece : board) {
            i++;
            if (piece == 0) {
                currentRank.append('1');
            }
            if (piece != 0) {
                switch (getType(piece)) {
                    case "King" -> currentRank.append(isColour(piece, BLACK) ? "k" : "K");
                    case "Pawn" -> currentRank.append(isColour(piece, BLACK) ? "p" : "P");
                    case "Knight" -> currentRank.append(isColour(piece, BLACK) ? "n" : "N");
                    case "Bishop" -> currentRank.append(isColour(piece, BLACK) ? "b" : "B");
                    case "Rook" -> currentRank.append(isColour(piece, BLACK) ? "r" : "R");
                    case "Queen" -> currentRank.append(isColour(piece, BLACK) ? "q" : "Q");
                }
            }
            if (i % 8 == 0) {
                encodedBord.append(currentRank.reverse());
                currentRank = new StringBuilder();

                if (i < 63) {
                    encodedBord.append('/');
                }
            }
        }
        return encodedBord.reverse().toString();
    }

    public static String generateFenFromMoveData(MoveData moveData) {
        String squaresFen = getFenFromSquares(moveData.getSquares());
        String colourFen = moveData.getColourToPlay() == WHITE ? "w" : "b";

        String castlingRightsFen = "";

        boolean[] castlingRights = moveData.getCastlingRights();

        //White King side rights
        if (castlingRights[0]) {
            castlingRightsFen += "K";
        }
        //White Queen side rights
        if (castlingRights[1]) {
            castlingRightsFen += "Q";
        }
        //Black Queen side rights
        if (castlingRights[2]) {
            castlingRightsFen += "k";
        }
        //Black King side rights
        if (castlingRights[3]) {
            castlingRightsFen += "q";
        }

        if (castlingRightsFen.length() == 0) {
            castlingRightsFen += "-";
        }

        String enPassantTarget = moveData.getEnPassantTarget();
        String enPassantFen = (enPassantTarget == null || enPassantTarget.length() == 0) ? "-" : enPassantTarget;

        String newFen = squaresFen;
        newFen += " " + colourFen;
        newFen += " " + castlingRightsFen;
        newFen += " " + enPassantFen;
        return newFen;
    }


    public static int[] getSquaresFromFen(String fen) {
        Map<Character, Integer> pieceTypeFromSymbol = new HashMap<>() {{
            put('k', KING);
            put('p', PAWN);
            put('n', KNIGHT);
            put('b', BISHOP);
            put('r', ROOK);
            put('q', QUEEN);
        }};

        int file = 0, rank = 7;
        int[] squares = new int[64];

        String fenBoard = fen.split(" ")[0];

        for (char symbol : fenBoard.toCharArray()) {
            if (symbol == '/') {
                file = 0;
                rank--;
                continue;
            }
            if (Character.isDigit(symbol)) {
                file += Character.getNumericValue(symbol);
                continue;
            }
            int colour = (Character.isUpperCase(symbol) ? WHITE : BLACK);
            int type = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
            squares[file + (rank * 8)] = type | colour;
            file++;
        }
        return squares;
    }


    public static boolean[] getCastlingRightsFromFen(String fen) {
        String castlingRightString = fen.split(" ")[2];
        if (Objects.equals(castlingRightString, "-")) {
            return new boolean[]{false, false, false, false};
        }
        boolean[] castlingRights = new boolean[4];
        castlingRights[0] = castlingRightString.contains("K");
        castlingRights[1] = castlingRightString.contains("Q");
        castlingRights[2] = castlingRightString.contains("k");
        castlingRights[3] = castlingRightString.contains("q");
        return castlingRights;
    }

    public static int getColourToPlayFromFen(String fen) {
        String colourToPlay = fen.split(" ")[1];
        return Objects.equals(colourToPlay, "w") ? WHITE : BLACK;
    }

    public static String getEnPassantTargetFromFen(String fen) {
        return fen.split(" ")[3];
    }

    public static String checkValidityOfFenString(String fenString) throws InvalidFenFileException {
        try {
            getSquaresFromFen(fenString);
            getColourToPlayFromFen(fenString);
            getEnPassantTargetFromFen(fenString);
            getCastlingRightsFromFen(fenString);
            return fenString;
        } catch (Exception e) {
            throw new InvalidFenFileException("Invalid FEN string: " + e.getMessage(), e);
        }
    }
}
