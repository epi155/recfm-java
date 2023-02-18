package io.github.epi155.recfm.java;

/**
 * Exception thrown if the value of field is not in permitted domain
 */
public class NotDomainException extends SetterException {
    /**
     * Constructor
     *
     * @param value offending value
     */
    public NotDomainException(String value) {   // setter
        super(value);
    }

    /**
     * Constructor
     *
     * @param offset field offset
     * @param value  offending value
     */
    public NotDomainException(int offset, String value) {   // getter
        super(value, offset);
    }
}
