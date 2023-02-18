package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not all ascii chars
 */
public class NotAsciiException extends SetterException {
    /**
     * Constructor
     *
     * @param c offending char
     * @param u relative char offset
     */
    public NotAsciiException(char c, int u) {
        super(c, u);
    }
}
