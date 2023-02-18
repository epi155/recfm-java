package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not all latin1 chars
 */
public class NotLatinException extends SetterException {
    /**
     * Constructor
     *
     * @param c offending char
     * @param u relative char offset
     */
    public NotLatinException(int c, int u) {
        super(c, u);
    }
}
