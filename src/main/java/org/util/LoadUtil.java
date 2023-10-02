package org.util;

import org.gui.exception.InvalidFenFileException;

import java.io.File;
import java.util.Scanner;

import static org.util.FenUtil.checkValidityOfFenString;

public class LoadUtil {
    public static String loadFenFromFile(String url) throws InvalidFenFileException {
        String fenString;
        try {
            File fenFile = new File(url);
            Scanner scanner = new Scanner(fenFile);
            fenString = scanner.nextLine();
            checkValidityOfFenString(fenString);
            return fenString;
        } catch (Exception e) {
            throw new InvalidFenFileException("Invalid FEN string encountered in the file: " + url, e);
        }
    }

    public static String loadFenFromFile() {
        try {
            return loadFenFromFile("src/main/resources/boards/initial-board.fen");
        } catch (InvalidFenFileException ex) {
            throw new InvalidFenFileException("Invalid FEN string encountered in the file: src/main/resources/boards/initial-board.fen", ex);
        }
    }
}
