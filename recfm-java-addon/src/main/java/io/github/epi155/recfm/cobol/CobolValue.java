package io.github.epi155.recfm.cobol;

import lombok.Getter;
import lombok.Setter;

import java.nio.CharBuffer;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class CobolValue extends CobolEntry {
    @Getter
    private final int length;
    @Getter
    private final int offset;
    @Setter
    private NumberFormat format;

    protected CobolValue(int level, String name, int offset, int length) {
        super(level, name);
        this.offset = offset;
        this.length = length;
    }
    protected String length() {
        return format.format(length);
    }

    protected String tail() {
        return "    ! " + lpad(OFFSET_FORMAT.format(getOffset()), 9);
    }

    private static final Map<Integer, String> PAD = new HashMap<>();
    private static String lpad(String s, int wid) {
        int len = s.length();
        if (len > wid) return s.substring(0, wid);
        if (len == wid) return s;
        String pad = PAD.computeIfAbsent(wid-len, p -> CharBuffer.allocate(p).toString().replace('\u0000', ' '));
        return pad+s;
    }
}
