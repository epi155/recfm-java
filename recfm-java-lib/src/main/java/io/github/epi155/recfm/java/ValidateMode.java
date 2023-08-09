package io.github.epi155.recfm.java;

public enum ValidateMode {
    /**
     * Fail First Mode
     */
    FAIL_FIRST {
        @Override
        public boolean isFailFirst() {
            return true;
        }
    },
    /**
     * Fail All Mode
     */
    FAIL_ALL {
        @Override
        public boolean isFailFirst() {
            return false;
        }
    };

    /**
     * Behavoiur for many error on single field
     * @return fail mode for single field
     */
    public abstract boolean isFailFirst();
}
