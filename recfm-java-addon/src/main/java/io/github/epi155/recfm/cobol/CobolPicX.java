package io.github.epi155.recfm.cobol;

public class CobolPicX extends CobolValue {
    public CobolPicX(int level, String name, int offset, int length) {
        super(level, name, offset, length);
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                spacePic() +
                " PIC X(" + length() +")."
//                + tail()
                ;
    }
}
