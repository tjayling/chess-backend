package org.logic;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static org.logic.PrecomputedMoveData.*;
import static org.util.BinUtil.*;
import static org.util.MoveUtil.getStartSquare;
import static org.util.MoveUtil.getTargetSquare;
import static org.util.PieceUtil.*;


public class MoveGenerator {
    private static long northOne(long bit) {
        return bit << 8;
    }

    private static long northEastOne(long bit) {
        return (bit << 9) & notAFile;
    }

    private static long eastOne(long bit) {
        return (bit << 1) & notAFile;
    }

    private static long southEastOne(long bit) {
        return (bit >>> 7) & notAFile;
    }

    private static long southOne(long bit) {
        return bit >>> 8;
    }

    private static long southWestOne(long bit) {
        return (bit >>> 9) & notHFile;
    }

    private static long westOne(long bit) {
        return (bit >>> 1) & notHFile;
    }

    private static long northWestOne(long bit) {
        return (bit << 7) & notHFile;
    }

    public static List<String> generateMoves(final BoardState boardState) {
        MoveGeneratorState moveGeneratorState = new MoveGeneratorState(boardState);

        int friendlyColour = boardState.getColourToPlay();

        boolean friendly;
        for (int i = 0; i < 2; i++) {
            friendly = i != 0;

            for (int startSquare = 0; startSquare < 64; startSquare++) {
                int piece = boardState.getSquare(startSquare);
                if ((piece == 0) || (friendly && isColour(piece, boardState.getOpponentColour())) || (!friendly && isColour(piece, friendlyColour))) {
                    continue;
                }
                long startTime = System.nanoTime();
                switch (getType(piece)) {
                    case BISHOP, ROOK, QUEEN -> generateSlidingMoves(startSquare, piece, friendly, boardState, moveGeneratorState);
                    case PAWN -> generatePawnMoves(startSquare, friendly, boardState, moveGeneratorState);
                    case KNIGHT -> generateKnightMoves(startSquare, friendly, boardState.getFriendlyKingPosition(), moveGeneratorState);
                }
                long endTime = System.nanoTime();

                double totalTime = (endTime - startTime) / 1000000.0;
                int finalStartSquare = startSquare;
                EventQueue.invokeLater(() -> System.out.printf("%s: %s time: %.5f\n", finalStartSquare, getTypeString(piece), totalTime));
            }
            generateKingMoves(friendly, boardState, moveGeneratorState);
            if (friendly) {
                generateCastleMoves(boardState, moveGeneratorState);
            }
        }

        checkKingLegality(boardState, moveGeneratorState);
        checkEnPassantMoves(boardState.getPossibleEnPassantMoves(), moveGeneratorState);

        return moveGeneratorState.getMoves();
    }

    private static void checkEnPassantMoves(List<String> possibleEnPassantMoves, MoveGeneratorState moveGeneratorState) {
        if (possibleEnPassantMoves == null || possibleEnPassantMoves.size() == 0) {
            return;
        }
        for (String move : possibleEnPassantMoves) {
            long startPositionBitboard = addBit(0, getStartSquare(move));
            if ((startPositionBitboard & moveGeneratorState.getPinnedPieceBitboard()) == 0) {
                addMove(move, moveGeneratorState);
            }
        }
    }

    private static void removeMoves(int position, MoveGeneratorState moveGeneratorState) {
        List<String> movesToRemove = new ArrayList<>();

        for (String move : moveGeneratorState.getMoves()) {
            if (getStartSquare(move) == position) {
                movesToRemove.add(move);
            }
        }
        moveGeneratorState.removeAllFromMoves(movesToRemove);
    }

    private static void generatePinnedPieceMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        List<Integer> pinnedPiecePositions = getPositionsFromBitboard(moveGeneratorState.getPinnedPieceBitboard());

        for (int position : pinnedPiecePositions) {
            int pinnedPiece = boardState.getSquare(position);

            int friendlyKingPosition = boardState.getFriendlyKingPosition();

            int xDist = (position % 8) - (friendlyKingPosition % 8);
            int yDist = (int) (floor(position / 8f) - floor(friendlyKingPosition / 8f));

            int directionToKingIndex = getDirectionToTarget(xDist, yDist);
            int directionToPinningPieceIndex = getOppositeDirection(directionToKingIndex);

            if (isType(pinnedPiece, PAWN)) {
                List<String> movesToRemove = new ArrayList<>();

                for (String move : moveGeneratorState.getMoves()) {
                    if (getStartSquare(move) != position) {
                        continue;
                    }
                    if (directionToPinningPieceIndex == 2 || directionToPinningPieceIndex == 3) {
                        if (getStartSquare(move) == position) {
                            movesToRemove.add(move);
                        }
                    }
                    if (directionToPinningPieceIndex == 0 || directionToPinningPieceIndex == 1) {
                        if (getTargetSquare(move) + 8 != getStartSquare(move) && getTargetSquare(move) + 16 != getStartSquare(move) && getTargetSquare(move) - 8 != getStartSquare(move) && getTargetSquare(move) - 16 != getStartSquare(move)) {
                            movesToRemove.add(move);
                        }
                        continue;
                    }
                    int attackTargetSquare = getTargetSquare(move);
                    int pawnAttackXDist = (position % 8) - (attackTargetSquare % 8);
                    int pawnAttackYDist = (int) ((int) floor(position / 8f) - floor(attackTargetSquare / 8f));
                    int pawnAttackDirection = getDirectionToTarget(pawnAttackXDist, pawnAttackYDist);

                    int targetPiece = boardState.getSquare(getTargetSquare(move));
                    if (!isType(targetPiece, BISHOP) && !isType(targetPiece, QUEEN) || (pawnAttackDirection != directionToPinningPieceIndex)) {
                        movesToRemove.add(move);
                    }
                }
                moveGeneratorState.removeAllFromMoves(movesToRemove);
                continue;
            }

            removeMoves(position, moveGeneratorState);

            // If a knight is pinned it cannot move, so we move on to the next pinned piece if any
            if (isType(pinnedPiece, KNIGHT)) {
                continue;
            }
            // Rooks cannot move diagonally so if the direction index is greater than 3 (meaning a diagonal move) we move on to the next pinned piece if any
            if (isType(pinnedPiece, ROOK) && directionToKingIndex > 3) {
                continue;
            }
            // Bishops cannot move orthogonally so if the direction index is less than 4 (meaning an orthogonal move) we move on to the next pinned piece if any
            if (isType(pinnedPiece, BISHOP) && directionToKingIndex < 4) {
                continue;
            }

            // First, calculate moves to king
            for (int n = 1; n <= numSquaresToEdge[position][directionToPinningPieceIndex]; n++) {
                int targetSquare = position + directionOffsets[directionToPinningPieceIndex] * n;
                addMove(position, targetSquare, moveGeneratorState);
                if (boardState.getSquare(targetSquare) != 0) {
                    break;
                }
            }

            // Then, calculate moved to pinning piece
            for (int n = 1; n <= numSquaresToEdge[position][directionToKingIndex]; n++) {
                int targetSquare = position + directionOffsets[directionToKingIndex] * n;
                if (boardState.getSquare(targetSquare) != 0) {
                    break;
                }
                addMove(position, targetSquare, moveGeneratorState);
            }
        }
    }

    /**
     * @param xDist the x distance between pinned piece and king.
     * @param yDist the y distance between pinned piece and king.
     * @return an index referring to the direction offsets as defined in PrecomputedMoveData.
     * @see org.logic.PrecomputedMoveData
     */
    private static int getDirectionToTarget(int xDist, int yDist) {
        if (xDist == 0 && yDist < 0) {
            return 0; // North
        }
        if (xDist == 0 && yDist > 0) {
            return 1; // South
        }
        if (xDist > 0 && yDist == 0) {
            return 2; // East
        }
        if (xDist < 0 && yDist == 0) {
            return 3; // West
        }
        if (xDist > 0 && yDist < 0) {
            return 4; // South East
        }
        if (xDist < 0 && yDist > 0) {
            return 5; // North West
        }
        if (xDist < 0) {
            return 6; // South West
        }
        if (xDist > 0) {
            return 7; // North East
        }
        // TODO: Change for logger
        System.out.println("There was an error in direction to king method");
        return -1;
    }

    private static int getOppositeDirection(int direction) {
        switch (direction) {
            case 0 -> {
                return 1;
            }
            case 1 -> {
                return 0;
            }
            case 2 -> {
                return 3;
            }
            case 3 -> {
                return 2;
            }
            case 4 -> {
                return 5;
            }
            case 5 -> {
                return 4;
            }
            case 6 -> {
                return 7;
            }
            case 7 -> {
                return 6;
            }
            default -> {
                // TODO: Change for logger
                System.out.println("There was an error in opposite direction method");
                return -1;
            }
        }
    }

    private static void generateBitboards() {
        generateBitboard();
    }

    private static void generateBitboard() {
    }

    private static void checkKingLegality(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        long kingPositionBitboard = addBit(0, boardState.getFriendlyKingPosition());

        checkForPinnedPieces(kingPositionBitboard, boardState, moveGeneratorState);
        generatePinnedPieceMoves(boardState, moveGeneratorState);

        boolean check = (kingPositionBitboard & moveGeneratorState.getTaboo()) != 0;

        boolean movesSizeIs0 = moveGeneratorState.getMoves().size() == 0;

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

        int directionIndex = getOppositeDirection(getDirectionToTarget(xDist, yDist));

        int checkDirection = directionOffsets[directionIndex];

        List<Integer> interceptingTargets = new ArrayList<>();
        if (!isType(boardState.getSquare(getStartSquare(checkingMove)), KNIGHT)) {
            for (int i = 1; i <= numSquaresToEdge[friendlyKingPosition][directionIndex]; i++) {
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
            for (int n = 0; n < numSquaresToEdge[friendlyKingPosition][directionIndex]; n++) {
                int targetSquare = friendlyKingPosition + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = boardState.getSquare(targetSquare);

                // If there is no piece on the target square, move to the next square in this direction
                if (pieceOnTargetSquare == 0) {
                    continue;
                }

                // If there are no potential pinned pieces and we encounter a piece that belongs to the opponent, none of our pieces can be pinned in this direction
                if (potentialPinnedPiecePositions.size() == 0 && isColour(pieceOnTargetSquare, opponentColour)) {
                    break;
                }

                // If there is more than one potential pinned piece, neither are pinned as they block a potential check
                if (potentialPinnedPiecePositions.size() > 1) {
                    break;
                }

                if (isColour(pieceOnTargetSquare, opponentColour)) {
                    if (isSlidingPiece(pieceOnTargetSquare)) {
                        // If there is a potential pinned piece, we need to see whether the attacking piece can apply a check
                        if (potentialPinnedPiecePositions.size() > 0) {
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
                if (isColour(pieceOnTargetSquare, boardState.getColourToPlay())) {
                    potentialPinnedPiecePositions.add(targetSquare);
                }
            }
        }
    }

    private static long generatePawnAttacks(long binStartSquare, int friendlyColour, MoveGeneratorState moveGeneratorState) {
        if ((binStartSquare & moveGeneratorState.getPinnedPieceBitboard()) > 0) {
            return 0;
        }
        return friendlyColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) & ~moveGeneratorState.getNotBlack() : (southEastOne(binStartSquare) | southWestOne(binStartSquare)) & ~moveGeneratorState.getNotWhite();
    }

    private static void generatePawnMoves(int start, boolean friendly, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        long rank4 = 0x00000000FF000000L;
        long rank5 = 0x000000FF00000000L;

        long binStartSquare = (long) pow(2, start);

        // Taboo logic
        if (!friendly) {
            long attacks = boardState.getOpponentColour() == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) : (southEastOne(binStartSquare) | southWestOne(binStartSquare));

            moveGeneratorState.tabooOrEquals(attacks);
            for (int target : getPositionsFromBitboard(attacks)) {
                if (target == boardState.getFriendlyKingPosition()) {
                    moveGeneratorState.addCheckingMove(squareMap.get(start) + squareMap.get(target));
                }
            }
            return;
        }

        int friendlyColour = boardState.getColourToPlay();
        long empty = moveGeneratorState.getEmpty();

        long singleTargets = friendlyColour == WHITE ? northOne(binStartSquare) & empty : southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? northOne(singleTargets) & empty & rank4 : southOne(singleTargets) & empty & rank5;
        long attacks = generatePawnAttacks(binStartSquare, friendlyColour, moveGeneratorState);

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(start, pawnTargets, moveGeneratorState);
    }

    private static void addPawnMoves(int start, long pawnTargets, MoveGeneratorState moveGeneratorState) {
        for (int target = 0; target < 64; target++) {
            // If there is a bit switched on
            if (pawnTargets << ~target < 0) {
                if (target < 8 || target > 55) {
                    char[] promotionPieces = new char[]{'r', 'n', 'b', 'q'};
                    for (char c : promotionPieces) {
                       moveGeneratorState.addMove(squareMap.get(start) + squareMap.get(target) + c);
                    }
                    continue;
                }
                addMove(start, target, moveGeneratorState);
            }
        }
    }

    private static void generateKnightMoves(int start, boolean friendly, int friendlyKingPosition, MoveGeneratorState moveGeneratorState) {

        long[] possiblePosition = new long[8];

        long binStartSquare = addBit(0, start);
        long knightMoves = 0L;

        possiblePosition[0] = ((binStartSquare << 17) & notAFile);
        possiblePosition[1] = ((binStartSquare << 10) & notABFile);
        possiblePosition[2] = ((binStartSquare >>> 6) & notABFile);
        possiblePosition[3] = ((binStartSquare >>> 15) & notAFile);
        possiblePosition[4] = ((binStartSquare << 15) & notHFile);
        possiblePosition[5] = ((binStartSquare << 6) & notGHFile);
        possiblePosition[6] = ((binStartSquare >>> 10) & notGHFile);
        possiblePosition[7] = ((binStartSquare >>> 17) & notHFile);

        for (long position : possiblePosition) {
            if (!friendly) {
                int target = getPositionFromBitboard(position);
//                taboo |= position;
                moveGeneratorState.tabooOrEquals(position);
                if (target == friendlyKingPosition) {
                   moveGeneratorState.addCheckingMove(squareMap.get(start) + squareMap.get(target));
                }
                continue;
            }

            position &= moveGeneratorState.getNotFriendlyPieces();
            int target = getPositionFromBitboard(position);
            addMove(start, target, moveGeneratorState);
        }
    }

    private static void generateKingMoves(boolean friendly, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int friendlyKingPosition = boardState.getFriendlyKingPosition();

        long binStartSquare = addBit(0, friendly ? friendlyKingPosition : boardState.getOpponentKingPosition());
        long[] kingTargets = new long[8];

        kingTargets[0] = northWestOne(binStartSquare);
        kingTargets[1] = northOne(binStartSquare);
        kingTargets[2] = northEastOne(binStartSquare);
        kingTargets[3] = southEastOne(binStartSquare);
        kingTargets[4] = southOne(binStartSquare);
        kingTargets[5] = southWestOne(binStartSquare);
        kingTargets[6] = eastOne(binStartSquare);
        kingTargets[7] = westOne(binStartSquare);

        for (long targetSquareBitboard : kingTargets) {
            if (!friendly) {
//                taboo |= targetSquareBitboard;
                moveGeneratorState.tabooOrEquals(targetSquareBitboard);
                continue;
            }
            targetSquareBitboard &= ~moveGeneratorState.getTaboo() & moveGeneratorState.getNotFriendlyPieces();

            int targetSquare = getPositionFromBitboard(targetSquareBitboard);
            addMove(friendlyKingPosition, targetSquare, moveGeneratorState);
            addKingMove(friendlyKingPosition, targetSquare, moveGeneratorState);
        }
    }

    private static void generateCastleMoves(BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int friendlyColour = boardState.getColourToPlay();
        int friendlyKingPosition = boardState.getFriendlyKingPosition();
        int[] squares = boardState.getSquares();
        long taboo = moveGeneratorState.getTaboo();

        boolean[] castlingRights = boardState.getCastlingRights();
        if (friendlyColour == WHITE) {
            if (castlingRights[0] && squares[5] == 0 && squares[6] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 6), 5), 4);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 6, moveGeneratorState);
                }
            }
            if (castlingRights[1] && squares[3] == 0 && squares[2] == 0 && squares[1] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 4), 3), 2);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 2, moveGeneratorState);
                }
            }
            return;
        }
        if (friendlyColour == BLACK) {
            if (castlingRights[2] && squares[61] == 0 && squares[62] == 0) {
                long involvedSquares = addBit(addBit(addBit(0L, 60), 61), 62);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 62, moveGeneratorState);
                }
            }
            if (castlingRights[3] && squares[57] == 0 && squares[58] == 0 && squares[59] == 0) {
                long involvedSquares = addBit(addBit(addBit(0L, 58), 59), 60);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 58, moveGeneratorState);
                }
            }
        }
    }

    private static void generateSlidingMoves(int start, int piece, boolean friendly, BoardState boardState, MoveGeneratorState moveGeneratorState) {
        int startDirIndex = isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = isType(piece, ROOK) ? 4 : 8;
        boolean moveBlocked;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            moveBlocked = false;
            for (int n = 0; n < numSquaresToEdge[start][directionIndex]; n++) {
                int target = start + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = boardState.getSquare(target);

                if (!friendly) {
                    if (!moveBlocked) {
                        moveGeneratorState.addTabooBit(target);
//                        taboo = addBit(taboo, target);
                        if (target == boardState.getFriendlyKingPosition()) {
                            moveGeneratorState.addCheckingMove(squareMap.get(start) + squareMap.get(target));
                        }
                    }

                   if (pieceOnTargetSquare > 0 && !(isType(pieceOnTargetSquare, KING) && isColour(pieceOnTargetSquare, boardState.getColourToPlay()))) {
                        moveBlocked = true;
                    }
                    moveGeneratorState.addTabooXRayBit(target);
//                    tabooXRay = addBit(tabooXRay, target);
                    continue;
                }
                if (isColour(pieceOnTargetSquare, boardState.getColourToPlay())) {
                    moveBlocked = true;
                }
                if (!moveBlocked) {
                    addMove(start, target, moveGeneratorState);
                }
                if (isColour(pieceOnTargetSquare, boardState.getOpponentColour())) {
                    moveBlocked = true;
                }
            }
        }
    }

    /**
     * If you provide a bitboard, this method returns the position of the lest significant bit
     *
     * @param bitboard the bitboard to query
     * @return the position of the least significant bit
     */
    private static int getPositionFromBitboard(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
    }

    private static List<Integer> getPositionsFromBitboard(long bitboard) {
        List<Integer> positions = new ArrayList<>();
        while (bitboard != 0) {
            int pos = getPositionFromBitboard(bitboard);
            positions.add(pos);
            bitboard = clearBit(bitboard, pos);
        }
        return positions;
    }

    private static void addMove(int start, int target, MoveGeneratorState moveGeneratorState) {
        if (target < 0 || target > 63) {
            return;
        }
        moveGeneratorState.addMove(squareMap.get(start) + squareMap.get(target));
    }

    private static void addMove(String move, MoveGeneratorState moveGeneratorState) {
        moveGeneratorState.addMove(move);
    }

    private static void addKingMove(int start, int target, MoveGeneratorState moveGeneratorState) {
        if (target < 0 || target > 63) {
            return;
        }
        moveGeneratorState.addKingMove(squareMap.get(start) + squareMap.get(target));
    }
}
