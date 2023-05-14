package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.PrepareField;
import io.github.epi155.recfm.type.FieldCustom;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

public class PreCustom extends DelegateWriter implements PrepareField<FieldCustom> {
    public PreCustom(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void prepare(@NotNull FieldCustom fld, int bias) {
        val regex = fld.getRegex();
        if (regex != null) {
            printf("    private static final java.util.regex.Pattern PATTERN_AT%dPLUS%d = java.util.regex.Pattern.compile(\"%s\");%n",
                    fld.getOffset(), fld.getLength(), StringEscapeUtils.escapeJava(regex));
        }
    }
}
