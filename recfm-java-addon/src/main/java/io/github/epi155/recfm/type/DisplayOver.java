package io.github.epi155.recfm.type;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

@Slf4j
public class DisplayOver {
    private int lo = -1;
    private int hi = -1;
    private Set<String> ov;

    public void add(Pair<Integer, Set<String>> it) {
        int nn = it.getLeft();
        if (lo == -1) {
            start(it);
        } else if (nn == hi + 1 && ov.equals(it.getRight())) {
            hi = nn;
        } else if (nn > hi + 1 || !ov.equals(it.getRight())) {
            close();
            start(it);
        } else {
            log.error("Overfloe error: range {}..{}, append {}", lo, hi, it);
            throw new ClassDefineException("Logic error in DisplayOver");
        }
    }

    private void start(Pair<Integer, Set<String>> it) {
        lo = hi = it.getLeft();
        ov = it.getRight();
    }

    public void close() {
        log.error("   [###X.]  Field overlap {}..{}, {} XXX", lo, hi, ov);
    }
}
