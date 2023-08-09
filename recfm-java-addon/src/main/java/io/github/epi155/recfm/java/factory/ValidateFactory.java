package io.github.epi155.recfm.java.factory;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.java.fields.*;
import io.github.epi155.recfm.java.rule.ValidateField;
import io.github.epi155.recfm.type.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.java.JavaTools.prefixOf;

public class ValidateFactory {
    private final ValidateField<FieldAbc> delegateAbc;
    private final ValidateField<FieldNum> delegateNum;
    private final ValidateField<FieldNux> delegateNux;
    private final ValidateField<FieldCustom> delegateCus;
    private final ValidateField<FieldConstant> delegateVal;
    private final ValidateField<FieldDomain> delegateDom;
    private final CodeWriter pw;

    private ValidateFactory(CodeWriter pw, FieldDefault defaults) {
        this.delegateAbc = new Abc(pw, defaults.getAbc());
        this.delegateNum = new Num(pw, defaults.getNum());
        this.delegateNux = new Nux(pw, defaults.getNux());
        this.delegateCus = new Custom(pw, defaults.getCus());
        this.delegateDom = new Domain(pw);
        this.delegateVal = new Constant(pw);
        this.pw = pw;
    }

    public static ValidateFactory getInstance(CodeWriter pw, FieldDefault defaults) {
        return new ValidateFactory(pw, defaults);
    }

    protected void validateGrp(@NotNull GroupAware fld, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        String prefix = prefixOf(isFirst.getAndSet(false));
        pw.printf("%s %s.assessFails(mode, handler);%n", prefix, fld.getName());
    }

    protected void validateOcc(OccursAware fld, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        for (int k = 1; k <= fld.getTimes(); k++) {
            String prefix = prefixOf(isFirst.getAndSet(false));
            pw.printf("%s %s(%d).assessFails(mode, handler);%n", prefix, fld.getName(), k);
        }
    }

    protected void validateVal(FieldConstant fld, int w, IntFunction<String> bias, AtomicBoolean isFirst) {
        delegateVal.validate(fld, w, bias, isFirst);
    }

    protected void validateNum(FieldNum fld, int w, IntFunction<String> bias, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        delegateNum.validate(fld, w, bias, isFirst);
    }
    protected void validateNux(FieldNux fld, int w, IntFunction<String> bias, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        delegateNux.validate(fld, w, bias, isFirst);
    }

    protected void validateAbc(FieldAbc fld, int w, IntFunction<String> bias, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        delegateAbc.validate(fld, w, bias, isFirst);
    }

    protected void validateDom(FieldDomain fld, int w, IntFunction<String> bias, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        delegateDom.validate(fld, w, bias, isFirst);
    }

    protected void validateCus(FieldCustom fld, int w, IntFunction<String> bias, AtomicBoolean isFirst) {
        if (fld.isOverride()) return;
        delegateCus.validate(fld, w, bias, isFirst);
    }

    public void validate(FieldModel fld, int padWidth, IntFunction<String> pos, AtomicBoolean firstStatement) {
        if (fld instanceof FieldAbc) {
            validateAbc((FieldAbc) fld, padWidth, pos, firstStatement);
        } else if (fld instanceof FieldNux) {
            validateNux((FieldNux) fld, padWidth, pos, firstStatement);
        } else if (fld instanceof FieldNum) {
            validateNum((FieldNum) fld, padWidth, pos, firstStatement);
        } else if (fld instanceof FieldCustom) {
            validateCus((FieldCustom) fld, padWidth, pos, firstStatement);
        } else if (fld instanceof FieldDomain) {
            validateDom((FieldDomain) fld, padWidth, pos, firstStatement);
        } else if (fld instanceof FieldConstant) {
            validateVal((FieldConstant) fld, padWidth, pos, firstStatement);
        } else if (fld instanceof OccursAware) {
            validateOcc((OccursAware) fld, firstStatement);
        } else if (fld instanceof GroupAware) {
            validateGrp((GroupAware) fld, firstStatement);
        }
    }

}
