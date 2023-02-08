package io.github.epi155.recfm.java;

/**
 * Generic method of any fixed record structure
 */
public interface FixBasic {
    /**
     * Validate all fields.
     *
     * @param handler   handler called when an arror is detected
     * @return <b>true</b> if there is any mistake, <b>false</b> if there are no errors
     */
    boolean validateFails(FieldValidateHandler handler);

    /**
     * Validate audit marked fields.
     *
     * @param handler   handler called when an arror is detected
     * @return <b>true</b> if there is any mistake, <b>false</b> if there are no errors
     */
    boolean auditFails(FieldValidateHandler handler);

    /**
     * Serialize record
     * @return  serialized string
     */
    String encode();
}
