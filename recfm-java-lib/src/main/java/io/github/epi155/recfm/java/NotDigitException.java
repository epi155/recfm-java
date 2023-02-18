package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not all digit chars
 */
public class NotDigitException extends SetterException {
    /**
     * Constructor
     *
     * @param c offending char
     * @param u relative char offset
     */
    public NotDigitException(char c, int u) {
        super(c, u);
    }
}
