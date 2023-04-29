package org.util;

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

        System.out.println(stringBuilder.toString());
    }

    public static long addBit(long bitboard, int position) {
        return bitboard | (1L << position);
    }
}
