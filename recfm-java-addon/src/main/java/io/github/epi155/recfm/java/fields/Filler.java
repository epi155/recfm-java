package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.ImmutableField;
import io.github.epi155.recfm.type.FieldFiller;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

public class Filler extends DelegateWriter implements ImmutableField<FieldFiller> {
    private final FieldDefault.FilDefault defaults;

    public Filler(CodeWriter pw, FieldDefault.FilDefault defaults) {
        super(pw);
        this.defaults = defaults;
    }

    @Override
    public void initialize(@NotNull FieldFiller fld, int bias) {
        char c = fld.getFill() == null ? defaults.getFill() : fld.getFill();
        printf("    fill(%5d, %4d, '%s');%n",
                fld.getOffset() - bias, fld.getLength(), StringEscapeUtils.escapeJava(String.valueOf(c)));
    }

}
