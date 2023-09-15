package org.util;

import java.util.ArrayList;
import java.util.List;

import static org.util.PrecomputedMoveData.NOT_A_FILE;
import static org.util.PrecomputedMoveData.NOT_H_FILE;

public class BinUtil {
    public static void printBin(long n) {
        String binaryString = (String.format("%64s", Long.toBinaryString(n)).replace(' ', '0'));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i++) {
            stringBuilder.append(binaryString.charAt(i));
            if ((i + 1) % 8 == 0) {
                stringBuilder.append("\n");
            }
        }

        System.out.println(stringBuilder);
    }


    /**
     * Gets a list of positions from a bitboard
     *
     * @param bitboard a bitboard representation of the board
     * @return a list of positions represented by the bitboard
     */
    public static List<Integer> getPositionsFromBitboard(long bitboard) {
        List<Integer> positions = new ArrayList<>();
        while (bitboard != 0) {
            int pos = getPositionFromBitboard(bitboard);
            positions.add(pos);
            bitboard ^= 1L << pos;
        }
        return positions;
    }

    /**
     * Returns a single position from a bitboard
     *
     * @param bitboard the bitboard to query
     * @return the position of the least significant bit
     */
    public static int getPositionFromBitboard(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
    }

    public static long addBit(long bitboard, int position) {
        return bitboard | (1L << position);
    }

    public static long northOne(long bit) {
        return bit << 8;
    }

    public static long northEastOne(long bit) {
        return (bit << 9) & NOT_A_FILE;
    }

    public static long eastOne(long bit) {
        return (bit << 1) & NOT_A_FILE;
    }

    public static long southEastOne(long bit) {
        return (bit >>> 7) & NOT_A_FILE;
    }

    public static long southOne(long bit) {
        return bit >>> 8;
    }

    public static long southWestOne(long bit) {
        return (bit >>> 9) & NOT_H_FILE;
    }

    public static long westOne(long bit) {
        return (bit >>> 1) & NOT_H_FILE;
    }

    public static long northWestOne(long bit) {
        return (bit << 7) & NOT_H_FILE;
    }

    public static int getMsb(int n) {
        return (n & 0xFFFFFFF0);
    }

    public static int getLsb(int n) {
        return (n & 0x0000000F);
    }
}
