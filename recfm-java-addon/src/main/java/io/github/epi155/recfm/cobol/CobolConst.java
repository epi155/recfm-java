package io.github.epi155.recfm.cobol;

import org.apache.commons.text.StringEscapeUtils;

public class CobolConst extends CobolValue {
    private final String value;

    public CobolConst(int level, int offset, int length, String value) {
        super(level, "filler", offset, length);
        this.value = value;
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                spacePic() +
                " PIC X(" + length() + ") VALUE '" + StringEscapeUtils.escapeJava(value) + "'."
//                + tail()
                ;
    }
}
