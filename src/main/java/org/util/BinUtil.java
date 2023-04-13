package org.util;

public class BinUtil {
    public static void printBin(long n) {
        System.out.println(String.format("%64s", Long.toBinaryString(n)).replace(' ', '0'));
    }
}
