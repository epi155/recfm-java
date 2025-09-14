package io.github.epi155.recfm.cobol;

public class CobolGroup extends CobolEntry {
    public CobolGroup(int level, String name) {
        super(level, name);
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                ".";
    }
}
