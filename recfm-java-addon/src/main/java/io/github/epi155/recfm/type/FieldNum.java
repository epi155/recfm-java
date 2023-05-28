package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.AlignMode;
import io.github.epi155.recfm.api.NormalizeNumMode;
import io.github.epi155.recfm.api.NumModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class FieldNum extends FloatingField implements NumModel {
    private boolean numericAccess;
    private NormalizeNumMode normalize;

    @Override
    public AlignMode getAlign() {
        return AlignMode.RGT;
    }

    @Override
    public String picture() {
        return "N";
    }

    @Override
    protected FieldNum shiftCopy(int plus) {
        val res = new FieldNum();
        res.numericAccess = this.numericAccess;
        res.normalize = this.normalize;
        res.setOnOverflow(getOnOverflow());
        res.setOnUnderflow(getOnUnderflow());
        res.setName(getName());
        res.setRedefines(isRedefines());
        res.setAudit(isAudit());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }
}
