package io.github.epi155.recfm.java.rule;


public interface AccessField<T> {
    void access(T fld, String wrkName, boolean doc);
}
