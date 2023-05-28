package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.AlignMode;
import io.github.epi155.recfm.api.CheckUser;
import io.github.epi155.recfm.api.CusModel;
import io.github.epi155.recfm.api.NormalizeAbcMode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class FieldCustom extends FloatingField implements SelfCheck, CusModel {
    private Character padChar;
    private Character initChar;
    private CheckUser check;
    private AlignMode align;
    private NormalizeAbcMode normalize;
    private String regex;
    private Boolean checkSetter;
    private Boolean checkGetter;

    @Override
    public String picture() {
        return "C";
    }

    @Override
    public void selfCheck() {
        if (regex != null) {
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                throw new ClassDefineException(fieldDescriptor() + " invalid regex: " + e.getMessage());
            }
        }
    }

    private String fieldDescriptor() {
        return "Field " + getName() + "@" + getOffset() + "+" + getLength();
    }

    @Override
    protected FieldCustom shiftCopy(int plus) {
        val res = new FieldCustom();
        res.padChar = this.padChar;
        res.initChar = this.initChar;
        res.check = this.check;
        res.align = this.align;
        res.normalize = this.normalize;
        res.regex = this.regex;
        res.checkGetter = this.checkGetter;
        res.checkSetter = this.checkSetter;
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
