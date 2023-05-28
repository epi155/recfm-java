package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.OccModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class FieldOccurs extends FieldGroup implements OccModel {
    private int times;

    @Override
    public void mark(boolean[] b, int bias) {
        for (int k = 0, shift = 0; k < times; k++, shift += getLength()) {
            int backShift = shift;
            forEachField(it -> ((NakedField)it).mark(b, bias - backShift));
        }
    }

    @Override
    public void mark(@SuppressWarnings("rawtypes") Set[] b, int bias) {
        if (isRedefines()) return;
        for (int k = 0, shift = 0; k < times; k++, shift += getLength()) {
            int backShift = shift;
            forEachField(it -> ((NakedField)it).mark(b, bias - backShift));
        }
    }

    @Override
    protected FieldOccurs shiftCopy(int plus) {
        val res = new FieldOccurs();
        res.times = this.times;
        res.setFields(getFields().stream().map(fld -> ((NakedField)fld).shiftCopy(plus)).collect(Collectors.toList()));
        res.setName(getName());
        res.setRedefines(isRedefines());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }

}
