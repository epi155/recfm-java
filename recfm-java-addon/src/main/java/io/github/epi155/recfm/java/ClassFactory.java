package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.GenerateArgs;
import io.github.epi155.recfm.api.LoadOverflowAction;
import io.github.epi155.recfm.api.LoadUnderflowAction;
import io.github.epi155.recfm.cobol.CobolEntry;
import io.github.epi155.recfm.cobol.CopyCobolGenerator;
import io.github.epi155.recfm.java.factory.AccessFactory;
import io.github.epi155.recfm.java.factory.InitializeFactory;
import io.github.epi155.recfm.java.factory.PrepareFactory;
import io.github.epi155.recfm.java.factory.ValidateFactory;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.DumpFactory;
import io.github.epi155.recfm.util.DumpInfo;
import io.github.epi155.recfm.util.Tools;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import static io.github.epi155.recfm.util.Tools.notNullOf;

@Slf4j
public class ClassFactory extends CodeHelper {
    public static final int LOW = 1;
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
        printf("import java.util.Collections;%n");
        printf("import java.util.List;%n");
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
        printf("/** record length */%n");
        printf("public static int length() { return LRECL; }%n");
        writeInitializer(clazz);
        writeValidator(clazz, BASE_ONE);
        clazz.forEachField(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, doc);
        });
        writeDump(clazz);
        popIndent();
        writeEndClass();
        copyCobol(clazz);
    }

    private void writeBeginClass(@NotNull ClassDefine struct) {
        embedInterface(struct);
        Collection<String> implementsList = struct.getTraits();
        if (implementsList.isEmpty()) {
            printf("public class %s extends FixRecord {%n", struct.getName());
        } else {
            printf("public class %s extends FixRecord implements %s {%n", struct.getName(), String.join(", ", implementsList));
        }
    }
    private void writeConstant(@NotNull ParentFields struct) {
        if (! struct.isOverride())
            printf("    private static final int LRECL = %d;%n", struct.getLength());
        val preparer = PrepareFactory.getInstance(this);
        struct.forEachField(it -> preparer.prepare(it, 1));

    }
    private void generateGroupCode(FieldGroup fld, IntFunction<String> pos) {
        AccessFactory access;
        if (fld instanceof FieldOccurs) {
            writeBeginClassOccurs((FieldOccurs) fld);
            if (fld.isOverride()) writeConstant(fld);
            pushPlusIndent(4);
            writeInitializerX(fld);
            writeValidator(fld, SHIFT_IT);
            access = AccessFactory.getInstance(this, defaults, SHIFT_IT);
        } else {
            writeBeginClassGroup(fld);
            if (fld.isOverride()) writeConstant(fld);
            pushPlusIndent(4);
            writeInitializer(fld);
            writeValidator(fld, pos);
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        fld.forEachField(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, pos);
            if (it instanceof FieldGroupTrait) generateGroupTraitCode((FieldGroupTrait) it, pos);
        });
        fld.forEachField(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, doc);
        });
        popIndent();
        writeEndClass();
    }
    private void generateGroupCodeTrace(FieldGroup fld, IntFunction<String> pos) {
        AccessFactory access;
        if (fld instanceof FieldOccurs) {
            writeBeginClassOccurs((FieldOccurs) fld);
            if (fld.isOverride()) writeConstant(fld);
            pushPlusIndent(4);
            writeInitializerX(fld);
            writeValidator(fld, SHIFT_IT);
            access = AccessFactory.getInstance(this, defaults, SHIFT_IT);
        } else {
            writeBeginClassGroup(fld);
            if (fld.isOverride()) writeConstant(fld);
            pushPlusIndent(4);
            writeInitializer(fld);
            writeValidator(fld, pos);
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        push(Tools.capitalize(fld.getName()));
        fld.forEachField(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCodeTrace((FieldGroup) it, pos);
            if (it instanceof FieldGroupTrait) generateGroupTraitCode((FieldGroupTrait) it, pos);
        });
        fld.forEachField(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, doc);
        });
        pop();
        popIndent();
        writeEndClass();
    }
    private void generateGroupTraitCode(FieldGroupTrait trait, IntFunction<String> pos) {
        AccessFactory access;
        if (trait instanceof FieldOccursTrait) {
            writeBeginClassOccursTrait((FieldOccursTrait) trait);
            if (trait.isOverride()) writeConstant(trait);
            pushPlusIndent(4);
            writeInitializerX(trait);
            writeValidator(trait, SHIFT_IT);
            access = AccessFactory.getInstance(this, defaults, SHIFT_IT);
        } else {
            writeBeginClassGroupTrait(trait);
            if (trait.isOverride()) writeConstant(trait);
            pushPlusIndent(4);
            writeInitializer(trait);
            writeValidator(trait, pos);
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        push(trait.getTypedef().getName());

        trait.forEachField(fld -> {
            if (fld instanceof SelfCheck) ((SelfCheck) fld).selfCheck();
            if (fld instanceof FieldGroup) generateGroupCodeTrace((FieldGroup) fld, pos);
            if (fld instanceof FieldGroupTrait) generateGroupTraitCode((FieldGroupTrait) fld, pos);
        });
        trait.forEachField(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, doc);
        });
        pop();
        popIndent();
        writeEndClass();
    }

    private void pop() {
        trace.pop();
    }

    private void push(String name) {
        if (trace.isEmpty()) {
            trace.push(name);
        } else {
            if (TraitDefine.contains(name)) {
                trace.push(name);
            } else {
                trace.push(trace.peek() + "." + name);
            }
        }
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

        Collection<String> embs = occurs.getTraits();

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

        Collection<String> embs = occurs.getTraits();
        if (embs.isEmpty()) {
            if (trace.isEmpty()) {
                printf("public class %s implements Validable {%n", capName);
            } else {
                printf("public class %1$s implements Validable, %2$s.%1$s {%n", capName, trace.peek());
            }
        } else {
            String traitList = String.join(", ", embs);
            if (trace.isEmpty()) {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitList);
            } else {
                printf("public class %1$s implements Validable, %2$s.%1$s, %3$s {%n", capName, trace.peek(), traitList);
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

        Collection<String> embs = group.getTraits();
        if (embs.isEmpty()) {
            if (trace.isEmpty()) {
                printf("public class %s implements Validable {%n", capName);
            } else {
                printf("public class %1$s implements Validable, %2$s.%1$s {%n", capName, trace.peek());
            }
        } else {
            String traitList = String.join(", ", embs);
            if (trace.isEmpty()) {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitList);
            } else {
                printf("public class %1$s implements Validable, %2$s.%1$s, %3$s {%n", capName, trace.peek(), traitList);
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

        Collection<String> embs = group.getTraits();
        if (embs.isEmpty()) {
            if (capName.equals(traitName)) {
                printf("public class %s implements Validable, %s.%s {%n", capName, wrtPackage, traitName);
            } else {
                printf(PUBLIC_CLASS_X_IMPLMENTS_Y, capName, traitName);
            }
        } else {
            String traitList = String.join(", ", embs);
            if (capName.equals(traitName)) {
                printf("public class %s implements Validable, %s.%s, %s {%n", capName, wrtPackage, traitName, traitList);
            } else {
                printf("public class %s implements Validable, %s, %s {%n", capName, traitName, traitList);
            }
        }
    }
    private void writeCtorVoid(String name) {
        printf("public %s() {%n", name);
        printf("    super(LRECL);%n");
        printf("    initValues();%n");
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
        printf("    initValues();%n");
        closeBrace();
        printf("private %s(FixRecord r) {%n", struct.getName());
        printf("    super(r, LRECL, %b, %b);%n", isOvfErr, isUnfErr);
        printf("    initValues();%n");
        closeBrace();
        printf("private %s(char[] c) {%n", struct.getName());
        printf("    super(c, LRECL, %b, %b);%n", isOvfErr, isUnfErr);
        printf("    initValues();%n");
        closeBrace();

        printf("/** cast constructor */%n");
        printf("public static %s of(FixRecord r) {%n", struct.getName());
        printf("    return new %s(r);%n", struct.getName());
        closeBrace();
        printf("/** deserialize constructor */%n");
        printf("public static %s decode(String s) {%n", struct.getName());
        printf("    return new %s(s);%n", struct.getName());
        closeBrace();
        printf("/** deep copy constructor */%n");
        printf("public %s copy() {%n", struct.getName());
        printf("    return new %s(Arrays.copyOf(rawData, LRECL));%n", struct.getName());
        closeBrace();
    }
    private void writeInitializer(ParentFields struct) {
        printf(OVERRIDE_METHOD);
        printf("public void initialize() {%n");
        val initializer = InitializeFactory.getInstance(this, defaults);
        struct.forEachField(initializer::initialize);
        closeBrace();
    }
    private void writeInitializerX(ParentFields occ) {
        printf(OVERRIDE_METHOD);
        printf("public void initialize() {%n");
        val initializer = InitializeFactory.getInstance(this, defaults);
        occ.forEachField(initializer::initializeItem);
        closeBrace();
    }
    private void writeValidator(@NotNull ParentFields struct, IntFunction<String> pos) {
        int padWidth = struct.evalPadWidth(6);
        val validator = ValidateFactory.getInstance(this, defaults);
        printf(OVERRIDE_METHOD);
        printf("public boolean validateFails(FieldValidateHandler handler) {%n");
        printf("    return assessFails(ValidateMode.FAIL_FIRST, handler);%n");
        closeBrace();
        printf(OVERRIDE_METHOD);
        printf("public boolean validateAllFails(FieldValidateHandler handler) {%n");
        printf("    return assessFails(ValidateMode.FAIL_ALL, handler);%n");
        closeBrace();
        printf("private boolean assessFails(ValidateMode mode, FieldValidateHandler handler) {%n");
        AtomicBoolean firstCheck = new AtomicBoolean(true);
        struct.forEachField(fld -> validator.validate(fld, padWidth, pos, firstCheck));
        if (firstCheck.get()) {
            printf("    return false;%n");
        } else {
            printf("    return error;%n");
        }
        closeBrace();
    }
    private void copyCobol(ClassDefine clazz) {
        List<CobolEntry> entries = CopyCobolGenerator.create(clazz);
        printf("//* length %,d%n", clazz.getLength());
        for(val entry: entries) {
            printf("// %s%n", entry.code());
        }
    }

    private void writeDump(ClassDefine clazz) {
        List<DumpInfo> l3 = DumpFactory.getInstance(clazz);
        if (l3.isEmpty()) return;

        printf("private static class Helper {%n");
        printf("    private static final DumpInfo[] DIL = new DumpInfo[%d];%n", l3.size());
        int k=0;
        int n=0;
        for(DumpInfo di: l3) {
            if (k % 2000 == 0) {
                if (k>0) {
                    printf("    };%n");
                }
                    printf("    private static void init%d() {%n", n++);
            }
            printf("        DIL[%d] = new DumpInfo(\"%s\", %d, %d);%n", k, di.name, di.offset - LOW, di.length);
            k++;
        }
        printf("    };%n");
        printf("    static {%n");
        for(int j=0; j<n; j++) {
            printf("        init%d();%n", j);
        }
        printf("    }%n");
        printf("}%n");

        printf(OVERRIDE_METHOD);
        printf("public String toString() {%n");
        printf("    StringBuilder sb = new StringBuilder();%n");
        printf("    String eol = System.lineSeparator();%n");
        printf("    for(DumpInfo di: Helper.DIL) {%n");
        printf("        sb.append(di.lab).append(\" : \").append(dump(di.at, di.len)).append(eol);%n");
        printf("    }%n");
        printf("    return sb.toString();%n");
        closeBrace();

    }
}
