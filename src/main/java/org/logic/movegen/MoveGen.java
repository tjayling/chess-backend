package org.logic.movegen;

import static org.logic.movegen.Direction.EAST;
import static org.logic.movegen.Direction.NORTH;
import static org.logic.movegen.Direction.NORTH_EAST;
import static org.logic.movegen.Direction.NORTH_WEST;
import static org.logic.movegen.Direction.SOUTH;
import static org.logic.movegen.Direction.SOUTH_EAST;
import static org.logic.movegen.Direction.SOUTH_WEST;
import static org.logic.movegen.Direction.WEST;
import static org.util.BinUtil.addBit;
import static org.util.BinUtil.getPositions;
import static org.util.MoveUtil.getStartSquare;
import static org.util.MoveUtil.getTargetSquare;
import static org.util.PieceUtil.BISHOP;
import static org.util.PieceUtil.BLACK;
import static org.util.PieceUtil.KNIGHT;
import static org.util.PieceUtil.PAWN;
import static org.util.PieceUtil.QUEEN;
import static org.util.PieceUtil.ROOK;
import static org.util.PieceUtil.WHITE;
import static org.util.PieceUtil.getType;
import static org.util.PieceUtil.isColour;
import static org.util.PieceUtil.isSlidingPiece;
import static org.util.PieceUtil.isType;
import static org.util.PrecomputedMoveData.CASTLE_MASKS;
import static org.util.PrecomputedMoveData.DIRECTION_OFFSETS;
import static org.util.PrecomputedMoveData.NUM_SQUARES_TO_EDGE;
import static org.util.PrecomputedMoveData.X_DIST;
import static org.util.PrecomputedMoveData.Y_DIST;

import static java.lang.Math.floor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.logic.model.BoardState;
import org.logic.model.MoveGeneratorState;
import org.util.PrecomputedMoveData;


public class MoveGen {

    private final BoardState boardState;
    private final MoveGeneratorState moveGeneratorState;

    public MoveGen(BoardState boardState) {
        this.boardState = boardState;
        this.moveGeneratorState = new MoveGeneratorState(boardState);
    }

    public List<String> generateMoves() {
        for (int i = 0; i < 2; i++) {
            boolean friendly = i != 0;

            if (friendly) {
                for (int startSquare = 0; startSquare < 64; startSquare++) {
                    int piece = boardState.getSquare(startSquare);
                    if (piece == 0 || isColour(piece, boardState.getOpponentColour())) {
                        continue;
                    }
                    switch (getType(piece)) {
                        case PAWN ->
                                PawnGen.generatePawnMoves(startSquare, true, boardState, moveGeneratorState);
                        case BISHOP, ROOK, QUEEN ->
                                SlidingGen.generateFriendlySlidingMoves(startSquare, piece, boardState, moveGeneratorState);
                        case KNIGHT ->
                                KnightGen.generateFriendlyKnightMoves(startSquare, moveGeneratorState);
                    }
                }
                KingGen.generateFriendlyKingMoves(boardState, moveGeneratorState);
                generateCastleMoves(boardState, moveGeneratorState);
            } else {
                for (int startSquare = 0; startSquare < 64; startSquare++) {
                    int piece = boardState.getSquare(startSquare);
                    if (piece == 0 || isColour(piece, boardState.getFriendlyColour())) {
                        continue;
                    }
                    switch (getType(piece)) {
                        case PAWN ->
                                PawnGen.generatePawnMoves(startSquare, false, boardState, moveGeneratorState);
                        case BISHOP, ROOK, QUEEN ->
                                SlidingGen.generateOpponentSlidingMoves(startSquare, piece, boardState, moveGeneratorState);
                        case KNIGHT ->
                                KnightGen.generateOpponentKnightMoves(startSquare, moveGeneratorState, boardState);

                    }
                }
                KingGen.generateOpponentKingMoves(boardState, moveGeneratorState);
            }
        }

        checkKingLegality(boardState, moveGeneratorState);
        checkEnPassantMoves(boardState.getPossibleEnPassantMoves(), moveGeneratorState);

        return moveGeneratorState.getMoves();
    }

    private static void checkEnPassantMoves(List<String> possibleEnPassantMoves, MoveGeneratorState moveGeneratorState) {
        if (possibleEnPassantMoves == null || possibleEnPassantMoves.isEmpty()) {
            return;
        }
        for (String move : possibleEnPassantMoves) {
            long startPositionBitboard = addBit(0, getStartSquare(move));
            if ((startPositionBitboard & moveGeneratorState.getPinnedPieceBitboard()) == 0) {
                moveGeneratorState.addMove(move);
            }
        }
    }

    private static void generatePinnedPieceMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        for (int position : getPositions(moveGeneratorState.getPinnedPieceBitboard())) {
            int pinnedPiece = boardState.getSquare(position);
            int friendlyKingPosition = boardState.getFriendlyKingPosition();

            int xDist = X_DIST[friendlyKingPosition][position];
            int yDist = Y_DIST[friendlyKingPosition][position];

            Direction directionToKing = getDirectionToTarget(xDist, yDist);
            Direction directionToPinningPiece = getOppositeDirection(directionToKing);

            if (isType(pinnedPiece, PAWN)) {
                List<String> movesToRemove = new ArrayList<>();

                for (String move : moveGeneratorState.getMoves()) {
                    if (getStartSquare(move) != position) {
                        continue;
                    }
                    if (directionToPinningPiece == EAST || directionToPinningPiece == WEST) {
                        movesToRemove.add(move);
                        continue;
                    }
                    if (directionToPinningPiece == NORTH || directionToPinningPiece == SOUTH) {
                        if (getTargetSquare(move) + 8 != getStartSquare(move) && getTargetSquare(move) + 16 != getStartSquare(move) && getTargetSquare(move) - 8 != getStartSquare(move) && getTargetSquare(move) - 16 != getStartSquare(move)) {
                            movesToRemove.add(move);
                        }
                        continue;
                    }

                    int attackTargetSquare = getTargetSquare(move);
                    int pawnAttackXDist = X_DIST[attackTargetSquare][position];
                    int pawnAttackYDist = Y_DIST[attackTargetSquare][position];
                    Direction pawnAttackDirection = getDirectionToTarget(pawnAttackXDist, pawnAttackYDist);

                    int targetPiece = boardState.getSquare(getTargetSquare(move));
                    if (!isType(targetPiece, BISHOP) && !isType(targetPiece, QUEEN) || (pawnAttackDirection != directionToPinningPiece)) {
                        movesToRemove.add(move);
                    }
                }
                moveGeneratorState.removeAllFromMoves(movesToRemove);
                continue;
            }

            moveGeneratorState.removeMovesFromStartSquare(position);

            int directionToKingIndex = directionToKing.getValue();

            boolean pinnedPieceIsKnight = isType(pinnedPiece, KNIGHT);
            boolean pinnedPieceIsRookAndIsPinned = isType(pinnedPiece, ROOK) && directionToKingIndex > 3;
            boolean pinnedPieceIsBishopAndIsPinned = isType(pinnedPiece, BISHOP) && directionToKingIndex < 4;

            if (pinnedPieceIsKnight || pinnedPieceIsRookAndIsPinned || pinnedPieceIsBishopAndIsPinned) {
                continue;
            }

            for (int n = 1; n <= 8; n++) {
                int targetSquare = friendlyKingPosition + DIRECTION_OFFSETS[directionToPinningPiece.getValue()] * n;
                if (boardState.getSquare(targetSquare) == pinnedPiece) {
                    continue;
                }
                moveGeneratorState.addMove(position, targetSquare);
                if (boardState.getSquare(targetSquare) != 0) {
                    break;
                }
            }
        }
    }

    /**
     * @param xDist the x distance between pinned piece and king.
     * @param yDist the y distance between pinned piece and king.
     * @return an index referring to the direction offsets as defined in PrecomputedMoveData.
     * @see PrecomputedMoveData
     */
    private static Direction getDirectionToTarget(int xDist, int yDist) {
        if (xDist == 0 && yDist < 0) {
            return Direction.NORTH; // North
        }
        if (xDist == 0 && yDist > 0) {
            return SOUTH; // South
        }
        if (xDist > 0 && yDist == 0) {
            return EAST; // East
        }
        if (xDist < 0 && yDist == 0) {
            return WEST; // West
        }
        if (xDist > 0 && yDist < 0) {
            return SOUTH_EAST; // South East
        }
        if (xDist < 0 && yDist > 0) {
            return NORTH_WEST; // North West
        }
        if (xDist < 0) {
            return SOUTH_WEST; // South West
        }
        if (xDist > 0) {
            return NORTH_EAST; // North East
        }
        throw new RuntimeException(String.format("There was an error in direction to king method. xDist: %s, yDist: %s", xDist, yDist));
    }

    private static Direction getOppositeDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case SOUTH_EAST -> NORTH_WEST;
            case NORTH_WEST -> SOUTH_EAST;
            case SOUTH_WEST -> NORTH_EAST;
            case NORTH_EAST -> SOUTH_WEST;
        };
    }

    private static void checkKingLegality(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        long kingPositionBitboard = addBit(0, boardState.getFriendlyKingPosition());

        checkForPinnedPieces(kingPositionBitboard, boardState, moveGeneratorState);
        generatePinnedPieceMoves(boardState, moveGeneratorState);

        boolean check = (kingPositionBitboard & moveGeneratorState.getTaboo()) != 0;

        boolean movesSizeIs0 = moveGeneratorState.getMoves().isEmpty();

        if (check) {
            moveGeneratorState.setMoves(getRemainingLegalMoves(boardState, moveGeneratorState));
            if (movesSizeIs0) {
                // TODO: WORK OUT CHECKING CONDITIONS AND HOW TO HANDLE THEM IN THE FUTURE
//                System.out.println("MATE");
                return;
            }
//            System.out.println("CHECK");
            return;
        }
        if (movesSizeIs0) {
//            System.out.println("STALEMATE");
            return;
        }
    }

    private static List<String> getRemainingLegalMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        if (moveGeneratorState.getCheckingMoves().size() > 1) {
            return moveGeneratorState.getKingMoves();
        }

        int friendlyKingPosition = boardState.getFriendlyKingPosition();

        // Checkmate is true unless a move can cover the check, or the checking piece can be taken
        List<String> remainingLegalMoves = new ArrayList<>();
        String checkingMove = moveGeneratorState.getCheckingMoves().get(0);
        int xDist = (getStartSquare(checkingMove) % 8) - (friendlyKingPosition % 8);
        int yDist = (int) (floor(getStartSquare(checkingMove) / 8f) - floor(friendlyKingPosition / 8f));
//        int yDist = (int) ((int) floor(position / 8f) - floor(friendlyKingPosition / 8f));

        int directionIndex = getOppositeDirection(getDirectionToTarget(xDist, yDist)).getValue();

        int checkDirection = DIRECTION_OFFSETS[directionIndex];

        List<Integer> interceptingTargets = new ArrayList<>();
        if (!isType(boardState.getSquare(getStartSquare(checkingMove)), KNIGHT)) {
            for (int i = 1; i <= NUM_SQUARES_TO_EDGE[friendlyKingPosition][directionIndex]; i++) {
                interceptingTargets.add(friendlyKingPosition + (checkDirection * i));
                if (boardState.getSquare(friendlyKingPosition + (checkDirection * i)) != 0) {
                    break;
                }
            }
        }

        for (String friendlyMove : moveGeneratorState.getMoves()) {
            int startSquare = getStartSquare(friendlyMove);
            int targetSquare = getTargetSquare(friendlyMove);

            boolean targetSquareIsCheckingPiece = targetSquare == getStartSquare(checkingMove);
            boolean targetSquareInterceptsCheck = interceptingTargets.contains(targetSquare);
            boolean pieceIsNotPinned = (1L << startSquare & moveGeneratorState.getPinnedPieceBitboard()) == 0;

            if ((targetSquareIsCheckingPiece || targetSquareInterceptsCheck) && pieceIsNotPinned) {
                remainingLegalMoves.add(friendlyMove);
            }
        }

        remainingLegalMoves.addAll(moveGeneratorState.getKingMoves());
        return remainingLegalMoves.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private static void checkForPinnedPieces(long kingPositionBitboard, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int friendlyKingPosition = boardState.getFriendlyKingPosition();
        int opponentColour = boardState.getOpponentColour();

        if ((kingPositionBitboard & moveGeneratorState.getTabooXRay()) == 0) {
            return;
        }
        // Sliding logic sends rays out from the king
        for (int directionIndex = 0; directionIndex < 8; directionIndex++) {
            List<Integer> potentialPinnedPiecePositions = new ArrayList<>();

            // For each square stemming from the king position in certain direction
            for (int n = 0; n < NUM_SQUARES_TO_EDGE[friendlyKingPosition][directionIndex]; n++) {
                int targetSquare = friendlyKingPosition + DIRECTION_OFFSETS[directionIndex] * (n + 1);
                int pieceOnTargetSquare = boardState.getSquare(targetSquare);

                // If there is no piece on the target square, move to the next square in this direction
                if (pieceOnTargetSquare == 0) {
                    continue;
                }

                // If there are no potential pinned pieces, and we encounter a piece that belongs to the opponent, none of our pieces can be pinned in this direction
                if (potentialPinnedPiecePositions.isEmpty() && isColour(pieceOnTargetSquare, opponentColour)) {
                    break;
                }

                // If there is more than one potential pinned piece, neither are pinned as they block a potential check
                if (potentialPinnedPiecePositions.size() > 1) {
                    break;
                }

                if (isColour(pieceOnTargetSquare, opponentColour)) {
                    if (isSlidingPiece(pieceOnTargetSquare)) {
                        // If there is a potential pinned piece, we need to see whether the attacking piece can apply a check
                        if (!potentialPinnedPiecePositions.isEmpty()) {
                            // If the potential pinning piece is a rook and the direction to the rook is orthogonal, the rook cannot be pinning any pieces to us, and nothing else will be pinning us
                            if (isType(pieceOnTargetSquare, ROOK) && directionIndex > 3) {
                                break;
                            }
                            // If the potential pinning piece is a bishop and the direction to the rook is lateral, the bishop cannot be pinning any pieces to us, and nothing else will be pinning us
                            if (isType(pieceOnTargetSquare, BISHOP) && directionIndex < 4) {
                                break;
                            }
                            // If there is a sliding piece that does not pass the previous checks pinning us, any pinned piece is pinned so should be added to the bitboard
                            moveGeneratorState.addPinnedPieceBit(potentialPinnedPiecePositions.get(0)); // pinnedPieceBitboard  //= addBit(pinnedPieceBitboard, potentialPinnedPiecePositions.get(0));
                            break;
                        }
                        continue;
                    }
                    break;
                }
                // If the piece on the target square is friendly, it has the potential to be pinned
                if (isColour(pieceOnTargetSquare, boardState.getFriendlyColour())) {
                    potentialPinnedPiecePositions.add(targetSquare);
                }
            }
        }
    }

    private static void generateCastleMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int friendlyColour = boardState.getFriendlyColour();
        int friendlyKingPosition = boardState.getFriendlyKingPosition();
        int[] squares = boardState.getSquares();
        long taboo = moveGeneratorState.getTaboo();

        boolean[] castlingRights = boardState.getCastlingRights();
        switch (friendlyColour) {
            case WHITE -> {
                if ((castlingRights[0] && squares[5] == 0 && squares[6] == 0) && ((CASTLE_MASKS[0] & taboo) == 0)) {
                    moveGeneratorState.addMove(friendlyKingPosition, 6);
                }
                if ((castlingRights[1] && squares[3] == 0 && squares[2] == 0 && squares[1] == 0) && ((CASTLE_MASKS[1] & taboo) == 0)) {
                    moveGeneratorState.addMove(friendlyKingPosition, 2);
                }
            }
            case BLACK -> {
                if ((castlingRights[2] && squares[61] == 0 && squares[62] == 0) && ((CASTLE_MASKS[2] & taboo) == 0)) {
                    moveGeneratorState.addMove(friendlyKingPosition, 62);
                }
                if ((castlingRights[3] && squares[57] == 0 && squares[58] == 0 && squares[59] == 0) && ((CASTLE_MASKS[3] & taboo) == 0)) {
                    moveGeneratorState.addMove(friendlyKingPosition, 58);
                }
            }
        }
    }


}
