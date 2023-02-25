package io.github.epi155.recfm.java;

public class Action {
    public enum Normalize {
        None, RTrim, RTrim1, LTrim, LTrim1
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
