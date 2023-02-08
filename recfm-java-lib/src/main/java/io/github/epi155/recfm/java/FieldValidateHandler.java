package io.github.epi155.recfm.java;

/**
 * Handler called on Field Validate Error
 */
public interface FieldValidateHandler {
    /**
     * method called when a validation error occurs
     * @param fieldValidateError    error details
     */
    void error(FieldValidateError fieldValidateError);
}
