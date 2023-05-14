package io.github.epi155.recfm.java.factory;

import io.github.epi155.recfm.java.fields.Abc;
import io.github.epi155.recfm.java.fields.Custom;
import io.github.epi155.recfm.java.fields.Domain;
import io.github.epi155.recfm.java.fields.Num;
import io.github.epi155.recfm.java.rule.AccessField;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.GenerateArgs;
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
    private final AccessField<FieldCustom> delegateCus;
    private final AccessField<FieldDomain> delegateDom;

    /**
     * Constructor
     *
     * @param pw  print writer
     * @param pos offset field to string form
     */
    private AccessFactory(CodeWriter pw, Defaults defaults, IntFunction<String> pos) {
        this.delegateAbc = new Abc(pw, defaults.getAbc(), pos);
        this.delegateNum = new Num(pw, pos, defaults.getNum());
        this.delegateCus = new Custom(pw, defaults.getCus(), pos);
        this.delegateDom = new Domain(pw, pos);
    }

    public static AccessFactory getInstance(CodeWriter pw, Defaults defaults, IntFunction<String> pos) {
        return new AccessFactory(pw, defaults, pos);
    }

    private void createMethodsDomain(FieldDomain fld, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateDom.access(fld, wrkName, ga);
    }

    private void createMethodsNum(@NotNull FieldNum fld, @NotNull GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateNum.access(fld, wrkName, ga);
    }

    private void createMethodsAbc(@NotNull FieldAbc fld, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateAbc.access(fld, wrkName, ga);
    }

    private void createMethodsCustom(FieldCustom fld, GenerateArgs ga) {
        val wrkName = Tools.getWrkName(fld.getName());
        delegateCus.access(fld, wrkName, ga);
    }

    /**
     * Method accessor creator
     *
     * @param fld    settable field
     * @param ga     generator arguments
     */
    public void createMethods(SettableField fld, GenerateArgs ga) {
        if (fld instanceof FieldAbc) {
            createMethodsAbc((FieldAbc) fld, ga);
        } else if (fld instanceof FieldNum) {
            createMethodsNum((FieldNum) fld, ga);
        } else if (fld instanceof FieldCustom) {
            createMethodsCustom((FieldCustom) fld, ga);
        } else if (fld instanceof FieldDomain) {
            createMethodsDomain((FieldDomain) fld, ga);
        } else {
            throw new IllegalStateException("Unknown field type " + fld.getName() +": " + fld.getClass().getSimpleName());
        }
    }
}
