package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of the structure is smaller than expected
 */
public class RecordUnderflowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param message error message
     */
    public RecordUnderflowException(String message) {
        super(message);
    }
}
