package io.github.epi155.recfm.java;

public class JavaTools {
    private JavaTools() {
    }

    public static String prefixOf(boolean isFirst) {
        if (isFirst) {
            return "        boolean error =";
        } else {
            return "        error |=";
        }
    }
}
