package io.github.epi155.recfm.java;

public class ValidateContext {
    private ValidateContext() {}
    private static final ThreadLocal<Boolean> failMode = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.TRUE;
        }
    };
     /**
     * sets the behavior in case of multiple errors on the same field: report <b>the first error</b>
     */
    public static void setFirst() { failMode.set(true); }
    /**
     * sets the behavior in case of multiple errors on the same field: report <b>all errors</b>
     */
    public static void setAll() { failMode.set(false); }

    /**
     * indicates how to behave in case of multiple errors on the same field
     * @return <b>true</b>first error, <b>false</b> all errors
     */
    public static boolean isFailFirst() {
        return failMode.get();
    }

    /**
     * remove ThreadLocal variable
     */
    public static void unset() {
        failMode.remove();
    }
}
