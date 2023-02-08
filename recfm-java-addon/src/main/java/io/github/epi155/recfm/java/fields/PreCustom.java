package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.type.FieldCustom;
import io.github.epi155.recfm.util.AbstractPrinter;
import io.github.epi155.recfm.util.PrepareField;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

public class PreCustom extends AbstractPrinter implements PrepareField<FieldCustom> {
    public PreCustom(PrintWriter pw) {
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
