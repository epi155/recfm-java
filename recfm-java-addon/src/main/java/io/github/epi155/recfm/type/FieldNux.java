package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.InitializeNuxMode;
import io.github.epi155.recfm.api.NuxModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = true)
public class FieldNux extends FieldNum implements NuxModel {
    private InitializeNuxMode initialize;
    @Override
    public String picture() {
        return "Ñ";
    }

    @Override
    protected FieldNux shiftCopy(int plus) {
        val res = new FieldNux();
        res.initialize = this.initialize;
        res.setAccess(this.getAccess());
        res.setWordWidth(this.getWordWidth());
        res.setNormalize(this.getNormalize());
        res.setOnOverflow(getOnOverflow());
        res.setOnUnderflow(getOnUnderflow());
        res.setName(getName());
        res.setOverride(isOverride());
        res.setAudit(isAudit());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }

}
