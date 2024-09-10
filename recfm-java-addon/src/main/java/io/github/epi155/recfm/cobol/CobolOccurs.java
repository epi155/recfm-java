package io.github.epi155.recfm.cobol;

public class CobolOccurs extends CobolEntry {
    private final int times;

    public CobolOccurs(int level, String name, int times) {
        super(level, name);
        this.times = times;
    }

    @Override
    public String code() {
        return spaceLevel() +
                level() + " " +
                name() +
                " OCCURS " + times + " TIMES.";
    }
}
