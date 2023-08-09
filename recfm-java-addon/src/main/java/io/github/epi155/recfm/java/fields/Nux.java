package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.api.*;
import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.Action;
import io.github.epi155.recfm.java.rule.MutableField;
import io.github.epi155.recfm.type.FieldNux;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;
import static io.github.epi155.recfm.util.Tools.notNullOf;

public class Nux extends DelegateWriter implements MutableField<FieldNux> {
    private static final String TEST_DIGIT_CHECK = "    testDigitBlank(%s, %d);%n";
    private static final String LET_S_ABC_NULL = "    String s = getAbcNull(%s, %d);%n";

    private final FieldDefault.NuxDefault defaults;

    public Nux(CodeWriter pw, IntFunction<String> pos, FieldDefault.NuxDefault defaults) {
        super(pw, pos);
        this.defaults = defaults;
    }

    public Nux(CodeWriter pw, FieldDefault.NuxDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }


    @Override
    public void initialize(@NotNull FieldNux fld, int bias) {
        val init = notNullOf(fld.getInitialize(), defaults.getInitialize());
        if (init == InitializeNuxMode.Spaces) {
            printf("    fill(%5d, %4d, ' ');%n", fld.getOffset() - bias, fld.getLength());
        } else {    // InitializeNuxMode.Zeroes
            printf("    fill(%5d, %4d, '0');%n", fld.getOffset() - bias, fld.getLength());
        }
    }

    @Override
    public void validate(@NotNull FieldNux fld, int w, @NotNull IntFunction<String> bias, @NotNull AtomicBoolean isFirst) {
        String prefix = prefixOf(isFirst.getAndSet(false));
        printf("%s checkDigitBlank(mode, \"%s\"%s, %-5s, %4d, handler);%n", prefix, fld.getName(), fld.pad(w), bias.apply(fld.getOffset()), fld.getLength());
    }

    @Override
    public void access(FieldNux fld, String wrkName, boolean doc) {
        AccesMode access = notNullOf(fld.getAccess(), defaults.getAccess());
        if (access != AccesMode.Number) {
            numeric(fld, wrkName, doc);
        }
        if (access != AccesMode.String) {
            WordWidth ww = notNullOf(fld.getWordWidth(), defaults.getWordWidth());
            boolean isNumeric = access == AccesMode.Number;
            if (fld.getLength() > 19) useBigInt(fld, wrkName, doc, isNumeric);
            else if (fld.getLength() > 9 || ww ==WordWidth.Long) useLong(fld, wrkName, doc, isNumeric);    // 10..19
            else if (fld.getLength() > 4 || ww ==WordWidth.Int) useInt(fld, wrkName, doc, isNumeric);     // 5..9
            else if (fld.getLength() > 2 || ww ==WordWidth.Short) useShort(fld, wrkName, doc, isNumeric);   // 3..4
            else useByte(fld, wrkName, doc, isNumeric);  // ..2
        }
    }

    private void useBigInt(FieldNux fld, String wrkName, boolean doc, boolean isNumeric) {
        if (doc) docGetter(fld, "BigInteger");
        if (isNumeric) {
            printf("public BigInteger get%s() {%n", wrkName);
        } else {
            printf("public BigInteger bigInteger%s() {%n", wrkName);
        }
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf(LET_S_ABC_NULL, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return s==null ? null : new BigInteger(s);%n");
        printf("}%n");
        if (doc) docSetter(fld, "n BigInteger");
        printf("public void set%s(BigInteger n) {%n", wrkName);
        printf("    String s = n==null ? null : lpad(n.toString(), %d, '0');%n", fld.getLength());
        setNum(fld, false);
    }

    private void numeric(FieldNux fld, String wrkName, boolean doc) {
        if (doc) docGetter(fld, "string");
        printf("public String get%s() {%n", wrkName);
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        val norm = notNullOf(fld.getNormalize(), defaults.getNormalize());
        if (norm == NormalizeNumMode.None) {
            printf("    return getAbcNull(%s, %d);%n", pos.apply(fld.getOffset()), fld.getLength());
        } else {
            printf("    return getAbcNull(%s, %d, %d, '0');%n",
                pos.apply(fld.getOffset()), fld.getLength(), Action.F_NRM_LT1);
        }
        printf("}%n");
        if (doc) docSetter(fld, "s string");
        printf("public void set%s(String s) {%n", wrkName);
        setNum(fld, true);
    }

    private void setNum(FieldNux fld, boolean doTest) {
        if (doTest) {
            printf("    if (s != null) testDigitBlank(s);%n");
        }
        val align = fld.getAlign();
        val ovfl = notNullOf(fld.getOnOverflow(), defaults.getOnOverflow());
        val unfl = notNullOf(fld.getOnUnderflow(), defaults.getOnUnderflow());
        val flag = Action.flagSetter(ovfl, unfl, align);
        printf("    setNumNull(s, %s, %d, %d);%n",
            pos.apply(fld.getOffset()), fld.getLength(), flag);
        printf("}%n");
    }

    private void useByte(FieldNux fld, String wrkName, boolean doc, boolean isNumeric) {
        if (doc) docGetter(fld, "Byte");
        if (isNumeric) {
            printf("public Byte get%s() {%n", wrkName);
        } else {
            printf("public Byte byte%s() {%n", wrkName);
        }
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf(LET_S_ABC_NULL, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return s==null ? null : Byte.parseByte(s);%n");
        printf("}%n");
        if (doc) docSetter(fld, "n byte");
        printf("public void set%s(Byte n) {%n", wrkName);
        fmtNum(fld);
    }

    private void fmtNum(@NotNull FieldNux fld) {
        printf("    String s = n==null ? null : pic9(%d).format(n);%n", fld.getLength());
        setNum(fld, false);
    }

    private void useShort(FieldNux fld, String wrkName, boolean doc, boolean isNumeric) {
        if (doc) docGetter(fld, "Short");
        if (isNumeric) {
            printf("public Short get%s() {%n", wrkName);
        } else {
            printf("public Short short%s() {%n", wrkName);
        }
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf(LET_S_ABC_NULL, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return s==null ? null : Short.parseShort(s);%n");
        printf("}%n");
        if (doc) docSetter(fld, "n Short");
        printf("public void set%s(Short n) {%n", wrkName);
        fmtNum(fld);
    }

    private void useInt(FieldNux fld, String wrkName, boolean doc, boolean isNumeric) {
        if (doc) docGetter(fld, "Integer");
        if (isNumeric) {
            printf("public Integer get%s() {%n", wrkName);
        } else {
            printf("public Integer int%s() {%n", wrkName);
        }
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf(LET_S_ABC_NULL, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return s==null ? null : Integer.parseInt(s);%n");
        printf("}%n");
        if (doc) docSetter(fld, "n Integer");
        printf("public void set%s(Integer n) {%n", wrkName);
        fmtNum(fld);
    }

    private void useLong(FieldNux fld, String wrkName, boolean doc, boolean isNumeric) {
        if (doc) docGetter(fld, "Long");
        if (isNumeric) {
            printf("public Long get%s() {%n", wrkName);
        } else {
            printf("public Long long%s() {%n", wrkName);
        }
        printf(TEST_DIGIT_CHECK, pos.apply(fld.getOffset()), fld.getLength());
        printf(LET_S_ABC_NULL, pos.apply(fld.getOffset()), fld.getLength());
        printf("    return s==null ? null : Long.parseLong(s);%n");
        printf("}%n");
        if (doc) docSetter(fld, "n Long");
        printf("public void set%s(Long n) {%n", wrkName);
        fmtNum(fld);
    }

    void docSetter(@NotNull FieldNux fld, String dsResult) {
        printf("/**%n");
        printf(" * {@code @Nullable} Num @%d+%d%n", fld.getOffset(), fld.getLength());
        printf(" * @param %s value%n", dsResult);
        printf(" */%n");
    }

    void docGetter(@NotNull FieldNux fld, String dsType) {
        printf("/**%n");
        printf(" * {@code @Nullable} Num @%d+%d%n", fld.getOffset(), fld.getLength());
        printf(" * @return %s value%n", dsType);
        printf(" */%n");
    }
}
