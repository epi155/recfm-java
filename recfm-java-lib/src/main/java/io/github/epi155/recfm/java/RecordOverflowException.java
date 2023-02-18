package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of the structure is larger than expected
 */
public class RecordOverflowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param message error message
     */
    public RecordOverflowException(String message) {
        super(message);
    }
}
