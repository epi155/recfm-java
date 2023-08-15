package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.ImmutableField;
import io.github.epi155.recfm.java.rule.ValidateField;
import io.github.epi155.recfm.type.FieldConstant;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;

public class Constant extends DelegateWriter implements ImmutableField<FieldConstant>, ValidateField<FieldConstant> {
    public Constant(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void initialize(@NotNull FieldConstant fld, int bias) {
        printf("    fill(%5d, %4d, VALUE_AT%dPLUS%d);%n",
                fld.getOffset() - bias, fld.getLength(), fld.getOffset(), fld.getLength());
    }
    @Override
    public void validate(@NotNull FieldConstant fld, int w, @NotNull IntFunction<String> pos, @NotNull AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkEqual(%s %-5s, %4d, handler, VALUE_AT%dPLUS%d);%n", prefix, fld.pad(-3, w),
                pos.apply(fld.getOffset()), fld.getLength(), fld.getOffset(), fld.getLength());
    }
}
