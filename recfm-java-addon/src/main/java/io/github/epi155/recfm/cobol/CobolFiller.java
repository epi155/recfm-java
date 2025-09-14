package io.github.epi155.recfm.cobol;

public class CobolFiller extends CobolValue {
    public CobolFiller(int level, int offset, int length) {
        super(level, "filler", offset, length);
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                spacePic() +
                " PIC X(" + length() + ")."
//                + tail()
                ;
    }

}
