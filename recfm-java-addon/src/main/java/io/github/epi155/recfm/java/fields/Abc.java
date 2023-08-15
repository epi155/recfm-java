package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.NormalizeAbcMode;
import io.github.epi155.recfm.java.JavaDoc;
import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.Action;
import io.github.epi155.recfm.java.rule.MutableField;
import io.github.epi155.recfm.type.FieldAbc;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;
import static io.github.epi155.recfm.util.Tools.notNullOf;

public class Abc extends DelegateWriter implements MutableField<FieldAbc>, JavaDoc {
    private final FieldDefault.AbcDefault defaults;

    public Abc(CodeWriter pw, FieldDefault.AbcDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }

    public Abc(CodeWriter pw, FieldDefault.AbcDefault defaults, IntFunction<String> pos) {
        super(pw, pos);
        this.defaults = defaults;
    }

    public void initialize(@NotNull FieldAbc fld, int bias) {
        printf("    fill(%5d, %4d, ' ');%n", fld.getOffset() - bias, fld.getLength());
    }

    public void validate(@NotNull FieldAbc fld, int w, @NotNull IntFunction<String> bias, @NotNull AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.get());
        switch (notNullOf(fld.getCheck(), defaults.getCheck())) {
            case None:
                break;
            case Ascii:
                printf("%s checkAscii(mode, \"%s\"%s, %-5s, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), bias.apply(fld.getOffset()), fld.getLength());
                isFirst.set(false);
                break;
            case Latin1:
                printf("%s checkLatin(mode, \"%s\"%s, %-5s, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), bias.apply(fld.getOffset()), fld.getLength());
                isFirst.set(false);
                break;
            case Valid:
                printf("%s checkValid(mode, \"%s\"%s, %-5s, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), bias.apply(fld.getOffset()), fld.getLength());
                isFirst.set(false);
                break;
        }
    }

    public void access(FieldAbc fld, String wrkName, boolean doc) {
        buildGetter(fld, wrkName, doc);
        buildSetter(fld, wrkName, doc);
    }

    private void buildSetter(FieldAbc fld, String wrkName, boolean doc) {
        if (doc) docSetter(fld);
        printf("public void set%s(String s) {%n", wrkName);
        boolean chkSet = notNullOf(fld.getCheckSetter(), defaults.isCheckSetter());
        if (chkSet) chkSetter(fld);
        val align = fld.getAlign();
        val ovfl = notNullOf(fld.getOnOverflow(), defaults.getOnOverflow());
        val unfl = notNullOf(fld.getOnUnderflow(), defaults.getOnUnderflow());
        val flag = Action.flagSetter(ovfl, unfl, align);
        printf("    setAbc(s, %s, %d, %d, '%c', ' ');%n",
            pos.apply(fld.getOffset()), fld.getLength(), flag, fld.getPadChar());
        printf("}%n");
    }

    private void buildGetter(FieldAbc fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld);
        printf("public String get%s() {%n", wrkName);
        boolean chkGet = notNullOf(fld.getCheckGetter(), defaults.isCheckGetter());
        if (chkGet) chkGetter(fld);
        val norm = notNullOf(fld.getNormalize(), defaults.getNormalize());
        if (norm == NormalizeAbcMode.None) {
            printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        } else {
            printf("    return getAbc(%s, %d, %d, ' ');%n",
                pos.apply(fld.getOffset()), fld.getLength(), Action.flagGetter(norm, fld.getAlign()));
        }
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
