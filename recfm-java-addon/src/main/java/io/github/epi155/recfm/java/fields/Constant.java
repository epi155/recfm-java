package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.type.FieldConstant;
import io.github.epi155.recfm.util.ImmutableField;
import io.github.epi155.recfm.util.IndentPrinter;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;

public class Constant extends IndentPrinter implements ImmutableField<FieldConstant> {
    public Constant(PrintWriter pw) {
        super(pw);
    }

    public void initialize(@NotNull FieldConstant fld, int bias) {
        printf("        fill(%5d, %4d, VALUE_AT%dPLUS%d);%n",
                fld.getOffset() - bias, fld.getLength(), fld.getOffset(), fld.getLength());
    }

    public void validate(@NotNull FieldConstant fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkEqual(%s %5d, %4d, handler, VALUE_AT%dPLUS%d);%n", prefix, fld.pad(-3, w),
                fld.getOffset() - bias, fld.getLength(), fld.getOffset(), fld.getLength());
    }
}
