package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of a field is larger than the one expected by the structure
 */
public class FieldOverFlowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param message error message
     */
    public FieldOverFlowException(String message) {
        super(message);
    }
}
