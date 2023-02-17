package io.github.epi155.recfm.java;

/**
 * Generic fixed record structure constructor
 */
public abstract class FixRecord extends FixEngine implements FixBasic {
    /**
     * Constructor with default values
     *
     * @param length structure length
     */
    protected FixRecord(int length) {
        super(length);
    }

    /**
     * Constructor with string as initial record value
     *
     * @param s              string initial value
     * @param length         structure length
     * @param overflowError  overflow behaviour
     * @param underflowError underflow behaviour
     */
    protected FixRecord(String s, int length, boolean overflowError, boolean underflowError) {
        super(s.toCharArray(), length, overflowError, underflowError);
    }

    /**
     * Constructor with another record as initial record value
     *
     * @param r              other record
     * @param lrecl          structure lenght
     * @param overflowError  overflow behaviour
     * @param underflowError underflow behaviour
     */
    protected FixRecord(FixRecord r, int lrecl, boolean overflowError, boolean underflowError) {
        super(r.rawData, lrecl, overflowError, underflowError);
    }

    /**
     * Constructor with char array as initial record value
     *
     * @param c              char array
     * @param lrecl          structure lenght
     * @param overflowError  overflow behaviour
     * @param underflowError underflow behaviour
     */
    protected FixRecord(char[] c, int lrecl, boolean overflowError, boolean underflowError) {
        super(c, lrecl, overflowError, underflowError);
    }
}
