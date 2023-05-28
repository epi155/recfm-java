package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.api.GrpModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class FieldGroup extends NamedField implements ParentFields, GrpModel {
    private List<FieldModel> fields = new ArrayList<>();

    @Override
    public boolean noHole() {
        return noHole(getOffset());
    }

    @Override
    public boolean noOverlap() {
        return noOverlap(getOffset());
    }

    @Override
    protected FieldGroup shiftCopy(int plus) {
        val res = new FieldGroup();
        res.fields = this.fields.stream().map(fld -> ((NakedField)fld).shiftCopy(plus)).collect(Collectors.toList());
        res.setName(getName());
        res.setRedefines(isRedefines());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }
}
