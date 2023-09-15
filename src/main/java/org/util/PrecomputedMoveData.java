package org.util;

import java.util.ArrayList;
import java.util.List;

public class PrecomputedMoveData {
    public static final List<String> SQUARE_MAP = new ArrayList<>();
    public static final long RANK_4 = 0x00000000FF000000L;
    public static final long RANK_5 = 0x000000FF00000000L;
    public static final long NOT_A_FILE = 0xFEFEFEFEFEFEFEFEL;
    public static final long NOT_H_FILE = 0x7F7F7F7F7F7F7F7FL;
    public static final long NOT_AB_FILE = 0xFCFCFCFCFCFCFCFCL;
    public static final long NOT_GH_FILE = 0x3F3F3F3F3F3F3F3FL;
    public static final char[] promotionPieces = new char[]{'r', 'n', 'b', 'q'};
    // north, south, east, west, south-east, north-west, south-west, north-east
    public static final int[] DIRECTION_OFFSETS = {8, -8, -1, 1, 7, -7, 9, -9};

    public static final int[][] NUM_SQUARES_TO_EDGE = new int[64][8];

    public static final long[] CASTLE_MASKS = {
            0x70L, // White king side
            0x1CL, // White queen side
            0x7000000000000000L, // Black king side
            0x1C00000000000000L // Black queen side
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
