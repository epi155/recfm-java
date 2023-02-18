package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not all ascii chars
 */
public class NotAsciiException extends SetterException {
    /**
     * Constructor
     *
     * @param c offending char
     * @param offs field offset
     * @param pos offending char offset
     */
    public NotAsciiException(char c, int offs, int pos) {
        super(c, offs, pos);
    }
    /**
     * Constructor
     *
     * @param c offending char
     * @param pos offending char offset
     */
    public NotAsciiException(char c, int pos) {
        super(c, pos);
    }
}
