package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not all valid UTF-8 chars
 */
public class NotValidException extends SetterException {
    /**
     * Constructor
     *
     * @param c offending char
     * @param u relative char offset
     */
    public NotValidException(char c, int u) {
        super(c, u);
    }
    /**
     * Constructor
     *
     * @param c offending char
     * @param offs field offset
     * @param pos offending char offset
     */
    public NotValidException(char c, int offs, int pos) {
        super(c, offs, pos);
    }
}
