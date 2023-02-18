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
    public NotLatinException(char c, int u) {
        super(c, u);
    }
    /**
     * Constructor
     *
     * @param c offending char
     * @param offs field offset
     * @param pos offending char offset
     */
    public NotLatinException(char c, int offs, int pos) {
        super(c, offs, pos);
    }
}
