package io.github.epi155.recfm.java;

/**
 * Generic method of any fixed record structure
 */
public interface Validable {
    /**
     * Validate all fields.
     *
     * @param handler   handler called when an rrror is detected
     * @return <b>true</b> if there is any mistake, <b>false</b> if there are no errors
     */
    boolean validateFails(FieldValidateHandler handler);
}
