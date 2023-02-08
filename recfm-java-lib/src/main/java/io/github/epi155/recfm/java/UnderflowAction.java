package io.github.epi155.recfm.java;

/**
 * Action on field / record underflow
 */
public enum UnderflowAction {
    /**
     * throws FieldUnderFlowException / RecordUnderflowException
     */
    Error,
    /**
     * Pad right
     */
    PadR,
    /**
     * Pad left
     */
    PadL
}
