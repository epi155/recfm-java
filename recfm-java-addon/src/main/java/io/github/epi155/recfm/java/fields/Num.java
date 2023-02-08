package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.type.FieldNum;
import io.github.epi155.recfm.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;

@Slf4j
public class Num extends IndentPrinter implements MutableField<FieldNum> {
    public Num(PrintWriter pw, IntFunction<String> pos) {
        super(pw, pos);
    }

    public Num(PrintWriter pw) {
        super(pw);
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
                log.warn("Field {} too large {}-digits for numeric access", fld.getName(), fld.getLength());
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
        printf("    testDigit(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("    return getAbc(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
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
        printf("    setNum(s, %s, %d, OverflowAction.%s, UnderflowAction.%s, '0');%n",
                pos.apply(fld.getOffset()), fld.getLength(), fld.safeOverflow().of(align), fld.safeUnderflow().of(align));
        printf("}%n");
    }

    private void useByte(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "byte");
        printf("public byte byte%s() {%n", wrkName);
        printf("    testDigit(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
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
        printf("    testDigit(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("    return Short.parseShort(getAbc(%s, %d), 10);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (doc) docSetter(fld, "n short");
        printf("public void set%s(short n) {%n", wrkName);
        fmtNum(fld);
    }

    private void useInt(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "integer");
        printf("public int int%s() {%n", wrkName);
        printf("    testDigit(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("    return Integer.parseInt(getAbc(%s, %d), 10);%n", pos.apply(fld.getOffset()), fld.getLength());
        printf("}%n");
        if (doc) docSetter(fld, "n integer");
        printf("public void set%s(int n) {%n", wrkName);
        fmtNum(fld);
    }

    private void useLong(FieldNum fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "long");
        printf("public long long%s() {%n", wrkName);
        printf("    testDigit(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
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
