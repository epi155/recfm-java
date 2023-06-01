package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.api.GrpTraitModel;
import io.github.epi155.recfm.api.TraitModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class FieldGroupTrait extends NamedField implements ParentFields, GrpTraitModel {
    private TraitModel typedef;

    @Override
    public List<FieldModel> getFields() {
        val fields = typedef.getFields();
        int base = fields.get(0).getOffset();
        val plus = getOffset() - base;
        return fields.stream().flatMap(it -> ((NakedField)it).expand())
            .map(fld -> ((NakedField)fld).shiftCopy(plus)).collect(Collectors.toList());
    }

    @Override
    public boolean noHole() {
        return noHole(getOffset());
    }

    @Override
    public boolean noOverlap() {
        return noOverlap(getOffset());
    }

    @Override
    protected FieldGroupTrait shiftCopy(int plus) {
        val res = new FieldGroupTrait();
        res.typedef = this.typedef;
        res.setName(getName());
        res.setOverride(isOverride());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }
}
