package io.github.epi155.recfm.type;

public abstract class SettableField extends NamedField{

    public abstract String picture();

    public String pad(int w) {
        return pad(getName().length(), w);
    }
}
