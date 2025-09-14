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
        this.delegateGrp = new Group(pw);
        this.delegateOcc = new Occurs(pw);
        this.delegateGTr = new GroupTrait(pw);
        this.delegateOTr = new OccursTrait(pw);
    }

    public static InitializeFactory getInstance(CodeWriter pw, FieldDefault defaults) {
        return new InitializeFactory(pw, defaults);
    }

    protected void initializeOcc(FieldOccurs fld) {
        if (fld.isOverride()) return;
        delegateOcc.initialize(fld);
    }
    protected void initializeOccTrt(FieldOccursTrait fld) {
        if (fld.isOverride()) return;
        delegateOTr.initialize(fld);
    }

    protected void initializeDom(FieldDomain fld) {
        if (fld.isOverride()) return;
        delegateDom.initialize(fld);
    }

    protected void initializeCus(FieldCustom fld) {
        if (fld.isOverride()) return;
        delegateCus.initialize(fld);
    }

    protected void initializeGrp(FieldGroup fld) {
        if (fld.isOverride()) return;
        delegateGrp.initialize(fld);
    }
    protected void initializeGrpTrt(FieldGroupTrait fld) {
        if (fld.isOverride()) return;
        delegateGTr.initialize(fld);
    }

    protected void initializeFil(FieldFiller fld) {
        delegateFil.initialize(fld);
    }

    protected void initializeVal(FieldConstant fld) {
        delegateVal.initialize(fld);
    }

    protected void initializeNum(FieldNum fld) {
        if (fld.isOverride()) return;
        delegateNum.initialize(fld);
    }
    protected void initializeNux(FieldNux fld) {
        if (fld.isOverride()) return;
        delegateNux.initialize(fld);
    }

    protected void initializeAbc(FieldAbc fld) {
        if (fld.isOverride()) return;
        delegateAbc.initialize(fld);
    }

    public void initialize(FieldModel fld) {
        if (fld instanceof FieldAbc) {
            initializeAbc((FieldAbc) fld);
        } else if (fld instanceof FieldNux) {
            initializeNux((FieldNux) fld);
        } else if (fld instanceof FieldNum) {
            initializeNum((FieldNum) fld);
        } else if (fld instanceof FieldCustom) {
            initializeCus((FieldCustom) fld);
        } else if (fld instanceof FieldDomain) {
            initializeDom((FieldDomain) fld);
        } else if (fld instanceof FieldConstant) {
            initializeVal((FieldConstant) fld);
        } else if (fld instanceof FieldFiller) {
            initializeFil((FieldFiller) fld);
        } else if (fld instanceof FieldOccurs) {
            initializeOcc((FieldOccurs) fld);
        } else if (fld instanceof FieldGroup) {
            initializeGrp((FieldGroup) fld);
        } else if (fld instanceof FieldOccursTrait) {
            initializeOccTrt((FieldOccursTrait) fld);
        } else if (fld instanceof FieldGroupTrait) {
            initializeGrpTrt((FieldGroupTrait) fld);
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
            initializeDomItem((FieldDomain) fld);
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

    private void initializeDomItem(FieldDomain fld) {
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
