package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.OccTraitModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Set;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class FieldOccursTrait extends FieldGroupTrait implements OccTraitModel {
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
        if (isOverride()) return;
        for (int k = 0, shift = 0; k < times; k++, shift += getLength()) {
            int backShift = shift;
            forEachField(it -> ((NakedField)it).mark(b, bias - backShift));
        }
    }

    @Override
    protected FieldOccursTrait shiftCopy(int plus) {
        val res = new FieldOccursTrait();
        res.times = this.times;
        res.setTypedef(getTypedef());
        res.setName(getName());
        res.setOverride(isOverride());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }

}
