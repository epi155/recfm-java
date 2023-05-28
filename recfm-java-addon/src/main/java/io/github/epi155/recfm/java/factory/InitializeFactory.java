package io.github.epi155.recfm.java.factory;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.java.fields.*;
import io.github.epi155.recfm.java.rule.InitializeField;
import io.github.epi155.recfm.type.*;

public class InitializeFactory {
    private final InitializeField<FieldAbc> delegateAbc;
    private final InitializeField<FieldNum> delegateNum;
    private final InitializeField<FieldNux> delegateNux;
    private final InitializeField<FieldCustom> delegateCus;
    private final InitializeField<FieldDomain> delegateDom;
    private final InitializeField<FieldFiller> delegateFil;
    private final InitializeField<FieldConstant> delegateVal;

    private InitializeFactory(CodeWriter pw, FieldDefault defaults) {
        this.delegateAbc = new Abc(pw, defaults.getAbc());
        this.delegateNum = new Num(pw, defaults.getNum());
        this.delegateNux = new Nux(pw, defaults.getNux());
        this.delegateCus = new Custom(pw, defaults.getCus());
        this.delegateDom = new Domain(pw);
        this.delegateFil = new Filler(pw, defaults.getFil());
        this.delegateVal = new Constant(pw);
    }

    public static InitializeFactory getInstance(CodeWriter pw, FieldDefault defaults) {
        return new InitializeFactory(pw, defaults);
    }

    protected void initializeOcc(FieldOccurs fld, int bias) {
        if (fld.isRedefines()) return;
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            int backShift = shift;
            fld.forEachField(it -> initialize(it, bias - backShift));
        }
    }
    protected void initializeOccTrt(FieldOccursTrait fld, int bias) {
        if (fld.isRedefines()) return;
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            int backShift = shift;
            fld.forEachField(it -> initialize(it, bias - backShift));
        }
    }

    protected void initializeDom(FieldDomain fld, int bias) {
        if (fld.isRedefines()) return;
        delegateDom.initialize(fld, bias);
    }

    protected void initializeCus(FieldCustom fld, int bias) {
        if (fld.isRedefines()) return;
        delegateCus.initialize(fld, bias);
    }

    protected void initializeGrp(FieldGroup fld, int bias) {
        if (fld.isRedefines()) return;
        fld.forEachField(it -> initialize(it, bias));
    }
    protected void initializeGrpTrt(FieldGroupTrait fld, int bias) {
        if (fld.isRedefines()) return;
        fld.forEachField(it -> initialize(it, bias));
    }

    protected void initializeFil(FieldFiller fld, int bias) {
        delegateFil.initialize(fld, bias);
    }

    protected void initializeVal(FieldConstant fld, int bias) {
        delegateVal.initialize(fld, bias);
    }

    protected void initializeNum(FieldNum fld, int bias) {
        if (fld.isRedefines()) return;
        delegateNum.initialize(fld, bias);
    }
    protected void initializeNux(FieldNux fld, int bias) {
        if (fld.isRedefines()) return;
        delegateNux.initialize(fld, bias);
    }

    protected void initializeAbc(FieldAbc fld, int bias) {
        if (fld.isRedefines()) return;
        delegateAbc.initialize(fld, bias);
    }

    public void initialize(FieldModel fld, int bias) {
        if (fld instanceof FieldAbc) {
            initializeAbc((FieldAbc) fld, bias);
        } else if (fld instanceof FieldNux) {
            initializeNux((FieldNux) fld, bias);
        } else if (fld instanceof FieldNum) {
            initializeNum((FieldNum) fld, bias);
        } else if (fld instanceof FieldCustom) {
            initializeCus((FieldCustom) fld, bias);
        } else if (fld instanceof FieldDomain) {
            initializeDom((FieldDomain) fld, bias);
        } else if (fld instanceof FieldConstant) {
            initializeVal((FieldConstant) fld, bias);
        } else if (fld instanceof FieldFiller) {
            initializeFil((FieldFiller) fld, bias);
        } else if (fld instanceof FieldOccurs) {
            initializeOcc((FieldOccurs) fld, bias);
        } else if (fld instanceof FieldGroup) {
            initializeGrp((FieldGroup) fld, bias);
        } else if (fld instanceof FieldOccursTrait) {
            initializeOccTrt((FieldOccursTrait) fld, bias);
        } else if (fld instanceof FieldGroupTrait) {
            initializeGrpTrt((FieldGroupTrait) fld, bias);
        } else {
            throw new IllegalStateException("Unknown field type " + fld.getClass().getSimpleName());
        }
    }

}
