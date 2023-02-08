package io.github.epi155.recfm.java;

/**
 * Detail of a Field Validate Error
 */
public interface FieldValidateError {
    /**
     * field name in error
     * @return field name
     */
    String name();

    /**
     * field offset in error
     * @return field offset
     */
    int offset();

    /**
     * field length in error
     * @return field length
     */
    int length();

    /**
     * field value in error.
     * @return field value
     */
    String value();

    /**
     * column of the record with the wrong character
     * @return column of the record
     */
    Integer column();

    /**
     * error category
     * @return error category
     */
    ValidateError code();

    /**
     * wrong character
     * @return wrong character
     */
    Character wrong();

    /**
     * field message error
     * @return message error
     */
    String message();
}
