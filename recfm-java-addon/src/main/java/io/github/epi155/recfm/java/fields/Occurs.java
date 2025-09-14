package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.InitializeField;
import io.github.epi155.recfm.type.FieldOccurs;
import org.jetbrains.annotations.NotNull;

public class Occurs extends DelegateWriter implements InitializeField<FieldOccurs> {

    public Occurs(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void initialize(@NotNull FieldOccurs fld) {
        printf("    for(int k=1; k<=%s; k++) %s(k).initialize();%n", fld.getTimes(), fld.getName());
    }

    @Override
    public void initializeItem(@NotNull FieldOccurs fld) {
        initialize(fld);
    }
}
