package io.github.epi155.recfm.cobol;

import org.apache.commons.text.StringEscapeUtils;

public class CobolDomain extends CobolValue {
    private final String[] items;

    public CobolDomain(int level, String name, int offset, int length, String[] items) {
        super(level, name, offset, length);
        this.items = items;
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                spacePic() +
                " PIC X(" + length() + ") VALUE '" + StringEscapeUtils.escapeJava(items[0]) + "'."
//                + tail()
                ;
    }
}
