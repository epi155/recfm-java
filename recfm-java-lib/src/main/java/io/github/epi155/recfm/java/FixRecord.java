package io.github.epi155.recfm.java;

public abstract class FixRecord extends FixEngine implements FixBasic {
    protected FixRecord(int length) {
        super(length);
    }

    protected FixRecord(String s, int length, boolean overflowError, boolean underflowError) {
        super(s.toCharArray(), length, overflowError, underflowError);
    }

    protected FixRecord(FixRecord r, int lrecl, boolean overflowError, boolean underflowError) {
        super(r.rawData, lrecl, overflowError, underflowError);
    }

    protected FixRecord(char[] c, int lrecl, boolean overflowError, boolean underflowError) {
        super(c, lrecl, overflowError, underflowError);
    }
}
