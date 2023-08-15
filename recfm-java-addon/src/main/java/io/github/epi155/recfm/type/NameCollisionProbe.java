package io.github.epi155.recfm.type;

import org.slf4j.event.Level;

public interface NameCollisionProbe {
    Level collisionLevel(NamedField fld, FieldGroup grp);
}
