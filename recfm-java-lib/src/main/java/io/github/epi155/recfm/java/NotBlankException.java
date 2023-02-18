package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not all SPACE chars
 */
public class NotBlankException extends SetterException {
    /**
     * Constructor
     *
     * @param c offending char
     * @param u relative char offset
     */
    public NotBlankException(char c, int u) {
        super(c, u);
    }
}
