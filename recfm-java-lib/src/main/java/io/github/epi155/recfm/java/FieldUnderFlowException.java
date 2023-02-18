package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of a field is smaller than the one expected by the structure
 */
public class FieldUnderFlowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param message error message
     */
    public FieldUnderFlowException(String message) {
        super(message);
    }
}
