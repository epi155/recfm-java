package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.AlignMode;
import io.github.epi155.recfm.api.OverflowAction;
import io.github.epi155.recfm.api.UnderflowAction;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FloatingField extends SettableField implements CheckAware {
    private OverflowAction onOverflow;
    private UnderflowAction onUnderflow;
    private boolean audit;
    public abstract AlignMode getAlign();
}
