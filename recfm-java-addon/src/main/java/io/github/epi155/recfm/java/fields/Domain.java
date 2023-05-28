package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.api.GenerateArgs;
import io.github.epi155.recfm.java.JavaDoc;
import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.MutableField;
import io.github.epi155.recfm.type.FieldDomain;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;

public class Domain extends DelegateWriter implements MutableField<FieldDomain>, JavaDoc {
    public Domain(CodeWriter pw) {
        super(pw);
    }

    public Domain(CodeWriter pw, IntFunction<String> pos) {
        super(pw, pos);
    }

    public void access(FieldDomain fld, String wrkName, @NotNull GenerateArgs ga) {
        if (ga.doc) docGetter(fld);
        printf("public String get%s() {%n", wrkName);
        printf("    testArray(%1$s, %2$d, DOMAIN_AT%3$sPLUS%2$d);%n", pos.apply(fld.getOffset()), fld.getLength(), pos.apply(fld.getOffset() + 1));
        printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (ga.doc) docSetter(fld);
        printf("public void set%s(String s) {%n", wrkName);
        printf("    testArray(s, DOMAIN_AT%sPLUS%d);%n", pos.apply(fld.getOffset() + 1), fld.getLength());
        printf("    setDom(s, %s, VALUE_AT%dPLUS%d);%n",
                pos.apply(fld.getOffset()), fld.getOffset(), fld.getLength());
        printf("}%n");
    }

    public void initialize(@NotNull FieldDomain fld, int bias) {
        printf("    fill(%5d, %4d, VALUE_AT%dPLUS%d);%n",
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
