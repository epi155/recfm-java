package io.github.epi155.recfm.java;

/**
 * Actions on the value of a field
 */
public class Action {
    /**
     * The value does not undergo any modification
     */
    public static final int F_NRM_NOX = 0x00;
    /**
     * All padding characters to the right are removed
     */
    public static final int F_NRM_RT0 = 0x01;
    /**
     * All padding characters to the right are removed up to the minimum length of a single character value
     */
    public static final int F_NRM_RT1 = 0x02;
    /**
     * All padding characters to the left are removed
     */
    public static final int F_NRM_LT0 = 0x03;
    /**
     * All padding characters to the left are removed up to the minimum length of a single character value
     */
    public static final int F_NRM_LT1 = 0x04;
    public static final int F_NRM_MSK = 0x07;

    /**
     * throws FieldOverFlowException / RecordOverflowException
     */
    public static final int F_OVF_ERR = 0x00 << 3;
    /**
     * Truncate right
     */
    public static final int F_OVF_TRR = 0x01 << 3;
    /**
     * Truncate left
     */
    public static final int F_OVF_TRL = 0x02 << 3;
    public static final int F_OVF_MSK = 0x03 << 3;

    /**
     * throws FieldUnderFlowException / RecordUnderflowException
     */
    public static final int F_UNF_ERR = 0x00 << 5;
    /**
     * Pad right
     */
    public static final int F_UNF_PAR = 0x01 << 5;
    /**
     * Pad left
     */
    public static final int F_UNF_PAL = 0x02 << 5;
    public static final int F_UNF_MSK = 0x03 << 5;
    private Action() {}

}
