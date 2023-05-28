package io.github.epi155.recfm.type;

public class ClassDefineException extends RuntimeException {
    public ClassDefineException(String s) {
        super(s);
    }

    public ClassDefineException(Throwable t) {
        super(t.toString(), t);
    }
}
