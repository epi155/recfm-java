package io.github.epi155.recfm.java.factory;

import java.util.function.IntFunction;

public class DelegateWriter {
    private final CodeWriter delegate;
    protected final IntFunction<String> pos;

    public DelegateWriter(CodeWriter pw) {
        this.delegate = pw;
        this.pos = String::valueOf;
    }

    public DelegateWriter(CodeWriter pw, IntFunction<String> pos) {
        this.delegate = pw;
        this.pos = pos;
    }

    public void printf(String format, Object... args) {
        delegate.printf(format, args);
    }
    protected void pushIndent(int indent) {
        delegate.pushIndent(indent);
    }
    protected void popIndent() {
        delegate.popIndent();
    }

}
