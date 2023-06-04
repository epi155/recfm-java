package io.github.epi155.recfm.java.factory;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.java.fields.*;
import io.github.epi155.recfm.java.rule.AccessField;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.Tools;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

/**
 * class that generates the java methods for accessing the fields.
 */
public class AccessFactory {
    private final AccessField<FieldAbc> delegateAbc;
    private final AccessField<FieldNum> delegateNum;
    private final AccessField<FieldNux> delegateNux;
    private final AccessField<FieldCustom> delegateCus;
    private final AccessField<FieldDomain> delegateDom;

    /**
     * Constructor
     *
     * @param pw  print writer
     * @param pos offset field to string form
     */
    private AccessFactory(CodeWriter pw, FieldDefault defaults, IntFunction<String> pos) {
        this.delegateAbc = new Abc(pw, defaults.getAbc(), pos);
        this.delegateNum = new Num(pw, pos, defaults.getNum());
        this.delegateNux = new Nux(pw, pos, defaults.getNux());
        this.delegateCus = new Custom(pw, defaults.getCus(), pos);
        this.delegateDom = new Domain(pw, pos);
    }

    public static AccessFactory getInstance(CodeWriter pw, FieldDefault defaults, IntFunction<String> pos) {
        return new AccessFactory(pw, defaults, pos);
    }

    private void createMethodsDomain(FieldDomain fld, boolean doc) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateDom.access(fld, wrkName, doc);
    }

    private void createMethodsNum(@NotNull FieldNum fld, boolean doc) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateNum.access(fld, wrkName, doc);
    }

    private void createMethodsNumNull(@NotNull FieldNux fld, boolean doc) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateNux.access(fld, wrkName, doc);
    }

    private void createMethodsAbc(@NotNull FieldAbc fld, boolean doc) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateAbc.access(fld, wrkName, doc);
    }

    private void createMethodsCustom(FieldCustom fld, boolean doc) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateCus.access(fld, wrkName, doc);
    }

    /**
     * Method accessor creator
     *
     * @param fld    settable field
     * @param doc     generator arguments
     */
    public void createMethods(SettableField fld, boolean doc) {
        if (fld instanceof FieldAbc) {
            createMethodsAbc((FieldAbc) fld, doc);
        } else if (fld instanceof FieldNux) {
            createMethodsNumNull((FieldNux) fld, doc);
        } else if (fld instanceof FieldNum) {
            createMethodsNum((FieldNum) fld, doc);
        } else if (fld instanceof FieldCustom) {
            createMethodsCustom((FieldCustom) fld, doc);
        } else if (fld instanceof FieldDomain) {
            createMethodsDomain((FieldDomain) fld, doc);
        } else {
            throw new IllegalStateException("Unknown field type " + fld.getName() +": " + fld.getClass().getSimpleName());
        }
    }
}
