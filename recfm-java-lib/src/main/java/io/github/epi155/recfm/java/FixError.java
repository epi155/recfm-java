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

    static String explainChar(char wrong) {
        String charName = Character.getName(wrong);
        if (charName == null) {
            return String.format("(U+%04X) [unassigned char]", (int) wrong);
        }
        Character.UnicodeBlock block = Character.UnicodeBlock.of(wrong);
        if (Character.isISOControl(wrong) ||
                block == null ||
                block == Character.UnicodeBlock.SPECIALS) {
            return String.format("(U+%04X) [%s]", (int) wrong, charName);
        } else {
            return String.format("'%c' [%s]", wrong, charName);
        }
    }
}
