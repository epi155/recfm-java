package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.FieldModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Data
@Slf4j
public abstract class NakedField implements FieldModel {
    private Integer offset;
    private int length;

    public void mark(boolean[] b, int bias) {
        for (int k = offset - bias, u = 0; u < length; k++, u++) {
            checkBounds(k, b.length, bias);
            b[k] = true;
        }
    }

    protected void checkBounds(int k, int up, int bias) {
        if (k >= up || k < 0) {
            log.error("*** Field @{}+{} out of bounds {}..{}", offset, length, bias, up + bias - 1);
            throw new ClassDefineException("Field @" + offset + "+" + length + " out of bound: " + bias + ".." + (up + bias - 1));
        }
    }

    @SuppressWarnings("unchecked")
    public void mark(@SuppressWarnings("rawtypes") Set[] b, int bias) {
        for (int k = offset - bias, u = 0; u < length; k++, u++) {
            checkBounds(k, b.length, bias);
            if (b[k] == null) {
                b[k] = new HashSet<String>();
            }
            //noinspection unchecked
            b[k].add(String.format("unnamed<@%d+%d>", offset, length));
        }
    }

    public String pad(int l, int w) {
        if (l < w)
            return CharBuffer.allocate(w - l).toString().replace('\0', ' ');
        else
            return "";
    }

    protected abstract NakedField shiftCopy(int plus);
    protected Stream<FieldModel> expand() {
        return Stream.of(this);
    }

}
