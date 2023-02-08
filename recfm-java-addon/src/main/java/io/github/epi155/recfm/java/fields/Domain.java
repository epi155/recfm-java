package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.JavaDoc;
import io.github.epi155.recfm.type.FieldDomain;
import io.github.epi155.recfm.util.*;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;

public class Domain extends IndentPrinter implements MutableField<FieldDomain>, JavaDoc {
    public Domain(PrintWriter pw) {
        super(pw);
    }

    public Domain(PrintWriter pw, IntFunction<String> pos) {
        super(pw, pos);
    }

    public void access(FieldDomain fld, String wrkName, int indent, @NotNull GenerateArgs ga) {
        pushIndent(indent);
        if (ga.doc) docGetter(fld);
        printf("public String get%s() {%n", wrkName);
        if (ga.getCheck) chkGetter(fld);
        printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (ga.doc) docSetter(fld);
        printf("public void set%s(String s) {%n", wrkName);
        if (ga.setCheck) chkSetter(fld);
        printf("    setAbc(s, %s, %d);%n",
                pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        popIndent();
    }

    private void chkSetter(@NotNull FieldDomain fld) {
        printf("    testArray(s, DOMAIN_AT%sPLUS%d);%n", pos.apply(fld.getOffset() + 1), fld.getLength());
    }

    private void chkGetter(@NotNull FieldDomain fld) {
        printf("    testArray(%1$s, %2$d, DOMAIN_AT%3$sPLUS%2$d);%n", pos.apply(fld.getOffset()), fld.getLength(), pos.apply(fld.getOffset() + 1));
    }

    public void initialize(@NotNull FieldDomain fld, int bias) {
        printf("        fill(%5d, %4d, VALUE_AT%dPLUS%d);%n",
                fld.getOffset() - bias, fld.getLength(), fld.getOffset(), fld.getLength());
    }

    public void validate(@NotNull FieldDomain fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkArray(\"%s\"%s, %5d, %4d, handler, DOMAIN_AT%dPLUS%d);%n", prefix,
                fld.getName(), fld.pad(w),
                fld.getOffset() - bias, fld.getLength(),
                fld.getOffset(), fld.getLength()
        );
    }

    @Override
    public String tag() {
        return "Dom";
    }
}
