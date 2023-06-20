package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.GenerateArgs;
import io.github.epi155.recfm.api.LoadOverflowAction;
import io.github.epi155.recfm.api.LoadUnderflowAction;
import io.github.epi155.recfm.java.factory.AccessFactory;
import io.github.epi155.recfm.java.factory.InitializeFactory;
import io.github.epi155.recfm.java.factory.PrepareFactory;
import io.github.epi155.recfm.java.factory.ValidateFactory;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.DumpFactory;
import io.github.epi155.recfm.util.DumpInfo;
import io.github.epi155.recfm.util.Tools;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static io.github.epi155.recfm.util.Tools.notNullOf;

public class ClassFactory extends CodeHelper {
    private static final IntFunction<String> BASE_ONE = n -> String.format("%d", n - 1);
    private static final IntFunction<String> SHIFT_IT = n -> String.format("%d+shift", n - 1);
    private static final String PUBLIC_CLASS_X_IMPLMENTS_Y = "public class %s implements Validable, %s {%n";
    private final FieldDefault defaults;
    private final Deque<String> trace = new LinkedList<>();
    private boolean doc;

    private ClassFactory(PrintWriter pw, String wrtPackage, GenerateArgs ga, FieldDefault defaults) {
        super(pw, wrtPackage, ga);
        this.defaults = defaults;
    }

    public static ClassFactory newInstance(PrintWriter pw, String wrtPackage, GenerateArgs ga, FieldDefault defaults) {
        return new ClassFactory(pw, wrtPackage, ga, defaults);
    }

    public void writeImport() {
        printf("import java.util.Arrays;%n");
        println();
        printf("import %s.*;%n", SYSTEM_PACKAGE);
        println();
    }

    public void generateClassCode(ClassDefine clazz) {
        this.doc = notNullOf(clazz.getDoc(), defaults.getCls().isDoc());
        if (doc) {
            println("/**");
            tableDoc(clazz);
            println(" */");
        }
        writeBeginClass(clazz);
        writeConstant(clazz);
        println();
        pushIndent(4);
        clazz.forEachField(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, BASE_ONE);
            if (it instanceof FieldGroupTrait) generateGroupTraitCode((FieldGroupTrait) it, BASE_ONE);
        });
        val access = AccessFactory.getInstance(this, defaults, BASE_ONE);
        writeCtorVoid(clazz.getName());
        writeCtorParm(clazz);
        writeInitializer(clazz);
        writeValidator(clazz, BASE_ONE);
        clazz.forEachField(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, doc);
        });
        writeDump(clazz);
        popIndent();
        writeEndClass();
    }

    private void writeBeginClass(@NotNull ClassDefine struct) {
        List<String> traits = struct.getFields()
                .stream()
                .filter(FieldEmbedGroup.class::isInstance)
                .map(it -> ((FieldEmbedGroup) it).getSource().getName())
                .collect(Collectors.toList());
        if (traits.isEmpty()) {
            printf("public class %s extends FixRecord {%n", struct.getName());
        } else {
            printf("public class %s extends FixRecord implements %s {%n", struct.getName(), String.join(", ", traits));
        }
    }
    private void writeConstant(@NotNull ParentFields struct) {
        printf("    public static final int LRECL = %d;%n", struct.getLength());
        val preparer = PrepareFactory.getInstance(this);
        struct.forEachField(it -> preparer.prepare(it, 1));

    }
    private void generateGroupCode(FieldGroup fld, IntFunction<String> pos) {
        AccessFactory access;
        if (fld instanceof FieldOccurs) {
            writeBeginClassOccurs((FieldOccurs) fld);
            pushPlusIndent(4);
            writeValidator(fld, SHIFT_IT);
            access = AccessFactory.getInstance(this, defaults, SHIFT_IT);
        } else {
            writeBeginClassGroup(fld);
            pushPlusIndent(4);
            writeValidator(fld, pos);
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        fld.forEachField(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, pos);
        });
        fld.forEachField(it -> {
            if (it instanceof FloatingField) access.createMethods((FloatingField) it, doc);
        });
        popIndent();
        writeEndClass();
    }
    private void generateGroupTraitCode(FieldGroupTrait trait, IntFunction<String> pos) {
        AccessFactory access;
        if (trait instanceof FieldOccursTrait) {
            writeBeginClassOccursTrait((FieldOccursTrait) trait);
            pushPlusIndent(4);
            writeValidator(trait, SHIFT_IT);
            access = AccessFactory.getInstance(this, defaults, SHIFT_IT);
        } else {
            writeBeginClassGroupTrait(trait);
            pushPlusIndent(4);
            writeValidator(trait, pos);
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        trace.addLast(trait.getTypedef().getName());

        trait.forEachField(fld -> {
            if (fld instanceof SelfCheck) ((SelfCheck) fld).selfCheck();
            if (fld instanceof FieldGroup) generateGroupCode((FieldGroup) fld, pos);
            if (fld instanceof FieldGroupTrait) generateGroupTraitCode((FieldGroupTrait) fld, pos);
        });
        trait.forEachField(it -> {
            if (it instanceof FloatingField) access.createMethods((FloatingField) it, doc);
        });
        trace.removeLast();
        popIndent();
        writeEndClass();
    }
    private void writeBeginClassOccursTrait(FieldOccursTrait occurs) {
        String capName = Tools.capitalize(occurs.getName());
        String traitName = occurs.getTypedef().getName();
        printf("private final %s[] %s = new %1$s[] {%n", capName, occurs.getName());
        for (int k = 0, shift = 0; k < occurs.getTimes(); k++, shift += occurs.getLength()) {
            printf("    this.new %s(%d),%n", capName, shift);
        }
        printf("};%n");
        printf("public %s %s(int k) { return this.%2$s[k-1]; }%n", capName, occurs.getName());
        printf("public void with%1$s(int k, WithAction<%1$s> action) { action.accept(this.%2$s[k-1]); }%n", capName, occurs.getName());
        if (doc)
            javadocGroupDef(occurs);

        List<String> embs = occurs.getTypedef().getFields()
                .stream()
                .filter(FieldEmbedGroup.class::isInstance)
                .map(it -> ((FieldEmbedGroup) it).getSource().getName())
                .map(it -> capName.equals(it) ? wrtPackage+"."+it : it)
                .collect(Collectors.toList());

        if (embs.isEmpty()) {
            if (capName.equals(traitName)) {
                printf("public class %s implements Validable, %s.%s {%n", capName, wrtPackage, traitName);
            } else {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitName);
            }
        } else {
            val traitList = String.join(", ", embs);
            if (capName.equals(traitName)) {
                printf("public class %s implements Validable, %s.%s, %s {%n", capName, wrtPackage, traitName, traitList);
            } else {
                printf("public class %s implements Validable, %s, %s {%n", capName, traitName, traitList);
            }
        }

        printf("    private final int shift;%n");
        printf("    private %s(int shift) { this.shift = shift; }%n", capName);
    }


    private void writeBeginClassOccurs(@NotNull FieldOccurs occurs) {
        String capName = Tools.capitalize(occurs.getName());
        printf("private final %s[] %s = new %1$s[] {%n", capName, occurs.getName());
        for (int k = 0, shift = 0; k < occurs.getTimes(); k++, shift += occurs.getLength()) {
            printf("    this.new %s(%d),%n", capName, shift);
        }
        printf("};%n");
        printf("public %s %s(int k) { return this.%2$s[k-1]; }%n", capName, occurs.getName());
        printf("public void with%1$s(int k, WithAction<%1$s> action) { action.accept(this.%2$s[k-1]); }%n", capName, occurs.getName());
        if (doc)
            javadocGroupDef(occurs);

        List<String> embs = occurs.getFields()
                .stream()
                .filter(FieldEmbedGroup.class::isInstance)
                .map(it -> ((FieldEmbedGroup) it).getSource().getName())
                .map(it -> capName.equals(it) ? wrtPackage+"."+it : it)
                .collect(Collectors.toList());
        if (embs.isEmpty()) {
            if (trace.isEmpty()) {
                printf("public class %s implements Validable {%n", capName);
            } else {
                String trait = String.join(".", trace);
                printf("public class %1$s implements Validable, %2$s.%1$s {%n", capName, trait);
            }
        } else {
            String traitList = String.join(", ", embs);
            if (trace.isEmpty()) {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitList);
            } else {
                String trait = String.join(".", trace);
                printf("public class %1$s implements Validable, %2$s.%1$s, %3$s {%n", capName, trait, traitList);
            }
        }


        printf("    private final int shift;%n");
        printf("    private %s(int shift) { this.shift = shift; }%n", capName);
    }
    private void javadocGroupDef(ParentFields group) {
        println("/**");
        tableDoc(group);
        println(" */");
    }
    private void writeBeginClassGroup(FieldGroup group) {
        val name = group.getName();
        String capName = Tools.capitalize(name);
        printf("private final %s %s = this.new %1$s();%n", capName, name);
        printf("public %s %s() { return this.%2$s; }%n", capName, name);
        printf("public void with%1$s(WithAction<%1$s> action) { action.accept(this.%2$s); }%n", capName, name);

        if (doc) javadocGroupDef(group);

        List<String> embs = group.getFields()
                .stream()
                .filter(FieldEmbedGroup.class::isInstance)
                .map(it -> ((FieldEmbedGroup) it).getSource().getName())
                .map(it -> capName.equals(it) ? wrtPackage+"."+it : it)
                .collect(Collectors.toList());
        if (embs.isEmpty()) {
            if (trace.isEmpty()) {
                printf("public class %s implements Validable {%n", capName);
            } else {
                String trait = String.join(".", trace);
                printf("public class %1$s implements Validable, %2$s.%1$s {%n", capName, trait);
            }
        } else {
            String traitList = String.join(", ", embs);
            if (trace.isEmpty()) {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitList);
            } else {
                String trait = String.join(".", trace);
                printf("public class %1$s implements Validable, %2$s.%1$s, %3$s {%n", capName, trait, traitList);
            }
        }
    }

    private void writeBeginClassGroupTrait(FieldGroupTrait group) {
        val name = group.getName();
        val traitName = group.getTypedef().getName();
        String capName = Tools.capitalize(name);
        printf("private final %s %s = this.new %1$s();%n", capName, name);
        printf("public %s %s() { return this.%2$s; }%n", capName, name);
        printf("public void with%1$s(WithAction<%1$s> action) { action.accept(this.%2$s); }%n", capName, name);

        if (doc) javadocGroupDef(group);

        List<String> embs = group.getTypedef().getFields()
                .stream()
                .filter(FieldEmbedGroup.class::isInstance)
                .map(it -> ((FieldEmbedGroup) it).getSource().getName())
                .map(it -> capName.equals(it) ? wrtPackage+"."+it : it)
                .collect(Collectors.toList());

        if (embs.isEmpty()) {
            if (capName.equals(traitName)) {
                printf("public class %s implements Validable, %s.%s {%n", capName, wrtPackage, traitName);
            } else {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitName);
            }
        } else {
            String traitList = String.join(", ", embs);
            if (capName.equals(traitName)) {
                printf("public class %s implements Validable, %s.%s,  {%n", capName, wrtPackage, traitName, traitList);
            } else {
                printf("public class %s implements Validable, %s, %s {%n", capName, traitName, traitList);
            }
        }
    }
    private void writeCtorVoid(String name) {
        printf("public %s() {%n", name);
        printf("    super(LRECL);%n");
        printf("    initialize();%n");
        closeBrace();
    }
    private void closeBrace() {
        printf("}%n");
    }
    private void writeCtorParm(@NotNull ClassDefine struct) {
        val ovf = notNullOf(struct.getOnOverflow(), defaults.getCls().getOnOverflow());
        val unf = notNullOf(struct.getOnUnderflow(), defaults.getCls().getOnUnderflow());
        val isOvfErr = ovf == LoadOverflowAction.Error;
        val isUnfErr = unf == LoadUnderflowAction.Error;
        printf("private %s(String s) {%n", struct.getName());
        printf("    super(s, LRECL, %b, %b);%n", isOvfErr, isUnfErr);
        closeBrace();
        printf("private %s(FixRecord r) {%n", struct.getName());
        printf("    super(r, LRECL, %b, %b);%n", isOvfErr, isUnfErr);
        closeBrace();
        printf("private %s(char[] c) {%n", struct.getName());
        printf("    super(c, LRECL, %b, %b);%n", isOvfErr, isUnfErr);
        closeBrace();

        printf("public static %s of(FixRecord r) {%n", struct.getName());
        printf("    return new %s(r);%n", struct.getName());
        closeBrace();
        printf("public static %s decode(String s) {%n", struct.getName());
        printf("    return new %s(s);%n", struct.getName());
        closeBrace();
        printf("public %s copy() {%n", struct.getName());
        printf("    return new %s(Arrays.copyOf(rawData, LRECL));%n", struct.getName());
        closeBrace();
    }
    private void writeInitializer(ClassDefine struct) {
        printf("protected void initialize() {%n");
        val initializer = InitializeFactory.getInstance(this, defaults);
        struct.forEachField(it -> initializer.initialize(it, 1));
        closeBrace();
    }
    private void writeValidator(@NotNull ParentFields struct, IntFunction<String> pos) {
        int padWidth = struct.evalPadWidth(6);
        val validator = ValidateFactory.getInstance(this, defaults);
        printf(OVERRIDE_METHOD);
        printf("public boolean validateFails(FieldValidateHandler handler) {%n");
        AtomicBoolean firstCheck = new AtomicBoolean(true);
        struct.forEachField(fld -> validator.validate(fld, padWidth, pos, firstCheck));
        if (firstCheck.get()) {
            printf("    return false;%n");
        } else {
            printf("    return error;%n");
        }
        closeBrace();
    }
    private void writeDump(ParentFields parent) {
        List<DumpInfo> l3 = DumpFactory.getInstance(parent);
        if (! l3.isEmpty()) {
            printf(OVERRIDE_METHOD);
            printf("public String toString() {%n");
            printf("    StringBuilder sb = new StringBuilder();%n");
            printf("    String eol = System.lineSeparator();%n");
            l3.forEach(this::writeFieldDump);
            printf("    return sb.toString();%n");
            closeBrace();
        }
    }
    private void writeFieldDump(DumpInfo it) {
        printf("    sb.append(\"%s : \").append(dump(%d,%d)).append(eol);%n", it.name, it.offset - 1, it.length);
    }
}
