package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.InitializeField;
import io.github.epi155.recfm.type.FieldGroupTrait;
import org.jetbrains.annotations.NotNull;

public class GroupTrait extends DelegateWriter  implements InitializeField<FieldGroupTrait> {

    public GroupTrait(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void initialize(@NotNull FieldGroupTrait fld) {
        if (! fld.isEmbedded())
            printf("    %s.initialize();%n", fld.getName());
    }

    @Override
    public void initializeItem(@NotNull FieldGroupTrait fld) {
        if (! fld.isEmbedded())
            printf("    %s.initialize();%n", fld.getName());
    }
}
