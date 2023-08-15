package io.github.epi155.recfm.util;

import io.github.epi155.recfm.type.FieldGroup;
import io.github.epi155.recfm.type.NamedField;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

import java.nio.CharBuffer;

@Slf4j
public class Tools {

    private Tools() {
    }


    public static <T> T notNullOf(T u, T v) {
        return (u!=null) ? u : v;
    }

    public static String getWrkName(String name) {
        val fst = String.valueOf(Character.toUpperCase(name.charAt(0)));
        return (name.length() > 1) ? (fst + name.substring(1)) : fst;
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static @NotNull String rpad(@NotNull String s, int t, char pad) {
        final int len = s.length();
        if (len > t) return s.substring(0, t);
        if (len == t) return s;
        return s + CharBuffer.allocate(t - len).toString().replace('\0', pad);
    }

    public static Level testCollision(NamedField fld, FieldGroup grp) {
        if (fld.getOffset().equals(grp.getOffset()) && fld.getLength()==grp.getLength()) {
            // fld and grp full overlap
            if (grp.isOverride() && !fld.isOverride()) {
                log.info("  [#>...] Name '{}' group override field @{}+{} >>>", fld.getName(),
                    fld.getOffset(), fld.getLength());
                return Level.INFO;
            } else if (fld.isOverride() && !grp.isOverride()) {
                log.info("  [#>...] Name '{}' field override group @{}+{} >>>", fld.getName(),
                    fld.getOffset(), fld.getLength());
                return Level.INFO;
            } else {
                log.warn("  [#X...] Group/Field Name '{}' duplicate @{}+{} XXX", fld.getName(),
                    fld.getOffset(), fld.getLength());
                return Level.ERROR;
            }
        } else {
            log.warn("  [#>...] Name '{}' used by field @{}+{} and group @{}+{} >>>", fld.getName(),
                fld.getOffset(), fld.getLength(), grp.getOffset(), grp.getLength());
            return Level.WARN;
        }
    }
}
