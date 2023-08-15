package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.FilModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = true)
public class FieldFiller extends NakedField implements FilModel {
    private Character fill;

    @Override
    protected FieldFiller shiftCopy(int plus) {
        val res = new FieldFiller();
        res.fill = this.fill;
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }
}
