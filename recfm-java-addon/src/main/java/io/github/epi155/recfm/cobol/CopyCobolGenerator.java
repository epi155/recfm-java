package io.github.epi155.recfm.cobol;

import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.type.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CopyCobolGenerator {
    private CopyCobolGenerator() {}

    private static final int LEVEL_STEP = 5;
    public static List<CobolEntry> create(ClassDefine clazz) {
        List<CobolEntry> entries = new ArrayList<>();
        int level = 1;
        entries.add(new CobolGroup(level, clazz.getName()));
        level += LEVEL_STEP;
        addFields(entries, level, clazz.getFields());
        CobolEntry.beautify(entries);
        return entries;
    }

    private static void addFields(List<CobolEntry> entries, int level, List<FieldModel> fields) {
        for(FieldModel fld: fields) {
            if (fld instanceof NamedField && ((NamedField) fld).isOverride()) {
                continue;
            }

            if (fld instanceof FieldFiller) {
                entries.add(new CobolFiller(level, fld.getOffset(), fld.getLength()));
            } else if (fld instanceof FieldAbc) {
                entries.add(new CobolPicX(level, ((FieldAbc) fld).getName(), fld.getOffset(), fld.getLength()));
            } else if (fld instanceof FieldCustom) {
                entries.add(new CobolPicX(level, ((FieldCustom) fld).getName(), fld.getOffset(), fld.getLength()));
            } else if (fld instanceof FieldNum) {
                entries.add(new CobolPic9(level, ((FieldNum) fld).getName(), fld.getOffset(), fld.getLength()));
            } else if (fld instanceof FieldDomain) {
                entries.add(new CobolDomain(level, ((FieldDomain) fld).getName(), fld.getOffset(), fld.getLength(), ((FieldDomain) fld).getItems()));
            } else if (fld instanceof FieldConstant) {
                entries.add(new CobolConst(level, fld.getOffset(), fld.getLength(), ((FieldConstant) fld).getValue()));
            } else if (fld instanceof FieldOccurs) {
                entries.add(new CobolOccurs(level, ((FieldOccurs) fld).getName(), ((FieldOccurs) fld).getTimes()));
                level += LEVEL_STEP;
                addFields(entries, level, ((FieldOccurs) fld).getFields());
                level -= LEVEL_STEP;
            } else if (fld instanceof FieldGroup) {
                entries.add(new CobolGroup(level, ((FieldGroup) fld).getName()));
                level += LEVEL_STEP;
                addFields(entries, level, ((FieldGroup) fld).getFields());
                level -= LEVEL_STEP;
            } else if (fld instanceof FieldEmbedGroup) {
                addFields(entries, level, ((FieldEmbedGroup) fld).getSource().getFields());
            } else if (fld instanceof FieldOccursTrait) {
                entries.add(new CobolOccurs(level, ((FieldOccursTrait) fld).getName(), ((FieldOccursTrait) fld).getTimes()));
                level += LEVEL_STEP;
                addFields(entries, level, ((FieldOccursTrait) fld).getFields());
                level -= LEVEL_STEP;
            } else if (fld instanceof FieldGroupTrait) {
                entries.add(new CobolGroup(level, ((FieldGroupTrait) fld).getName()));
                level += LEVEL_STEP;
                addFields(entries, level, ((FieldGroupTrait) fld).getFields());
                level -= LEVEL_STEP;
            } else {
                log.warn("@{} Type unknown {}, skipped ", fld.getOffset(), fld.getClass().getName());
            }
        }
    }
}
