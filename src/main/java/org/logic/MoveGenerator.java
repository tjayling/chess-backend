package org.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static org.logic.PrecomputedMoveData.*;
import static org.util.BinUtil.printBin;
import static org.util.MoveUtil.getStartSquare;
import static org.util.MoveUtil.getTargetSquare;
import static org.util.PieceUtil.*;


public class MoveGenerator {
    private static final long notAFile = 0xFEFEFEFEFEFEFEFEL;
    private static final long notHFile = 0x7F7F7F7F7F7F7F7FL;
    private static long pinnedPieces = 0;
    private static long empty;
    private static long notWhite;
    private static long notBlack;
    private static long notFriendlyPieces;
    private static long taboo;
    private static long tabooXRay;
    private static List<String> moves;
    private static List<String> kingMoves;
    private static List<String> checkingMoves;
    private static int friendlyColour;
    private static int opponentColour;
    private static int[] squares;
    private static boolean[] castlingRights;
    private static int friendlyKingPosition;
    private static int opponentKingPosition;
//    private final Logger logger = LoggerFactory.getLogger(MoveGenerator.class);

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

    public static List<String> generateMoves(int[] squares, int colourToMove, List<String> possibleEnPassantMoves, boolean[] castlingRights, int friendlyKingPosition, int opponentKingPosition) {
        resetAttributes(squares, colourToMove, castlingRights, friendlyKingPosition, opponentKingPosition);
        generateBitboards();


        boolean friendly;
        for (int i = 0; i < 2; i++) {
            friendly = i != 0;

            boolean finalFriendly = friendly;
            for (int startSquare = 0; startSquare < 64; startSquare++) {
                int piece = squares[startSquare];
                if ((finalFriendly && isColour(piece, opponentColour)) | (!finalFriendly && isColour(piece, friendlyColour))) {
                    continue;
                }
                if (isType(piece, BISHOP) || isType(piece, ROOK) || isType(piece, QUEEN)) {
                    generateSlidingMoves(startSquare, piece, finalFriendly);
                    continue;
                }
                if (isType(piece, PAWN)) {
                    generatePawnMoves(startSquare, finalFriendly);
                    continue;
                }
                if (isType(piece, KNIGHT)) {
                    generateKnightMoves(startSquare, finalFriendly);
                }
            }
            generateKingMoves(friendly);
            if (friendly) {
                generateCastleMoves();
            }
        }

        checkKingLegality();
        checkEnPassantMoves(possibleEnPassantMoves);

        return moves;
    }

    private static void resetAttributes(int[] squares, int colourToMove, boolean[] castlingRights, int friendlyKingPosition, int opponentKingPosition) {
        MoveGenerator.friendlyKingPosition = friendlyKingPosition;
        MoveGenerator.opponentKingPosition = opponentKingPosition;
        MoveGenerator.squares = squares;
        friendlyColour = colourToMove;
        opponentColour = getOppositeColour(friendlyColour);
        moves = new ArrayList<>();
        kingMoves = new ArrayList<>();
        checkingMoves = new ArrayList<>();
        MoveGenerator.castlingRights = castlingRights;
        // Reset bitboards
        empty = 0;
        notWhite = 0;
        notBlack = 0;
        taboo = 0;
        tabooXRay = 0;
        pinnedPieces = 0;
    }

    private static void checkEnPassantMoves(List<String> possibleEnPassantMoves) {
        if (possibleEnPassantMoves == null || possibleEnPassantMoves.size() == 0) {
            return;
        }
        for (String move : possibleEnPassantMoves) {
            long startPositionBitboard = addBit(0, getStartSquare(move));
            if ((startPositionBitboard & pinnedPieces) == 0) {
                addMove(move);
            }
        }
    }

    private static void removeMoves(int position) {
        List<String> movesToRemove = new ArrayList<>();

        for (String move : moves) {
            if (getStartSquare(move) == position) {
                movesToRemove.add(move);
            }
        }
        moves.removeAll(movesToRemove);
    }

    private static void generatePinnedPieceMoves() {
        List<Integer> pinnedPiecePositions = getPositionsFromBitboard(pinnedPieces);

        for (int position : pinnedPiecePositions) {
            int pinnedPiece = squares[position];

            int xDist = (position % 8) - (friendlyKingPosition % 8);
            int yDist = (int) (floor(position / 8f) - floor(friendlyKingPosition / 8f));

            int directionToKingIndex = getDirectionToTarget(xDist, yDist);
            int directionToPinningPieceIndex = getOppositeDirection(directionToKingIndex);

            int distanceToKing = (int) sqrt(pow(xDist, 2) + pow(yDist, 2));

            if (isType(pinnedPiece, PAWN)) {
                List<String> movesToRemove = new ArrayList<>();

                for (String move : moves) {
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

                    if (!isType(squares[getTargetSquare(move)], BISHOP) && !isType(squares[getTargetSquare(move)], QUEEN) || (pawnAttackDirection != directionToPinningPieceIndex)) {
                        movesToRemove.add(move);
                    }
                }
                moves.removeAll(movesToRemove);
                continue;
            }

            removeMoves(position);

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
                addMove(position, targetSquare);
                if (squares[targetSquare] != 0) {
                    break;
                }
            }

            // Then, calculate moved to pinning piece
            for (int n = 1; n <= numSquaresToEdge[position][directionToKingIndex]; n++) {
                int targetSquare = position + directionOffsets[directionToKingIndex] * n;
                if (squares[targetSquare] != 0) {
                    break;
                }
                addMove(position, targetSquare);
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
        generateBitboard(WHITE);
//        generateBitboard(BLACK);
        notFriendlyPieces = friendlyColour == WHITE ? notWhite : notBlack;
        empty = notWhite & notBlack;
    }

    private static void generateBitboard(int colour) {
        notWhite = 0;
        notBlack = 0;
        for (int i = 0; i < 64; i++) {
            int piece = squares[i];
            if (!isColour(piece, BLACK)) {
                notBlack = addBit(notBlack, i);
            }
            if (!isColour(piece, WHITE)) {
                notWhite = addBit(notWhite, i);
            }
        }
    }

    private static void checkKingLegality() {
        long kingPositionBitboard = addBit(0, friendlyKingPosition);

        checkForPinnedPieces(kingPositionBitboard);
        generatePinnedPieceMoves();

        boolean check = (kingPositionBitboard & taboo) != 0;
        if (check) {
            moves = getRemainingLegalMoves();
            if (moves.size() == 0) {
                // TODO: WORK OUT CHECKING CONDITIONS AND HOW TO HANDLE THEM IN THE FUTURE
//                System.out.println("MATE");
                return;
            }
//            System.out.println("CHECK");
            return;
        }
        if (moves.size() == 0) {
//            System.out.println("STALEMATE");
            return;
        }
    }

    private static List<String> getRemainingLegalMoves() {
        if (checkingMoves.size() > 1) {
            return kingMoves;
        }

        // Checkmate is true unless a move can cover the check, or the checking piece can be taken
        List<String> remainingLegalMoves = new ArrayList<>();
        String checkingMove = checkingMoves.get(0);
        int xDist = (getStartSquare(checkingMove) % 8) - (friendlyKingPosition % 8);
        int yDist = (int) (floor(getStartSquare(checkingMove) / 8f) - floor(friendlyKingPosition / 8f));
//        int yDist = (int) ((int) floor(position / 8f) - floor(friendlyKingPosition / 8f));

        int directionIndex = getOppositeDirection(getDirectionToTarget(xDist, yDist));

        int checkDirection = directionOffsets[directionIndex];
//        int checkDistance = ((xDist <= 1 && xDist >= -1) && (yDist <= 1 && yDist >= -1)) ? 0 : (int) sqrt(pow(xDist < 0 ? xDist + 1 : xDist - 1, 2) + pow(yDist < 0 ? yDist + 1 : yDist - 1, 2));

//        int checkDistance = (xDist <= 1 && xDist >= -1) && (yDist <= 1 && yDist >= -1) ? 0 : (int) sqrt(pow(xDist, 2) + pow(yDist, 2));

//        System.out.println("Check dist:");
//        System.out.println(checkDistance);

//        if (checkDirection == 8 || checkDirection == -8) {
//            checkDirection *= -1;
//        }

        List<Integer> interceptingTargets = new ArrayList<>();
        if (!isType(squares[getStartSquare(checkingMove)], KNIGHT)) {
            for (int i = 1; i <= numSquaresToEdge[friendlyKingPosition][directionIndex]; i++) {
                interceptingTargets.add(friendlyKingPosition + (checkDirection * i));
                if (squares[friendlyKingPosition + (checkDirection * i)] != 0) {
                    break;
                }
            }
        }

        for (String friendlyMove : moves) {
            int startSquare = getStartSquare(friendlyMove);
            int targetSquare = getTargetSquare(friendlyMove);

            boolean targetSquareIsCheckingPiece = targetSquare == getStartSquare(checkingMove);
            boolean targetSquareInterceptsCheck = interceptingTargets.contains(targetSquare);
            boolean pieceIsNotPinned = (1L << startSquare & pinnedPieces) == 0;

            if ((targetSquareIsCheckingPiece || targetSquareInterceptsCheck) && pieceIsNotPinned) {
                remainingLegalMoves.add(friendlyMove);
            }
        }

        remainingLegalMoves.addAll(kingMoves);
        return remainingLegalMoves.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private static void checkForPinnedPieces(long kingPositionBitboard) {
        if ((kingPositionBitboard & tabooXRay) == 0) {
            return;
        }
        // Sliding logic sends rays out from the king
        for (int directionIndex = 0; directionIndex < 8; directionIndex++) {
            List<Integer> potentialPinnedPiecePositions = new ArrayList<>();

            // For each square stemming from the king position in certain direction
            for (int n = 0; n < numSquaresToEdge[friendlyKingPosition][directionIndex]; n++) {
                int targetSquare = friendlyKingPosition + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[targetSquare];

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
                            pinnedPieces = addBit(pinnedPieces, potentialPinnedPiecePositions.get(0));
                            break;
                        }
                        continue;
                    }
                    break;
                }
                // If the piece on the target square is friendly, it has the potential to be pinned
                if (isColour(pieceOnTargetSquare, friendlyColour)) {
                    potentialPinnedPiecePositions.add(targetSquare);
                }

//                // If we encounter an opponent sliding piece, we need to check if they are pinning a friendly piece to our king
//                if (isSlidingPiece(pieceOnTargetSquare) && isColour(pieceOnTargetSquare, opponentColour)) {
//                    // If there is a potential pinned piece, we need to see whether the attacking piece can apply a check
//                    if (potentialPinnedPiecePositions.size() > 0) {
//                        // If the potential pinning piece is a rook and the direction to the rook is orthogonal, the rook cannot be pinning any pieces to us, and nothing else will be pinning us
//                        if (isType(pieceOnTargetSquare, ROOK) && directionIndex > 3) {
//                            break;
//                        }
//                        // If the potential pinning piece is a bishop and the direction to the rook is lateral, the bishop cannot be pinning any pieces to us, and nothing else will be pinning us
//                        if (isType(pieceOnTargetSquare, BISHOP) && directionIndex < 4) {
//                            break;
//                        }
//                        // If there is a sliding piece that does not pass the previous checks pinning us, any pinned piece is pinned so should be added to the bitboard
//                        pinnedPieces = addBit(pinnedPieces, potentialPinnedPiecePositions.get(0));
//                        break;
//                    }
//                    continue;
//                }
//                // If the piece on the target square is friendly, it has the potential to be pinned
//                if (isColour(pieceOnTargetSquare, friendlyColour)) {
//                    potentialPinnedPiecePositions.add(targetSquare);
//                }
            }
        }
    }

    private static long generatePawnAttacks(long binStartSquare) {
        if ((binStartSquare & pinnedPieces) > 0) {
            return 0;
        }
        return friendlyColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) & ~notBlack : (southEastOne(binStartSquare) | southWestOne(binStartSquare)) & ~notWhite;
    }

    private static void generatePawnMoves(int start, boolean friendly) {
        long rank4 = 0x00000000FF000000L;
        long rank5 = 0x000000FF00000000L;

        long binStartSquare = (long) pow(2, start);

        // Taboo logic
        if (!friendly) {
            long attacks = opponentColour == WHITE ? (northEastOne(binStartSquare) | northWestOne(binStartSquare)) : (southEastOne(binStartSquare) | southWestOne(binStartSquare));
            taboo |= attacks;
//            if (start == 48) {
//                System.out.println("Attacks:");
//                printBin(attacks);
//            }
            for (int target : getPositionsFromBitboard(attacks)) {
                if (target == friendlyKingPosition) {
                    checkingMoves.add(squareMap.get(start) + squareMap.get(target));
                }
            }
            return;
        }

        long singleTargets = friendlyColour == WHITE ? northOne(binStartSquare) & empty : southOne(binStartSquare) & empty;
        long doubleTargets = friendlyColour == WHITE ? northOne(singleTargets) & empty & rank4 : southOne(singleTargets) & empty & rank5;
        long attacks = generatePawnAttacks(binStartSquare);

        long pawnTargets = singleTargets | doubleTargets | attacks;
        addPawnMoves(start, pawnTargets);
    }

    private static void addPawnMoves(int start, long pawnTargets) {
        for (int target = 0; target < 64; target++) {
            // If there is a bit switched on
            if (pawnTargets << ~target < 0) {
                if (target < 8 || target > 55) {
                    char[] promotionPieces = new char[]{'r', 'n', 'b', 'q'};
                    for (char c : promotionPieces) {
                        moves.add(squareMap.get(start) + squareMap.get(target) + c);
                    }
                    continue;
                }
                addMove(start, target);
            }
        }
    }

    private static void generateKnightMoves(int start, boolean friendly) {
        long notABFile = 0xFCFCFCFCFCFCFCFCL;
        long notGHFile = 0x3F3F3F3F3F3F3F3FL;

        long[] possiblePosition = new long[8];

        long binStartSquare = addBit(0, start);

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
                taboo |= position;
                if (target == friendlyKingPosition) {
                    checkingMoves.add(squareMap.get(start) + squareMap.get(target));
                }
                continue;
            }

            position &= notFriendlyPieces;
            int target = getPositionFromBitboard(position);
            addMove(start, target);
        }
    }

    private static void generateKingMoves(boolean friendly) {

        long binStartSquare = addBit(0, friendly ? friendlyKingPosition : opponentKingPosition);
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
                taboo |= targetSquareBitboard;
                continue;
            }
//            printBin(taboo);
            targetSquareBitboard &= ~taboo & notFriendlyPieces;

            int targetSquare = getPositionFromBitboard(targetSquareBitboard);
            addMove(friendlyKingPosition, targetSquare);
            addKingMove(friendlyKingPosition, targetSquare);
        }
    }

    private static void generateCastleMoves() {
        boolean[] whiteCastlingRights = Arrays.copyOfRange(castlingRights, 0, 2);
        boolean[] blackCastlingRights = Arrays.copyOfRange(castlingRights, 2, 4);
        if (friendlyColour == WHITE) {
            if (whiteCastlingRights[0] && squares[5] == 0 && squares[6] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 6), 5), 4);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 6);
                }
            }
            if (whiteCastlingRights[1] && squares[3] == 0 && squares[2] == 0 && squares[1] == 0) {
                long involvedSquares = addBit(addBit(addBit(0, 4), 3), 2);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 2);
                }
            }
            return;
        }
        if (friendlyColour == BLACK) {
            if (blackCastlingRights[0] && squares[61] == 0 && squares[62] == 0) {
                long involvedSquares = addBit(addBit(addBit(0L, 60), 61), 62);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 62);
                }
            }
            if (blackCastlingRights[1] && squares[57] == 0 && squares[58] == 0 && squares[59] == 0) {
                long involvedSquares = addBit(addBit(addBit(0L, 58), 59), 60);
                if ((involvedSquares & taboo) == 0) {
                    addMove(friendlyKingPosition, 58);
                }
            }
        }
    }

    private static void generateSlidingMoves(int start, int piece, boolean friendly) {
        int startDirIndex = isType(piece, BISHOP) ? 4 : 0;
        int endDirIndex = isType(piece, ROOK) ? 4 : 8;
        boolean moveBlocked;
        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            moveBlocked = false;
            for (int n = 0; n < numSquaresToEdge[start][directionIndex]; n++) {
                int target = start + directionOffsets[directionIndex] * (n + 1);
                int pieceOnTargetSquare = squares[target];

                if (!friendly) {
                    if (!moveBlocked) {
//                        if (isType(piece, ROOK)) {
//                            System.out.println("IS ROOK");
//                            System.out.println(target);
//                        }
                        taboo = addBit(taboo, target);
                        if (target == friendlyKingPosition) {
                            checkingMoves.add(squareMap.get(start) + squareMap.get(target));
                        }
                    }
                    if (pieceOnTargetSquare > 0 && !(isType(pieceOnTargetSquare, KING) && isColour(pieceOnTargetSquare, friendlyColour))) {
                        moveBlocked = true;
                    }
                    tabooXRay = addBit(tabooXRay, target);
                    continue;
                }
                if (isColour(pieceOnTargetSquare, friendlyColour)) {
                    moveBlocked = true;
                }
                if (!moveBlocked) {
                    addMove(start, target);
                }
                if (isColour(pieceOnTargetSquare, opponentColour)) {
                    moveBlocked = true;
                }
            }
        }
    }

    private static long addBit(long bitboard, int position) {
        return bitboard | (1L << position);
    }

    private static long clearBit(long bitboard, int position) {
        return (bitboard ^ (1L << position));
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

    private static void addMove(int start, int target) {
        if (target < 0 || target > 63) {
            return;
        }
        moves.add(squareMap.get(start) + squareMap.get(target));
    }

    private static void addMove(String move) {
        moves.add(move);
    }

    private static void addKingMove(int start, int target) {
        if (target < 0 || target > 63) {
            return;
        }
        kingMoves.add(squareMap.get(start) + squareMap.get(target));
    }
}
