package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.InitializeField;
import io.github.epi155.recfm.type.FieldOccurs;
import org.jetbrains.annotations.NotNull;

public class Occurs extends DelegateWriter implements InitializeField<FieldOccurs> {
    private final FieldDefault defaults;

    public Occurs(CodeWriter pw, FieldDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }

    @Override
    public void initialize(@NotNull FieldOccurs fld, int bias) {
        if (fld.isEmbedded()) return;
        printf("    for(int k=1; k<=%s; k++) %s(k).initialize();%n", fld.getTimes(), fld.getName());
    }

    @Override
    public void initializeItem(@NotNull FieldOccurs fld) {
        if (fld.isEmbedded()) return;
        printf("    for(int k=1; k<=%s; k++) %s(k).initialize();%n", fld.getTimes(), fld.getName());
    }
}
