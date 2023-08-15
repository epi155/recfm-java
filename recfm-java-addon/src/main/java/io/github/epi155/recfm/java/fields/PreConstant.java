package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.PrepareField;
import io.github.epi155.recfm.type.FieldConstant;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

public class PreConstant extends DelegateWriter implements PrepareField<FieldConstant> {
    public PreConstant(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void prepare(@NotNull FieldConstant fld, int bias) {
        printf("    private static final String VALUE_AT%dPLUS%d = \"%s\";%n",
                fld.getOffset(), fld.getLength(), StringEscapeUtils.escapeJava(fld.getValue()));
    }
}
