package io.github.epi155.recfm.java.factory;

public interface CodeWriter {
    void printf(String format, Object[] args);
    void pushIndent(int indent);
    void popIndent();
}
