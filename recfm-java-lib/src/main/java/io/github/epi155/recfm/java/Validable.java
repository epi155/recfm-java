package io.github.epi155.recfm.java;

/**
 * Generic method of any fixed record structure
 */
public interface Validable {
    /**
     * Validate all fields (fail first).
     *
     * @param handler   handler called when an Error is detected
     * @return <b>true</b> if there is any mistake, <b>false</b> if there are no errors
     */
    boolean validateFails(FieldValidateHandler handler);
    /**
     * Validate all fields (fail all).
     *
     * @param handler   handler called when an Error is detected
     * @return <b>true</b> if there is any mistake, <b>false</b> if there are no errors
     */
    boolean validateAllFails(FieldValidateHandler handler);
}
