package io.github.epi155.recfm.type;

import java.util.concurrent.atomic.AtomicInteger;

public class NuclearInt extends AtomicInteger {
    public NuclearInt(int n) {
        super(n);
    }

    public void maxOf(int l) {
        if (l > get())
            set(l);
    }
}
