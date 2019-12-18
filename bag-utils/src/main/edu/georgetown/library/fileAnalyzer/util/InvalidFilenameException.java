package edu.georgetown.library.fileAnalyzer.util;

public class InvalidFilenameException extends Exception {
    private static final long serialVersionUID = 1L;

    InvalidFilenameException(String s) {
        super(s);
    }

}
