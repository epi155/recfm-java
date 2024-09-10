package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.InitializeField;
import io.github.epi155.recfm.type.FieldGroup;
import org.jetbrains.annotations.NotNull;

public class Group extends DelegateWriter implements InitializeField<FieldGroup> {

    public Group(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void initialize(@NotNull FieldGroup fld) {
        if (! fld.isEmbedded())
            printf("    %s.initialize();%n", fld.getName());
    }

    @Override
    public void initializeItem(@NotNull FieldGroup fld) {
        if (! fld.isEmbedded())
            printf("    %s.initialize();%n", fld.getName());
    }
}
