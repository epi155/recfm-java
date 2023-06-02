package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class FieldNum extends FloatingField implements NumModel {
    private NormalizeNumMode normalize;
    private AccesMode access;
    private WordWidth wordWidth;

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
        res.access = this.access;
        res.wordWidth = this.wordWidth;
        res.normalize = this.normalize;
        res.setOnOverflow(getOnOverflow());
        res.setOnUnderflow(getOnUnderflow());
        res.setName(getName());
        res.setOverride(isOverride());
        res.setLength(getLength());
        res.setOffset(getOffset() + plus);
        return res;
    }
}
