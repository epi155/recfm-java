package io.github.epi155.recfm.java;

/**
 * Collector of exceptions that can occur in the management of fixed-path records
 */
public class FixError {
    static final int RECORD_BASE = 1;

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
    static String explainString(String value) {
        StringBuilder sb = new StringBuilder();
        char[] ca = value.toCharArray();
        for (char c : ca) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (Character.isISOControl(c) ||
                !Character.isDefined(c) ||
                block == null ||
                block == Character.UnicodeBlock.SPECIALS) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();

    }
}
