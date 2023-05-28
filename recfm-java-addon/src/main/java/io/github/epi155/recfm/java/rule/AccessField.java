package io.github.epi155.recfm.java.rule;


import io.github.epi155.recfm.api.GenerateArgs;

public interface AccessField<T> {
    void access(T fld, String wrkName, GenerateArgs ga);
}
