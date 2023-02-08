package io.github.epi155.recfm.java;

/**
 * Action on field / record overflow
 */
public enum OverflowAction {
    /**
     * throws FieldOverFlowException / RecordOverflowException
     */
    Error,
    /**
     * Truncate right
     */
    TruncR,
    /**
     * Truncate left
     */
    TruncL,
}
