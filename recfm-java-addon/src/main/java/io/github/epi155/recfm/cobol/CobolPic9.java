package io.github.epi155.recfm.cobol;

public class CobolPic9 extends CobolValue {
    public CobolPic9(int level, String name, int offset, int length) {
        super(level, name, offset, length);
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                spacePic() +
                " PIC 9(" + length() + ")."
//                + tail()
                ;
    }
}
