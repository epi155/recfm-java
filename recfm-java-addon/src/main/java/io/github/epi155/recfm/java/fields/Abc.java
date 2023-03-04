package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.JavaDoc;
import io.github.epi155.recfm.type.Defaults;
import io.github.epi155.recfm.type.FieldAbc;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.IndentPrinter;
import io.github.epi155.recfm.util.MutableField;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;
import static io.github.epi155.recfm.util.Tools.notNullOf;

public class Abc extends IndentPrinter implements MutableField<FieldAbc>, JavaDoc {
    private final Defaults.AbcDefault defaults;

    public Abc(PrintWriter pw, Defaults.AbcDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }

    public Abc(PrintWriter pw, Defaults.AbcDefault defaults, IntFunction<String> pos) {
        super(pw, pos);
        this.defaults = defaults;
    }

    public void initialize(@NotNull FieldAbc fld, int bias) {
        printf("        fill(%5d, %4d, ' ');%n", fld.getOffset() - bias, fld.getLength());
    }

    public void validate(@NotNull FieldAbc fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.get());
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("%s checkAscii(\"%s\"%s, %5d, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Latin1:
                printf("%s checkLatin(\"%s\"%s, %5d, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case Valid:
                printf("%s checkValid(\"%s\"%s, %5d, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
        }
    }

    public void access(FieldAbc fld, String wrkName, int indent, @NotNull GenerateArgs ga) {
        pushIndent(indent);
        buildGetter(fld, wrkName, ga);
        buildSetter(fld, wrkName, ga);
        popIndent();
    }

    private void buildSetter(FieldAbc fld, String wrkName, GenerateArgs ga) {
        if (ga.doc) docSetter(fld);
        printf("public void set%s(String s) {%n", wrkName);
        if (ga.setCheck) chkSetter(fld);
        val align = fld.getAlign();
        printf("    setAbc(s, %s, %d, OverflowAction.%s, UnderflowAction.%s, '%c', ' ');%n",
                pos.apply(fld.getOffset()), fld.getLength(), fld.safeOverflow().of(align),
                fld.safeUnderflow().of(align), fld.getPadChar());
        printf("}%n");
    }

    private void buildGetter(FieldAbc fld, String wrkName, GenerateArgs ga) {
        if (ga.doc) docGetter(fld);
        printf("public String get%s() {%n", wrkName);
        if (ga.getCheck) chkGetter(fld);
        printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
    }


    private void chkGetter(@NotNull FieldAbc fld) {
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("    testAscii(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case Latin1:
                printf("    testLatin(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case Valid:
                printf("    testValid(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
        }
    }

    private void chkSetter(@NotNull FieldAbc fld) {
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("    testAscii(s);%n");
                break;
            case Latin1:
                printf("    testLatin(s);%n");
                break;
            case Valid:
                printf("    testValid(s);%n");
                break;
        }
    }

    @Override
    public String tag() {
        return "Abc";
    }
}
