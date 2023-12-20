package org.util;

import java.util.ArrayList;
import java.util.List;

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
    public static List<Integer> getPositions(long bitboard) {
        List<Integer> positions = new ArrayList<>();
        while (bitboard != 0) {
            int pos = getLastPosition(bitboard);
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
    public static int getLastPosition(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
    }

    public static long addBit(long bitboard, int position) {
        return bitboard | (1L << position);
    }

    public static long bitboardFromPosition(int position) {
        return 1L << position;
    }

    public static int getMsb(int n) {
        return (n & 0xFFFFFFF0);
    }

    public static int getLsb(int n) {
        return (n & 0x0000000F);
    }
}
