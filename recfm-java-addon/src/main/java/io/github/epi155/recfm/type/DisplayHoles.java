package io.github.epi155.recfm.type;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisplayHoles {
    private int lo = -1;
    private int hi = -1;

    public void add(int it) {
        if (lo == -1) {
            lo = it;
            hi = it;
        } else if (it == hi + 1) {
            hi = it;
        } else if (it > hi + 1) {
            close();
            lo = it;
            hi = it;
        } else {
            log.error("Hole error: range {}..{}, append {}", lo, hi, it);
            throw new ClassDefineException("Logic error in DisplayHoles");
        }
    }

    public void close() {
        log.error("  [##X..] Offset {}..{} (@{}+{}) unassigned XXX", lo + 1, hi + 1, lo + 1, hi - lo + 1);
    }
}
