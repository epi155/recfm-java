package io.github.epi155.recfm.java.factory;

import io.github.epi155.recfm.java.fields.*;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.ValidateField;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class ValidateFactory {
    private final ValidateField<FieldAbc> delegateAbc;
    private final ValidateField<FieldNum> delegateNum;
    private final ValidateField<FieldCustom> delegateCus;
    private final ValidateField<FieldFiller> delegateFil;
    private final ValidateField<FieldConstant> delegateVal;
    private final ValidateField<FieldDomain> delegateDom;

    private ValidateFactory(PrintWriter pw, Defaults defaults) {
        this.delegateAbc = new Abc(pw, defaults.getAbc());
        this.delegateNum = new Num(pw, defaults.getNum());
        this.delegateCus = new Custom(pw, defaults.getCus());
        this.delegateDom = new Domain(pw);
        this.delegateFil = new Filler(pw, defaults.getFil());
        this.delegateVal = new Constant(pw);
    }

    public static ValidateFactory getInstance(PrintWriter pw, Defaults defaults) {
        return new ValidateFactory(pw, defaults);
    }

    protected void validateGrp(@NotNull FieldGroup fld, int w, int bias, AtomicBoolean firstField) {
        if (fld.isRedefines()) return;
        fld.getFields().forEach(it -> validate(it, w, bias, firstField));
    }

    protected void validateOcc(FieldOccurs fld, int w, int bias, AtomicBoolean firstField) {
        if (fld.isRedefines()) return;
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            int backShift = shift;
            fld.getFields().forEach(it -> validate(it, w, bias - backShift, firstField));
        }
    }

    protected void validateFil(FieldFiller fld, int w, int bias, AtomicBoolean isFirst) {
        delegateFil.validate(fld, w, bias, isFirst);
    }

    protected void validateVal(FieldConstant fld, int w, int bias, AtomicBoolean isFirst) {
        delegateVal.validate(fld, w, bias, isFirst);
    }

    protected void validateNum(FieldNum fld, int w, int bias, AtomicBoolean isFirst) {
        if (fld.isRedefines()) return;
        delegateNum.validate(fld, w, bias, isFirst);
    }

    protected void validateAbc(FieldAbc fld, int w, int bias, AtomicBoolean isFirst) {
        if (fld.isRedefines()) return;
        delegateAbc.validate(fld, w, bias, isFirst);
    }

    protected void validateDom(FieldDomain fld, int w, int bias, AtomicBoolean isFirst) {
        if (fld.isRedefines()) return;
        delegateDom.validate(fld, w, bias, isFirst);
    }

    protected void validateCus(FieldCustom fld, int w, int bias, AtomicBoolean isFirst) {
        if (fld.isRedefines()) return;
        delegateCus.validate(fld, w, bias, isFirst);
    }

    public void validate(NakedField fld, int padWidth, int bias, AtomicBoolean firstStatement) {
        if (fld instanceof FieldAbc) {
            validateAbc((FieldAbc) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldNum) {
            validateNum((FieldNum) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldCustom) {
            validateCus((FieldCustom) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldDomain) {
            validateDom((FieldDomain) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldConstant) {
            validateVal((FieldConstant) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldFiller) {
            validateFil((FieldFiller) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldOccurs) {
            validateOcc((FieldOccurs) fld, padWidth, bias, firstStatement);
        } else if (fld instanceof FieldGroup) {
            validateGrp((FieldGroup) fld, padWidth, bias, firstStatement);
        }
    }

}
