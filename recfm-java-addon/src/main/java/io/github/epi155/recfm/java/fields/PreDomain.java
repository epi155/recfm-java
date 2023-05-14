package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.java.factory.DelegateWriter;
import io.github.epi155.recfm.java.rule.PrepareField;
import io.github.epi155.recfm.type.FieldDomain;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PreDomain extends DelegateWriter implements PrepareField<FieldDomain> {
    public PreDomain(CodeWriter pw) {
        super(pw);
    }

    @Override
    public void prepare(@NotNull FieldDomain fld, int bias) {
        val items = fld.getItems();
        printf("    private static final String VALUE_AT%dPLUS%d = \"%s\";%n",
                fld.getOffset(), fld.getLength(), StringEscapeUtils.escapeJava(items[0]));
        val works = Arrays.asList(items);
        val domain = works.stream()
                .sorted()
                .map(it -> "\"" + StringEscapeUtils.escapeJava(it) + "\"")
                .collect(Collectors.joining(","));
        printf("    private static final String[] DOMAIN_AT%dPLUS%d = { %s };%n", fld.getOffset(), fld.getLength(), domain);
    }

}
