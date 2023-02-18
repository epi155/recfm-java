package io.github.epi155.recfm.java;

/**
 * Collector of exceptions that can occur in the management of fixed-path records
 */
public class FixError {
    static volatile boolean failFirst = false;
    static final int RECORD_BASE = 1;

    /**
     * sets the behavior in case of multiple errors on the same field: report <b>the first error</b>
     */
    public static synchronized void failFirst() { failFirst = true; }

    /**
     * sets the behavior in case of multiple errors on the same field: report <b>all errors</b>
     */
    public static synchronized void failAll() { failFirst = false; }

    private FixError() {
    }

}
