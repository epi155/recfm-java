package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.EmbModel;
import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.api.TraitModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class FieldEmbedGroup extends NakedField implements EmbModel {
    private TraitModel source;

    @Override
    protected FieldEmbedGroup shiftCopy(int plus) {
        throw new IllegalStateException();
    }
    @Override
    protected Stream<FieldModel> expand() {
        class Shifter {
            private final int targetOffset = getOffset();
            private final int sourceOffset = source.getFields().get(0).getOffset();
            public FieldModel shift(FieldModel fld) {
                fld.setOffset(fld.getOffset()-sourceOffset+targetOffset);
                return fld;
            }
        }
        Shifter shifter = new Shifter();
        return source.getFields().stream().map(shifter::shift);
    }

}
