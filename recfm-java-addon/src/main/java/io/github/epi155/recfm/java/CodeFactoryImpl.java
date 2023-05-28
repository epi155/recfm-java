package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.*;
import io.github.epi155.recfm.type.*;

public class CodeFactoryImpl implements CodeFactory {
    private CodeFactoryImpl() {}

    @Override
    public ClassModel newClassModel() {
        return new ClassDefine();
    }

    @Override
    public TraitModel newTraitModel() {
        return new TraitDefine();
    }

    @Override
    public AbcModel newAbcModel() {
        return new FieldAbc();
    }

    @Override
    public NumModel newNumModel() {
        return new FieldNum();
    }

    @Override
    public CusModel newCusModel() {
        return new FieldCustom();
    }

    @Override
    public DomModel newDomModel() {
        return new FieldDomain();
    }

    @Override
    public FilModel newFilModel() {
        return new FieldFiller();
    }

    @Override
    public ValModel newValModel() {
        return new FieldConstant();
    }

    @Override
    public GrpModel newGrpModel() {
        return new FieldGroup();
    }

    @Override
    public OccModel newOccModel() {
        return new FieldOccurs();
    }

    @Override
    public EmbModel newEmbModel() {
        return new FieldEmbedGroup();
    }

    @Override
    public GrpTraitModel newGrpTraitModel() {
        return new FieldGroupTrait();
    }

    @Override
    public OccTraitModel newOccTraitModel() {
        return new FieldOccursTrait();
    }

    private static class Helper {
        private static final CodeFactory INSTANCE = new CodeFactoryImpl();
    }
    public static CodeFactory getInstance() {
        return Helper.INSTANCE;
    }

}
