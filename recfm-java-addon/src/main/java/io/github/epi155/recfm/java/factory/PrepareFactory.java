package io.github.epi155.recfm.java.factory;

import io.github.epi155.recfm.java.fields.PreConstant;
import io.github.epi155.recfm.java.fields.PreCustom;
import io.github.epi155.recfm.java.fields.PreDomain;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.PrepareField;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

public class PrepareFactory {
    private final PrepareField<FieldDomain> delegateDom;
    private final PrepareField<FieldConstant> delegateVal;
    private final PrepareField<FieldCustom> delegateCus;

    private PrepareFactory(PrintWriter pw) {
        this.delegateDom = new PreDomain(pw);
        this.delegateVal = new PreConstant(pw);
        this.delegateCus = new PreCustom(pw);
    }

    public static PrepareFactory getInstance(PrintWriter pw) {
        return new PrepareFactory(pw);
    }

    protected void prepareVal(FieldConstant fld, int bias) {
        delegateVal.prepare(fld, bias);
    }

    protected void prepareDom(FieldDomain fld, int bias) {
        delegateDom.prepare(fld, bias);
    }

    protected void prepareCus(FieldCustom fld, int bias) {
        delegateCus.prepare(fld, bias);
    }

    private void prepareGrp(@NotNull FieldGroup fld, int bias) {
        if (fld.isRedefines()) return;
        fld.getFields().forEach(it -> prepare(it, bias));
    }

    private void prepareOcc(@NotNull FieldOccurs fld, int bias) {
        if (fld.isRedefines()) return;
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            int backShift = shift;
            fld.getFields().forEach(it -> prepare(it, bias - backShift));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void prepare(NakedField fld, int bias) {
        if (fld instanceof FieldAbc) {
            // nop
        } else if (fld instanceof FieldNum) {
            // nop
        } else if (fld instanceof FieldCustom) {
            prepareCus((FieldCustom) fld, bias);
        } else if (fld instanceof FieldDomain) {
            prepareDom((FieldDomain) fld, bias);
        } else if (fld instanceof FieldConstant) {
            prepareVal((FieldConstant) fld, bias);
        } else if (fld instanceof FieldFiller) {
            // nop
        } else if (fld instanceof FieldOccurs) {
            prepareOcc((FieldOccurs) fld, bias);
        } else if (fld instanceof FieldGroup) {
            prepareGrp((FieldGroup) fld, bias);
        } else {
            throw new IllegalStateException("Unknown field type " + fld.getClass().getSimpleName());
        }
    }
}
