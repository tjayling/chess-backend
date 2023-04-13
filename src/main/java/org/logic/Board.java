package org.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.floor;
import static org.logic.PrecomputedMoveData.squareMap;
import static org.util.FenUtil.*;
import static org.util.MoveUtil.getStartSquare;
import static org.util.MoveUtil.getTargetSquare;
import static org.util.PieceUtil.*;

public class Board {
    private final int[] squares;
    private final boolean[] castlingRights;
    private final int colourToPlay;
    private List<String> moves;
    private String enPassantTarget;
    private int friendlyKingPosition;
    private int opponentKingPosition;

    public Board(String encodedBoard) {
        moves = new ArrayList<>();
        squares = getSquaresFromFen(encodedBoard);
        castlingRights = getCastlingRightsFromFen(encodedBoard);
        colourToPlay = getColourToPlayFromFen(encodedBoard);
        getKingPositions();
        moves = generateMoves(encodedBoard);
    }

    public List<String> getMoves() {
        return moves;
    }

    private void promote(int targetPos, String targetPiece) {
        // Will take data from the request with the piece type to promote to.
        switch (targetPiece) {
            case "r":
                squares[targetPos] = colourToPlay | ROOK;
                break;
            case "n":
                squares[targetPos] = colourToPlay | KNIGHT;
                break;
            case "b":
                squares[targetPos] = colourToPlay | BISHOP;
                break;
            case "q":
                squares[targetPos] = colourToPlay | QUEEN;
                break;
        }
    }

    public List<String> getEnPassantMovesFromFen(int[] squares, String enPassantTarget) {
        if (Objects.equals(enPassantTarget, "-")) {
            return null;
        }
        char targetFile = enPassantTarget.charAt(0);

        int[] attackingPawnFiles = new int[2];

        switch (targetFile) {
            case 'a':
                attackingPawnFiles[0] = 'b';
                break;
            case 'h':
                attackingPawnFiles[0] = 'g';
                break;
            default:
                attackingPawnFiles[0] = targetFile - 1;
                attackingPawnFiles[1] = targetFile + 1;
                break;
        }

        int attackingRank = colourToPlay == WHITE ? 5 : 4;

        List<String> enPassantMoves = new ArrayList<>();

        for (int file : attackingPawnFiles) {
            if (file == 0) {
                continue;
            }

            String attacker = Character.toString(file) + attackingRank;

            int attackingPiece = squares[squareMap.indexOf(attacker)];
            boolean attackerIsFriendlyPawn = isType(attackingPiece, PAWN) && isColour(attackingPiece,colourToPlay);
            if (attackerIsFriendlyPawn) {
                enPassantMoves.add(attacker + enPassantTarget);
            }
        }
        return enPassantMoves;
    }

    public List<String> generateMoves(String encodedBoard) {
        List<String> possibleEnPassantMoves = getEnPassantMovesFromFen(squares, getEnPassantTargetFromFen(encodedBoard));
        return MoveGenerator.generateMoves(squares, colourToPlay, possibleEnPassantMoves, castlingRights, friendlyKingPosition, opponentKingPosition);
    }

    private void getKingPositions() {
        for (int startSquare = 0; startSquare < 64; startSquare++) {
            int piece = squares[startSquare];
            if (isType(piece, KING) && isColour(piece, colourToPlay)) {
                friendlyKingPosition = startSquare;
                continue;
            }
            if (isType(piece, KING) && isColour(piece, getOppositeColour(colourToPlay))) {
                opponentKingPosition = startSquare;
            }
        }
    }

    public MoveData move(String move) {
//        System.out.println(Arrays.toString(whiteCastlingRights));
        int startPos = getStartSquare(move);
        int targetPos = getTargetSquare(move);
        boolean castled = false;
        // If the king moves, castling rights are removed for the current player
        if (startPos == friendlyKingPosition) {
            castled = castle(targetPos);
            if (!castled) {
                switch (colourToPlay) {
                    case WHITE:
                        castlingRights[0] = false;
                        castlingRights[1] = false;
                        break;
                    case BLACK:
                        castlingRights[2] = false;
                        castlingRights[3] = false;
                        break;
                }
            }
        }

        if (!castled) {
            int pieceToMove = squares[startPos];
            int takenPiece = squares[targetPos];
            // Move pieces
            squares[startPos] = 0;
            squares[targetPos] = pieceToMove;
            // Check castling rights
            if (isType(pieceToMove, ROOK)) {
                updateCastlingRights(startPos);
            }

            if (isType(pieceToMove, PAWN)) {
                int targetRank = (int) (floor(targetPos / 8f) + 1);
                // Check en passant moves
                processEnPassantMoves(startPos, targetPos);
                // Then check for en passant moves on the next iteration
                checkEnPassantMoves(move, targetRank);
                // We don't need to check what colour because pawns can't move backwards, so target will always be one of the edge ranks
                if (targetRank == 8 || targetRank == 1) {
                    promote(targetPos, move.substring(4, 5));
                }
            }

            if (takenPiece > 0) {
                // Check castling rights
                if (isType(takenPiece, ROOK)) {
                    updateCastlingRights(targetPos);
                }
                //todo: remove piece
            }
        }

        return new MoveData(squares, colourToPlay, castlingRights, enPassantTarget);
    }

    private void processEnPassantMoves(int startPos, int targetPos) {
        if (squares[targetPos] != (colourToPlay | PAWN)) {
            return;
        }

        boolean isAttacking;
        int attackedPiece;
        boolean pieceUnderneathIsOpponentPawn;

        switch (colourToPlay) {
            case WHITE:
                isAttacking = startPos + 8 != targetPos;
                attackedPiece = squares[targetPos - 8];
                pieceUnderneathIsOpponentPawn = isType(attackedPiece, PAWN) && isColour(attackedPiece, BLACK);
                if (isAttacking && pieceUnderneathIsOpponentPawn) {
                    squares[targetPos - 8] = 0;
                }
                break;
            case BLACK:
                isAttacking = startPos - 8 != targetPos;
                attackedPiece = squares[targetPos + 8];
                pieceUnderneathIsOpponentPawn = isType(attackedPiece, PAWN) && isColour(attackedPiece, WHITE);

                if (isAttacking && pieceUnderneathIsOpponentPawn) {
                    squares[targetPos + 8] = 0;
                }
                //Todo: add taken piece to list
        }
    }

    private void checkEnPassantMoves(String move, int targetRank) {
        int startPos = getStartSquare(move);
        int targetPos = getTargetSquare(move);
        int startRank = (int) (floor(startPos / 8f) + 1);

        if (isColour(colourToPlay, WHITE) && startRank != 2) {
            return;
        }
        if (isColour(colourToPlay, BLACK) && startRank != 7) {
            return;
        }
        int targetFile = targetPos % 8 + 1;

        if (colourToPlay == WHITE && targetRank == 4) {
            generateEnPassantMoves(targetPos, targetFile, BLACK);
            return;
        }
        if (colourToPlay == BLACK && targetRank == 5) {
            generateEnPassantMoves(targetPos, targetFile, WHITE);
        }
    }

    private void generateEnPassantMoves(int targetPos, int targetFile, int opponentColour) {
        int targetPawnPos = opponentColour == BLACK ? targetPos - 8 : targetPos + 8;
        if (targetFile - 1 >= 1) {
            int leftPiece = squares[targetPos - 1];
            if (isColour(leftPiece, opponentColour) && isType(leftPiece, PAWN)) {
                enPassantTarget = squareMap.get(targetPawnPos);
            }
        }
        if (targetFile + 1 <= 8) {
            int rightPiece = squares[targetPos + 1];
            if (isColour(rightPiece, opponentColour) && isType(rightPiece, PAWN)) {
                enPassantTarget = squareMap.get(targetPawnPos);
            }
        }
    }

    private boolean castle(int targetPosition) {
        boolean friendlyPositionIsValid = friendlyKingPosition == 4 || friendlyKingPosition == 60;
        boolean targetIsCastleMove = targetPosition == 2 || targetPosition == 6 || targetPosition == 58 || targetPosition == 62;
        if (friendlyPositionIsValid && targetIsCastleMove) {
            switch (friendlyKingPosition) {
                // If white king start position
                case 4:
                    switch (targetPosition) {
                        case 2:
                            squares[0] = 0;
                            squares[4] = 0;
                            squares[2] = WHITE | KING;
                            squares[3] = WHITE | ROOK;
                            break;
                        case 6:
                            squares[4] = 0;
                            squares[7] = 0;
                            squares[6] = WHITE | KING;
                            squares[5] = WHITE | ROOK;
                            break;
                    }
                    break;
                // If black king start position
                case 60:
                    switch (targetPosition) {
                        case 58:
                            squares[56] = 0;
                            squares[60] = 0;
                            squares[58] = BLACK | KING;
                            squares[59] = BLACK | ROOK;
                            break;
                        case 62:
                            squares[60] = 0;
                            squares[63] = 0;
                            squares[62] = BLACK | KING;
                            squares[61] = BLACK | ROOK;
                            break;
                    }
            }
            return true;
        }
        return false;
    }

    public void updateCastlingRights(int pos) {
        if (castlingRights[0] || castlingRights[1] || castlingRights[2] || castlingRights[3]) {
            switch (pos) {
                case 7:
                    castlingRights[0] = false;
                    break;
                case 0:
                    castlingRights[1] = false;
                    break;
                case 56:
                    castlingRights[2] = false;
                    break;
                case 63:
                    castlingRights[3] = false;
                    break;
            }
        }
    }
}
