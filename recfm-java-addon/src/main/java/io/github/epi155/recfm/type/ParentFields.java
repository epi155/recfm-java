package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.FieldModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.event.Level;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface ParentFields {
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ParentFields.class);

    String getName();

    List<FieldModel> getFields();

    int getLength();

    default int evalPadWidth(int min) {
        NuclearInt wid = new NuclearInt(min);
        forEachField(it -> {
            if (it instanceof FloatingField) {
                FloatingField fld = (FloatingField) it;
                wid.maxOf(fld.getName().length());
            } else if (it instanceof ParentFields) {
                wid.maxOf(((ParentFields) it).evalPadWidth(wid.get()));
            }
        });
        return wid.get();
    }

    default void autoOffset(int base) {
        int prevOff = 0;
        int prevLen = 0;
        String prevName = null;
        for (FieldModel fld: getFields()) {
            if (fld.getOffset() == null) {
                if (fld instanceof NamedField && ((NamedField) fld).isOverride()) {
                    if (prevOff>0) {
                        if (fld.getLength()!=prevLen) {
                            if (prevName == null) {
                                log.warn("field {} overrides field @{}+{}, but lengths are different, expected {} provided {}",
                                        ((NamedField) fld).getName(), prevOff, prevLen, prevLen, fld.getLength());
                            } else {
                                log.warn("field {} overrides field {}@{}, but lengths are different, expected {} provided {}",
                                        ((NamedField) fld).getName(), prevName, prevLen, prevLen, fld.getLength());
                            }
                        }
                        fld.setOffset(prevOff);
                    } else {
                        throw new ClassDefineException("Field "+((NamedField) fld).getName() + " isOverride without reference in "+getName());
                    }
                } else {
                    fld.setOffset(base);
                }
            } else {
                base = fld.getOffset();
            }
            if (fld instanceof ParentFields) {
                ParentFields par = (ParentFields) fld;
                par.autoOffset(fld.getOffset());
            }
            if (fld instanceof NamedField) {
                if (! ((NamedField) fld).isOverride()) {
                    prevOff = base;
                    prevLen = fld.getLength();
                    prevName = ((NamedField) fld).getName();
                    base += fld.getLength();
                }
            } else {
                prevOff = base;
                prevLen = fld.getLength();
                prevName = null;
                base += fld.getLength();
            }
        }
    }
    default boolean noHole(int bias) {
        log.info("  [###o..] Checking for hole in group {}: [{}..{}] ...", getName(), bias, bias + getLength() - 1);
        boolean[] b = new boolean[getLength()];
        forEachField(it -> ((NakedField)it).mark(b, bias));
        List<Integer> hole = new ArrayList<>();
        for (int k = 0; k < getLength(); k++) {
            if (!b[k]) hole.add(k);
        }
        if (hole.isEmpty()) {
            return noHoleChilds();
        } else {
            DisplayHoles dis = new DisplayHoles();
            hole.forEach(dis::add);
            dis.close();
            return false;
        }
    }

    default boolean noHoleChilds() {
        AtomicInteger nmFail = new AtomicInteger(0);
        forEachField(it -> {
            if (it instanceof ParentFields) {
                ParentFields par = (ParentFields) it;
                if (!par.noHole()) nmFail.incrementAndGet();
            }
        });
        if (nmFail.get() == 0) {
            log.info("  [####..] No hole detected in {}.", getName());
            return true;
        } else {
            log.info("  [!!!!..] Hole detected in sub-group.");
            return false;
        }
    }
    default void forEachField(Consumer<FieldModel> action) {
        getFields().stream().flatMap(it -> ((NakedField)it).expand()).forEach(action);
    }

    default boolean noOverlap(int bias) {
        log.info("  [####o.] Checking for overlap in group {}: [{}..{}] ...", getName(), bias, bias + getLength() - 1);
        @SuppressWarnings("unchecked") Set<String>[] b = new Set[getLength()];
        forEachField(it -> ((NakedField)it).mark(b, bias));
        List<Pair<Integer, Set<String>>> over = new ArrayList<>();
        for (int k = 0; k < getLength(); k++) {
            if (b[k] != null && b[k].size() > 1) over.add(new ImmutablePair<>(k + bias, b[k]));
        }
        if (over.isEmpty()) {
            return noOverlapChilds();
        } else {
            DisplayOver dis = new DisplayOver();
            over.forEach(dis::add);
            dis.close();
            return false;
        }
    }

    default boolean noOverlapChilds() {
        AtomicInteger nmFail = new AtomicInteger(0);
        forEachField(it -> {
            if (it instanceof ParentFields) {
                ParentFields par = (ParentFields) it;
                if (!par.noOverlap()) nmFail.incrementAndGet();
            }
        });
        if (nmFail.get() == 0) {
            log.info("  [#####.] No overlap detected in {}.", getName());
            return true;
        } else {
            log.info("  [!!!!!.] Overlap detected in sub-group.");
            return false;
        }
    }

    default boolean noDuplicateName(NameCollisionProbe probe) {
        log.info("  [##o...] Checking for duplicate in group {} ...", getName());
        Map<String, NamedField> map = new HashMap<>();
        AtomicInteger countDup = new AtomicInteger();
        forEachField(it -> scanNamedField(it, map, countDup, probe));
        if (countDup.get() == 0) {
            forEachField(it -> scanParentFieldsDup(it, countDup, probe));
            if (countDup.get() == 0) {
                log.info("  [###...] No duplicate fieldName detected in {}.", getName());
                return true;
            } else {
                log.info("  [!!!...] Duplicate fieldName detected in sub-group.");
                return false;
            }
        } else {
            log.info("  [!!!...] {} duplicate fieldName detected.", countDup.get());
            return false;
        }
    }
    default void checkForVoid() {
        long voidFields = getFields().stream().filter(Objects::isNull).count();
        if (voidFields > 0) {
            log.error("{} void field definitions", voidFields);
            throw new ClassDefineException("Class <" + getName() + "> bad defined");
        }
    }

    default boolean noBadName() {
        log.info("  [o.....] Checking for bad name in group {} ...", getName());
        AtomicInteger countBad = new AtomicInteger();
        forEachField(it -> scanBadName(it, countBad));
        if (countBad.get() == 0) {
            forEachField(it -> scanParentFieldsBad(it, countBad));
            if (countBad.get() == 0) {
                log.info("  [#.....] No bad fieldName detected in {}.", getName());
                return true;
            } else {
                log.info("  [!.....] Bad fieldName detected in sub-group.");
                return false;
            }
        } else {
            log.info("  [!.....] {} bad fieldName detected.", countBad.get());
            return false;
        }
    }

    default void scanParentFieldsBad(FieldModel it, AtomicInteger countBad) {
        if (it instanceof ParentFields) {
            ParentFields par = (ParentFields) it;
            if (!par.noBadName()) countBad.getAndIncrement();
        }
    }

    default void scanParentFieldsDup(FieldModel it, AtomicInteger dup, NameCollisionProbe deepCheck) {
        if (it instanceof ParentFields) {
            ParentFields par = (ParentFields) it;
            if (!par.noDuplicateName(deepCheck)) dup.getAndIncrement();
        }
    }

    default void scanNamedField(FieldModel it, Map<String, NamedField> map, AtomicInteger dup, NameCollisionProbe probe) {
        if (it instanceof NamedField) {
            NamedField fld = (NamedField) it;
            NamedField old = map.put(fld.getName(), fld);
            if (old != null) {
                if (fld instanceof FieldGroup) {
                    if (old instanceof FieldGroup) {
                        // GG
                        log.error("  [#X...] GroupName '{}' duplicate @{}+{} and @{}+{} XXX", fld.getName(),
                            old.getOffset(), old.getLength(), it.getOffset(), it.getLength());
                        dup.getAndIncrement();
                    } else {
                        // fld-G x old-F
                        if (probe.collisionLevel(old, (FieldGroup) fld) == Level.ERROR)
                            dup.getAndIncrement();
                    }
                } else {
                    if (old instanceof FieldGroup) {
                        // fld-F x old-G
                        if (probe.collisionLevel(fld, (FieldGroup) old) == Level.ERROR)
                            dup.getAndIncrement();
                    } else {
                        // fld-F x old-F
                        log.error("  [#X...] FieldName '{}' duplicate @{}+{} and @{}+{} XXX", fld.getName(),
                            old.getOffset(), old.getLength(), it.getOffset(), it.getLength());
                        dup.getAndIncrement();
                    }
                }
            }
        }
    }

    default void scanBadName(FieldModel it, AtomicInteger dup) {
        if (it instanceof NamedField) {
            NamedField kt = (NamedField) it;
            if (kt.getName() == null) {
                log.error("  [X....]  null name @{}+{} XXX", it.getOffset(), it.getLength());
                dup.getAndIncrement();
            } else if (!kt.getName().matches("[a-zA-Z_][a-zA-Z_0-9$]*")) {
                log.error("  [X....]  FieldName '{}' not valid @{}+{} XXX", kt.getName(), it.getOffset(), it.getLength());
                dup.getAndIncrement();
            }
        }
    }

    default boolean noOverlap() {
        return noOverlap(1);
    }

    default boolean noHole() {
        return noHole(1);
    }

    default boolean checkLength() {
        log.info("  [#o....] Checking cross-reference in group {} ...", getName());
        List<FieldEmbedGroup> badEmbeds = getFields().stream()
                .filter(FieldEmbedGroup.class::isInstance)
                .map(it -> (FieldEmbedGroup)it)
                .filter(it -> it.getLength() != it.getSource().getLength())
                .collect(Collectors.toList());
        List<FieldGroupTrait> badTypDef = getFields().stream()
                .filter(FieldGroupTrait.class::isInstance)
                .map(it -> (FieldGroupTrait)it)
                .filter(it -> it.getLength() != it.getTypedef().getLength())
                .collect(Collectors.toList());
        if (badEmbeds.isEmpty() && badTypDef.isEmpty()) {
            return checkLengthChild();
        } else {
            if (! badEmbeds.isEmpty()) {
                badEmbeds.forEach(it -> log.error("  [#X....] length error {}@{}, class: {}, interface: {}",
                        it.getSource().getName(), it.getOffset(),
                        it.getLength(), it.getSource().getLength()));
            }
            if (! badTypDef.isEmpty()) {
                badTypDef.forEach(it -> log.error("  [#X....] length error {}@{}, class: {}, interface: {}",
                        it.getName(), it.getOffset(),
                        it.getLength(), it.getTypedef().getLength()));
            }
            return false;
        }
    }

    default boolean checkLengthChild() {
        class CheckStatus {
            private boolean success = true;

            public void and(boolean status) {
                success &= status;
            }
        }
        CheckStatus status = new CheckStatus();
        forEachField(it -> {
            if (it instanceof ParentFields) {
                ParentFields par = (ParentFields) it;
                status.and(par.checkLength());
            }
        });
        if (status.success) {
            log.info("  [##....] No cross-reference error detected in {}.", getName());
            return true;
        } else {
            log.info("  [!!....] Cross-reference error detected in sub-group.");
            return false;
        }
    }
}
