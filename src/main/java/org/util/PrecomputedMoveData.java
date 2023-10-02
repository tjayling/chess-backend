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
    public static final long[] CASTLE_MASKS = new long[4];
    public static final long[] NORTH_ONE = new long[64];
    public static final long[] NORTH_EAST_ONE = new long[64];
    public static final long[] EAST_ONE = new long[64];
    public static final long[] SOUTH_EAST_ONE = new long[64];
    public static final long[] SOUTH_ONE = new long[64];
    public static final long[] SOUTH_WEST_ONE = new long[64];
    public static final long[] WEST_ONE = new long[64];
    public static final long[] NORTH_WEST_ONE = new long[64];
    public static final long[][] KING_MOVES = new long[64][8];
    public static final long[][] KNIGHT_TARGETS = new long[64][8];
    public static final int[][] NUM_SQUARES_TO_EDGE = new int[64][8];

    static {
        for (int i = 0; i < 64; i++) {
            long currentSquare = BinUtil.createBitboard(i);

            NORTH_ONE[i] = BinUtil.northOne(currentSquare);
            NORTH_EAST_ONE[i] = BinUtil.northEastOne(currentSquare);
            EAST_ONE[i] = BinUtil.eastOne(currentSquare);
            SOUTH_EAST_ONE[i] = BinUtil.southEastOne(currentSquare);
            SOUTH_ONE[i] = BinUtil.southOne(currentSquare);
            SOUTH_WEST_ONE[i] = BinUtil.southWestOne(currentSquare);
            WEST_ONE[i] = BinUtil.westOne(currentSquare);
            NORTH_WEST_ONE[i] = BinUtil.northWestOne(currentSquare);

            KING_MOVES[i] = new long[]{
                    NORTH_ONE[i],
                    NORTH_EAST_ONE[i],
                    EAST_ONE[i],
                    SOUTH_EAST_ONE[i],
                    SOUTH_ONE[i],
                    SOUTH_WEST_ONE[i],
                    WEST_ONE[i],
                    NORTH_WEST_ONE[i]
            };

            KNIGHT_TARGETS[i] = new long[]{
                    ((currentSquare << 17) & NOT_A_FILE),
                    ((currentSquare << 10) & NOT_AB_FILE),
                    ((currentSquare >>> 6) & NOT_AB_FILE),
                    ((currentSquare >>> 15) & NOT_A_FILE),
                    ((currentSquare << 15) & NOT_H_FILE),
                    ((currentSquare << 6) & NOT_GH_FILE),
                    ((currentSquare >>> 10) & NOT_GH_FILE),
                    ((currentSquare >>> 17) & NOT_H_FILE)
            };
        }

        CASTLE_MASKS[0] = 0x70L; // White king side
        CASTLE_MASKS[1] = 0x1CL; // White queen side
        CASTLE_MASKS[2] = 0x7000000000000000L; // Black king side
        CASTLE_MASKS[3] = 0x1C00000000000000L; // Black queen side


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
    }
}
