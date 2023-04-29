package org.logic;

import java.util.ArrayList;
import java.util.List;

public class PrecomputedMoveData {
    public static final List<String> SQUARE_MAP = new ArrayList<>();
    protected static final long notAFile = 0xFEFEFEFEFEFEFEFEL;
    protected static final long notHFile = 0x7F7F7F7F7F7F7F7FL;
    protected static final long notABFile = 0xFCFCFCFCFCFCFCFCL;
    protected static final long notGHFile = 0x3F3F3F3F3F3F3F3FL;
    // north, south, east, west, south-east, north-west, south-west, north-east
    protected static final int[] directionOffsets = {8, -8, -1, 1, 7, -7, 9, -9};

    protected static final int[][] NUM_SQUARES_TO_EDGE = new int[64][8];

    protected static final long[] CASTLE_MASKS = {
            0x70L, // White king side
            0x1CL, // White queen side
            0x7000000000000000L, // Black king side
            0x1C00000000000000L // Black queen side
    };

    protected static final int[] CASTLE_TARGET_SQUARES = {
            6, 2, 62, 58
    };

    protected static final int[][] CASTLE_INVOLVED_SQUARES = {
            {5, 6}, {1, 2, 3}, {61, 62}, {57, 58, 59}
    };

    static {
        // Compute squareMap values
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                SQUARE_MAP.add(String.valueOf((char) (97 + j)) + (i + 1));
            }
        }

        // Compute number of squares to edge for each square on the board
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                int numNorth = 7 - rank;
                int numEast = 7 - file;

                int squareIndex = rank * 8 + file;

                NUM_SQUARES_TO_EDGE[squareIndex] = new int[]{
                        numNorth,
                        rank,
                        file,
                        numEast,
                        Math.min(numNorth, file),
                        Math.min(rank, numEast),
                        Math.min(numNorth, numEast),
                        Math.min(rank, file),
                };
            }
        }

//        // Compute knight moves for each square on the board
//        for (int square = 0; square < 64; square++) {
//            long binSquare = 1L << square;
//            long knightMoves = 0L;
//
//            knightMoves |= ((binSquare << 17) & notAFile);
//            knightMoves |= ((binSquare << 10) & notABFile);
//            knightMoves |= ((binSquare >>> 6) & notABFile);
//            knightMoves |= ((binSquare >>> 15) & notAFile);
//            knightMoves |= ((binSquare << 15) & notHFile);
//            knightMoves |= ((binSquare << 6) & notGHFile);
//            knightMoves |= ((binSquare >>> 10) & notGHFile);
//            knightMoves |= ((binSquare >>> 17) & notHFile);
//
//            KNIGHT_MOVES[square] = knightMoves;
//        }
    }
}
