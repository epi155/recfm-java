package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.type.Defaults;
import io.github.epi155.recfm.type.FieldNum;
import io.github.epi155.recfm.type.NormalizeNumMode;
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

public class Num extends IndentPrinter implements MutableField<FieldNum> {
    private static final String TEST_DIGIT_CHECK = "    testDigit(%s, %d);%n";
    private final Defaults.NumDefault defaults;
    public Num(PrintWriter pw, IntFunction<String> pos, Defaults.NumDefault defaults) {
        super(pw, pos);
        this.defaults = defaults;
    }

    public Num(PrintWriter pw, Defaults.NumDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }


    @Override
    public void initialize(@NotNull FieldNum fld, int bias) {
        printf("        fill(%5d, %4d, '0');%n", fld.getOffset() - bias, fld.getLength());
    }

    @Override
    public void validate(@NotNull FieldNum fld, int w, int bias, AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkDigit(\"%s\"%s, %5d, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), fld.getOffset() - bias, fld.getLength());
    }

    @Override
    public void access(FieldNum fld, String wrkName, int indent, @NotNull GenerateArgs ga) {
        pushIndent(indent);
        numeric(fld, wrkName, ga.doc);
        if (fld.isNumericAccess()) {
            if (fld.getLength() > 19)
                throw new IllegalStateException("Field "+fld.getName()+" too large "+fld.getLength()+"-digits for numeric access");
            else if (fld.getLength() > 9) useLong(fld, wrkName, ga.doc);    // 10..19
            else if (fld.getLength() > 4 || ga.align == 4) useInt(fld, wrkName, ga.doc);     // 5..9
            else if (fld.getLength() > 2 || ga.align == 2) useShort(fld, wrkName, ga.doc);   // 3..4
            else useByte(fld, wrkName, ga.doc);  // ..2
        }
        popIndent();
    }

    private void numeric(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "string");
        printf("public String get%s() {%n", wrkName);
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        val norm = notNullOf(fld.getNormalize(), defaults.getNormalize());
        if (norm == NormalizeNumMode.None) {
            printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        } else {
            printf("    return getAbc(%s, %d, Action.Normalize.LTrim1, '0');%n",
                pos.apply(fld.getOffset()), fld.getLength());
        }
        printf("}%n");
        if (doc) docSetter(fld, "s string");
        printf("public void set%s(String s) {%n", wrkName);
        setNum(fld, true);
    }

    private void setNum(FieldNum fld, boolean doTest) {
        if (doTest) {
            printf("    testDigit(s);%n");
        }
        val align = fld.getAlign();
        val ovfl = notNullOf(fld.getOnOverflow(), defaults.getOnOverflow());
        val unfl = notNullOf(fld.getOnUnderflow(), defaults.getOnUnderflow());
        printf("    setNum(s, %s, %d, Action.Overflow.%s, Action.Underflow.%s);%n",
            pos.apply(fld.getOffset()), fld.getLength(), ovfl.of(align), unfl.of(align));
        printf("}%n");
    }

    private void useByte(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "byte");
        printf("public byte byte%s() {%n", wrkName);
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return Byte.parseByte(getAbc(%s, %d), 10);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (doc) docSetter(fld, "n byte");
        printf("public void set%s(byte n) {%n", wrkName);
        fmtNum(fld);
    }

    private void fmtNum(@NotNull FieldNum fld) {
        printf("    String s = pic9(%d).format(n);%n", fld.getLength());
        setNum(fld, false);
    }

    private void useShort(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "short");
        printf("public short short%s() {%n", wrkName);
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return Short.parseShort(getAbc(%s, %d), 10);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (doc) docSetter(fld, "n short");
        printf("public void set%s(short n) {%n", wrkName);
        fmtNum(fld);
    }

    private void useInt(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "integer");
        printf("public int int%s() {%n", wrkName);
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return Integer.parseInt(getAbc(%s, %d), 10);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (doc) docSetter(fld, "n integer");
        printf("public void set%s(int n) {%n", wrkName);
        fmtNum(fld);
    }

    private void useLong(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "long");
        printf("public long long%s() {%n", wrkName);
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return Long.parseLong(getAbc(%s, %d), 10);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (doc) docSetter(fld, "n long");
        printf("public void set%s(long n) {%n", wrkName);
        fmtNum(fld);
    }

    void docSetter(@NotNull FieldNum fld, String dsResult) {
        printf("/**%n");
        printf(" * Num @%d+%d%n", fld.getOffset(), fld.getLength());
        printf(" * @param %s value%n", dsResult);
        printf(" */%n");
    }

    void docGetter(@NotNull FieldNum fld, String dsType) {
        printf("/**%n");
        printf(" * Num @%d+%d%n", fld.getOffset(), fld.getLength());
        printf(" * @return %s value%n", dsType);
        printf(" */%n");
    }
}
