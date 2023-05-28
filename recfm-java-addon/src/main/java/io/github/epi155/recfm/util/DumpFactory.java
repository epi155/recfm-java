package io.github.epi155.recfm.util;

import io.github.epi155.recfm.type.*;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.epi155.recfm.util.Tools.rpad;

public class DumpFactory {
    private static final String CONSTANT = "<Constant>";

    private DumpFactory() {
    }

    public static List<DumpInfo> getInstance(ParentFields parent) {
        List<Picture> lst = new ArrayList<>();
        dumpFields(lst, "", parent, 0);
        List<DumpInfo> l2 = lst.stream().map(Picture::normalize).collect(Collectors.toList());
        OptionalInt w = l2.stream().mapToInt(it -> it.name.length()).max();
        if (w.isPresent()) {
            val mx = w.getAsInt();
            return l2.stream().map(it -> dotFill(it, mx + 3)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static DumpInfo dotFill(DumpInfo it, int w) {
            return new DumpInfo(rpad(it.name, w, '.'), it.offset, it.length);
    }

    private static void dumpFields(List<Picture> lst, String px, ParentFields parent, int bias) {
        parent.forEachField(field -> {
            if (field instanceof FieldConstant) {
                lst.add(newPicture(CONSTANT, bias + field.getOffset(), field.getLength(), "V"));
            } else if (field instanceof NamedField) {
                NamedField na = (NamedField) field;
                if (na.isRedefines()) return;
                if (na instanceof SettableField) {
                    SettableField fs = (SettableField) na;
                    lst.add(newPicture(px + fs.getName(), bias + fs.getOffset(), fs.getLength(), fs.picture()));
                } else if (na instanceof FieldOccurs) {
                    FieldOccurs fo = (FieldOccurs) na;
                    lst.addAll(occursDump(px + fo.getName(), fo.getTimes(), fo.getLength(), fo, bias));
                } else if (na instanceof FieldGroup) {
                    FieldGroup fg = (FieldGroup) na;
                    lst.addAll(groupDump(px + fg.getName(), fg, bias));
                } else if (na instanceof FieldOccursTrait) {
                    FieldOccursTrait fo = (FieldOccursTrait) na;
                    lst.addAll(occursDump(px + fo.getName(), fo.getTimes(), fo.getLength(), fo, bias));
                } else if (na instanceof FieldGroupTrait) {
                    FieldGroupTrait fg = (FieldGroupTrait) na;
                    lst.addAll(groupDump(px + fg.getName(), fg, bias));
                }
            }
        });
    }
    private static Collection<? extends Picture> occursDump(String prefix, int times, int size, ParentFields parent, int initBias) {
        List<Picture> lst = new ArrayList<>();

        for (int k = 1, bias = initBias; k <= times; k++, bias += size) {
            String px = prefix + "[" + k + "].";
            dumpFields(lst, px, parent, bias);
        }
        return lst;
    }

    private static Collection<? extends Picture> groupDump(String prefix, ParentFields parent, int bias) {
        List<Picture> lst = new ArrayList<>();

        String px = prefix + ".";
        dumpFields(lst, px, parent, bias);
        return lst;
    }

    private static Picture newPicture(String name, int offset, int length, String picture) {
        return new Picture(name, offset, length, picture);
    }

    @AllArgsConstructor
    private static class Picture {
        public final String name;
        public final int offset;
        public final int length;
        public final String pic;


        public DumpInfo normalize() {
            return new DumpInfo(picName(), offset, length);
        }
        private String picName() {
            return name + ": " + pic + "(" + length + ")@" + offset;
        }

    }
}
