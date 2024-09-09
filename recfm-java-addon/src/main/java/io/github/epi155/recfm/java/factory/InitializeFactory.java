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
    private final InitializeField<FieldGroup> delegateGrp;
    private final InitializeField<FieldOccurs> delegateOcc;
    private final InitializeField<FieldGroupTrait> delegateGTr;
    private final InitializeField<FieldOccursTrait> delegateOTr;

    private InitializeFactory(CodeWriter pw, FieldDefault defaults) {
        this.delegateAbc = new Abc(pw, defaults.getAbc());
        this.delegateNum = new Num(pw, defaults.getNum());
        this.delegateNux = new Nux(pw, defaults.getNux());
        this.delegateCus = new Custom(pw, defaults.getCus());
        this.delegateDom = new Domain(pw);
        this.delegateFil = new Filler(pw, defaults.getFil());
        this.delegateVal = new Constant(pw);
        this.delegateGrp = new Group(pw, defaults);
        this.delegateOcc = new Occurs(pw, defaults);
        this.delegateGTr = new GroupTrait(pw, defaults);
        this.delegateOTr = new OccursTrait(pw, defaults);
    }

    public static InitializeFactory getInstance(CodeWriter pw, FieldDefault defaults) {
        return new InitializeFactory(pw, defaults);
    }

    protected void initializeOcc(FieldOccurs fld, int bias) {
        if (fld.isOverride()) return;
        delegateOcc.initialize(fld, bias);
//        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
//            int backShift = shift;
//            fld.forEachField(it -> initialize(it, bias - backShift));
//        }
    }
    protected void initializeOccTrt(FieldOccursTrait fld, int bias) {
        if (fld.isOverride()) return;
        delegateOTr.initialize(fld, bias);
//        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
//            int backShift = shift;
//            fld.forEachField(it -> initialize(it, bias - backShift));
//        }
    }

    protected void initializeDom(FieldDomain fld, int bias) {
        if (fld.isOverride()) return;
        delegateDom.initialize(fld, bias);
    }

    protected void initializeCus(FieldCustom fld, int bias) {
        if (fld.isOverride()) return;
        delegateCus.initialize(fld, bias);
    }

    protected void initializeGrp(FieldGroup fld, int bias) {
        if (fld.isOverride()) return;
//        fld.forEachField(it -> initialize(it, bias));
        delegateGrp.initialize(fld, bias);
    }
    protected void initializeGrpTrt(FieldGroupTrait fld, int bias) {
        if (fld.isOverride()) return;
//        fld.forEachField(it -> initialize(it, bias));
        delegateGTr.initialize(fld, bias);
    }

    protected void initializeFil(FieldFiller fld, int bias) {
        delegateFil.initialize(fld, bias);
    }

    protected void initializeVal(FieldConstant fld, int bias) {
        delegateVal.initialize(fld, bias);
    }

    protected void initializeNum(FieldNum fld, int bias) {
        if (fld.isOverride()) return;
        delegateNum.initialize(fld, bias);
    }
    protected void initializeNux(FieldNux fld, int bias) {
        if (fld.isOverride()) return;
        delegateNux.initialize(fld, bias);
    }

    protected void initializeAbc(FieldAbc fld, int bias) {
        if (fld.isOverride()) return;
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

    public void initializeItem(FieldModel fld) {
        if (fld instanceof FieldAbc) {
            initializeAbcItem((FieldAbc) fld);
        } else if (fld instanceof FieldNux) {
            initializeNuxItem((FieldNux) fld);
        } else if (fld instanceof FieldNum) {
            initializeNumItem((FieldNum) fld);
        } else if (fld instanceof FieldCustom) {
            initializeCusItem((FieldCustom) fld);
        } else if (fld instanceof FieldDomain) {
            initializeDom((FieldDomain) fld);
        } else if (fld instanceof FieldConstant) {
            initializeValItem((FieldConstant) fld);
        } else if (fld instanceof FieldFiller) {
            initializeFilItem((FieldFiller) fld);
        } else if (fld instanceof FieldOccurs) {
            initializeOccItem((FieldOccurs) fld);
        } else if (fld instanceof FieldGroup) {
            initializeGrpItem((FieldGroup) fld);
        } else if (fld instanceof FieldOccursTrait) {
            initializeOccTrtItem((FieldOccursTrait) fld);
        } else if (fld instanceof FieldGroupTrait) {
            initializeGrpTrtItem((FieldGroupTrait) fld);
        } else {
            throw new IllegalStateException("Unknown field type " + fld.getClass().getSimpleName());
        }
    }

    private void initializeOccTrtItem(FieldOccursTrait fld) {
        delegateOTr.initializeItem(fld);
    }

    private void initializeGrpTrtItem(FieldGroupTrait fld) {
        delegateGTr.initializeItem(fld);
    }

    private void initializeOccItem(FieldOccurs fld) {
        delegateOcc.initializeItem(fld);
    }

    private void initializeGrpItem(FieldGroup fld) {
        delegateGrp.initializeItem(fld);
    }

    private void initializeFilItem(FieldFiller fld) {
        delegateFil.initializeItem(fld);
    }

    private void initializeValItem(FieldConstant fld) {
        delegateVal.initializeItem(fld);
    }

    private void initializeDom(FieldDomain fld) {
        if (fld.isOverride()) return;
        delegateDom.initializeItem(fld);
    }

    private void initializeCusItem(FieldCustom fld) {
        if (fld.isOverride()) return;
        delegateCus.initializeItem(fld);
    }

    private void initializeNumItem(FieldNum fld) {
        if (fld.isOverride()) return;
        delegateNum.initializeItem(fld);
    }

    private void initializeNuxItem(FieldNux fld) {
        if (fld.isOverride()) return;
        delegateNux.initializeItem(fld);
    }

    private void initializeAbcItem(FieldAbc fld) {
        if (fld.isOverride()) return;
        delegateAbc.initializeItem(fld);
    }
}
