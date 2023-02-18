package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field does not match permitted regular expression pattern
 */
public class NotMatchesException extends SetterException {
    /**
     * Constructor
     *
     * @param value offending value
     */
    public NotMatchesException(String value) {
        super(value);
    }

    /**
     * Constructor
     *
     * @param offset field offset
     * @param value  offending value
     */
    public NotMatchesException(int offset, String value) {
        super(value, offset);
    }
}
