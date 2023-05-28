package io.github.epi155.recfm.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class NamedField extends NakedField implements CheckAware {
    private String name;
    private boolean redefines;
    private boolean audit;

    @Override
    protected void checkBounds(int k, int up, int bias) {
        if (k >= up || k < 0) {
            log.error("*** Field {} @{}+{} out of bounds {}..{}", name, getOffset(), getLength(), bias, up + bias - 1);
            throw new ClassDefineException("Field " + name + " @" + getOffset() + "+" + getLength() + " out of bound: " + bias + ".." + (up + bias - 1));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mark(@SuppressWarnings("rawtypes") Set[] b, int bias) {
        if (redefines) return;
        for (int k = getOffset() - bias, u = 0; u < getLength(); k++, u++) {
            checkBounds(k, b.length, bias);
            if (b[k] == null) {
                b[k] = new HashSet<String>();
            }
            //noinspection unchecked
            b[k].add(name);
        }
    }

}
