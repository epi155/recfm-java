package io.github.epi155.recfm.java.rule;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

public interface ValidateField<T> {
    void validate(T fld, int padWidth, @NotNull IntFunction<String> pos, @NotNull AtomicBoolean firstStatement);
}
