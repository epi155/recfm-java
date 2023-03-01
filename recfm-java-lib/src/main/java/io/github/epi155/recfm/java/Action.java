package io.github.epi155.recfm.java;

/**
 * Actions on the value of a field
 */
public class Action {
    private Action() {}

    /**
     * Normalization actions
     */
    public enum Normalize {
        /**
         * The value does not undergo any modification
         */
        None,
        /**
         * All padding characters to the right are removed
         */
        RTrim,
        /**
         * All padding characters to the right are removed up to the minimum length of a single character value
         */
        RTrim1,
        /**
         * All padding characters to the left are removed
         */
        LTrim,
        /**
         * All padding characters to the left are removed up to the minimum length of a single character value
         */
        LTrim1
    }

    /**
     * Action on field / record overflow
     */
    public enum Overflow {
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

    /**
     * Action on field / record underflow
     */
    public enum Underflow {
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
}
