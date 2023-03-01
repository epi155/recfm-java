package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.JavaDoc;
import io.github.epi155.recfm.type.Defaults;
import io.github.epi155.recfm.type.FieldCustom;
import io.github.epi155.recfm.type.NormalizeAbcMode;
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

public class Custom extends IndentPrinter implements MutableField<FieldCustom>, JavaDoc {
    private final Defaults.CusDefault defaults;

    public Custom(PrintWriter pw, Defaults.CusDefault defaults, IntFunction<String> pos) {
        super(pw, pos);
        this.defaults = defaults;
    }

    public Custom(PrintWriter pw, Defaults.CusDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }

    @Override
    public void initialize(@NotNull FieldCustom fld, int bias) {
        val init = notNullOf(fld.getInitChar(), defaults.getInit());
        printf("        fill(%5d, %4d, '%c');%n", fld.getOffset() - bias, fld.getLength(), init);
    }

    @Override
    public void validate(@NotNull FieldCustom fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.get());
        if (fld.getRegex() != null) {
            printf("%s checkRegex(\"%s\"%s, %5d, %4d, handler, PATTERN_AT%dPLUS%d);%n", prefix,
                    fld.getName(), fld.pad(w),
                    fld.getOffset() - bias, fld.getLength(),
                    fld.getOffset(), fld.getLength()
            );
            isFirst.set(false);
            return;
        }
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
            case Digit:
                printf("%s checkDigit(\"%s\"%s, %5d, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
            case DigitOrBlank:
                printf("%s checkDigitBlank(\"%s\"%s, %5d, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
                isFirst.set(false);
                break;
        }
    }

    @Override
    public void access(FieldCustom fld, String wrkName, int indent, @NotNull GenerateArgs ga) {
        pushIndent(indent);
        buildGetter(fld, wrkName, ga);
        buildSetter(fld, wrkName, ga);
        buildInitialize(fld, wrkName, ga);
        popIndent();
    }

    private void buildInitialize(FieldCustom fld, String wrkName, GenerateArgs ga) {
        if (ga.doc) docInitialize(fld);
        printf("public void initialize%s() {%n", wrkName);
        val init = notNullOf(fld.getInitChar(), defaults.getInit());
        printf("    fill(%s, %d, '%c');%n", pos.apply(fld.getOffset()), fld.getLength(), init);
        printf("}%n");
    }

    public void docInitialize(@NotNull FieldCustom fld) {
        val init = notNullOf(fld.getInitChar(), defaults.getInit());
        printf("/**%n");
        printf(" * Initialize to %s%n", Character.getName(init));
        printf(" * %s @%d+%d%n", tag(), fld.getOffset(), fld.getLength());
        printf(" */%n");
    }

    private void buildSetter(FieldCustom fld, String wrkName, GenerateArgs ga) {
        if (ga.doc) docSetter(fld);
        printf("public void set%s(String s) {%n", wrkName);
        val align = notNullOf(fld.getAlign(), defaults.getAlign());
        val pad = notNullOf(fld.getPadChar(), defaults.getPad());
        val init = notNullOf(fld.getInitChar(), defaults.getInit());
        val ovfl = notNullOf(fld.getOnOverflow(), defaults.getOnOverflow());
        val unfl = notNullOf(fld.getOnUnderflow(), defaults.getOnUnderflow());
        if (ga.setCheck) {
            printf("    s = normalize(s, Action.Overflow.%s, Action.Underflow.%s, '%c', '%c', %s, %d);%n",
                ovfl.of(align), unfl.of(align), pad, init,
                pos.apply(fld.getOffset()), fld.getLength()
            );
            chkSetter(fld);
            printf("    setAsIs(s, %s);%n",
                    pos.apply(fld.getOffset()));
        } else {
            printf("    setAbc(s, %s, %d, Action.Overflow.%s, Action.Underflow.%s, '%c', '%c');%n",
                pos.apply(fld.getOffset()), fld.getLength(),
                ovfl.of(align), unfl.of(align), pad, init);
        }
        printf("}%n");
    }

    private void buildGetter(FieldCustom fld, String wrkName, GenerateArgs ga) {
        if (ga.doc) docGetter(fld);
        printf("public String get%s() {%n", wrkName);
        if (ga.getCheck) chkGetter(fld);
        val pad = notNullOf(fld.getPadChar(), defaults.getPad());
        val norm = notNullOf(fld.getNormalize(), defaults.getNormalize());
        if (norm == NormalizeAbcMode.None) {
            printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        } else {
            printf("    return getAbc(%s, %d, Action.Normalize.%s, '%c');%n",
                pos.apply(fld.getOffset()), fld.getLength(), norm.of(fld.getAlign()), pad);
        }
        printf("}%n");
    }

    private void chkSetter(@NotNull FieldCustom fld) {
        if (fld.getRegex() != null) {
            printf("    testRegex(s, PATTERN_AT%sPLUS%d);%n", pos.apply(fld.getOffset() + 1), fld.getLength());
            return;
        }
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
            case Digit:
                printf("    testDigit(s);%n");
                break;
            case DigitOrBlank:
                printf("    testDigitBlank(s);%n");
                break;
        }
    }

    private void chkGetter(@NotNull FieldCustom fld) {
        if (fld.getRegex() != null) {
            printf("    testRegex(%1$s, %2$d, PATTERN_AT%3$sPLUS%2$d);%n", pos.apply(fld.getOffset()), fld.getLength(), pos.apply(fld.getOffset() + 1));
            return;
        }
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
            case Digit:
                printf("    testDigit(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
            case DigitOrBlank:
                printf("    testDigitBlank(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
                break;
        }
    }

    @Override
    public String tag() {
        return "Cus";
    }
}
