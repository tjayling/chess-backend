package org.gui.exception;

public class InvalidFenFileException extends RuntimeException {
    public InvalidFenFileException(String message) {
        super(message);
    }

    public InvalidFenFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
