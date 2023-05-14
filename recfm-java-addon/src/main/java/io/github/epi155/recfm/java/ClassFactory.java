package io.github.epi155.recfm.java;

import io.github.epi155.recfm.java.factory.AccessFactory;
import io.github.epi155.recfm.java.factory.InitializeFactory;
import io.github.epi155.recfm.java.factory.PrepareFactory;
import io.github.epi155.recfm.java.factory.ValidateFactory;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.DumpFactory;
import io.github.epi155.recfm.util.DumpInfo;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.Tools;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

public class ClassFactory extends CodeFactory {
    private static final IntFunction<String> BASE_ONE = n -> String.format("%d", n - 1);
    private final Defaults defaults;

    private ClassFactory(PrintWriter pw, String wrtPackage, GenerateArgs ga, Defaults defaults) {
        super(pw, wrtPackage, ga);
        this.defaults = defaults;
    }

    public static ClassFactory newInstance(PrintWriter pw, String wrtPackage, GenerateArgs ga, Defaults defaults) {
        return new ClassFactory(pw, wrtPackage, ga, defaults);
    }

    public void writeImport() {
        printf("import java.util.Arrays;%n");
        println();
        printf("import %s.*;%n", SYSTEM_PACKAGE);
        println();
    }

    public void generateClassCode(ClassDefine clazz) {
        if (ga.doc) {
            println("/**");
            tableDoc(clazz);
            println(" */");
        }
        writeBeginClass(clazz);
        writeConstant(clazz);
        println();
        pushIndent(4);
        clazz.getFields().forEach(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, BASE_ONE);
            if (it instanceof FieldGroupProxy) generateGroupCode((FieldGroupProxy) it, BASE_ONE);
        });
        val access = AccessFactory.getInstance(this, defaults, BASE_ONE);
        writeCtorVoid(clazz.getName());
        writeCtorParm(clazz);
        writeInitializer(clazz);
        writeValidator(clazz);
        clazz.getFields().forEach(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, ga);
        });
        writeDump(clazz.getFields());
        popIndent();
        writeEndClass();
    }

    private void writeBeginClass(@NotNull ClassDefine struct) {
        printf("public class %s extends FixRecord {%n", struct.getName());
    }
    private void writeConstant(@NotNull ParentFields struct) {
        printf("    public static final int LRECL = %d;%n", struct.getLength());
        val preparer = PrepareFactory.getInstance(this);
        struct.getFields().forEach(it -> preparer.prepare(it, 1));

    }
    private void generateGroupCode(FieldGroup fld, IntFunction<String> pos) {
        AccessFactory access;
        if (fld instanceof FieldOccurs) {
            writeBeginClassOccurs((FieldOccurs) fld);
            access = AccessFactory.getInstance(this, defaults, n -> String.format("%d+shift", n - 1));
        } else {
            writeBeginClassGroup(fld);
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        pushPlusIndent(4);
        fld.getFields().forEach(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, pos);
        });
        fld.getFields().forEach(it -> {
            if (it instanceof FloatingField) access.createMethods((FloatingField) it, ga);
        });
        popIndent();
        writeEndClass();
    }
    private void generateGroupCode(FieldGroupProxy pxy, IntFunction<String> pos) {
        AccessFactory access;
        if (pxy instanceof FieldOccursProxy) {
//            writeBeginClassOccurs(pw, (FieldOccurs) pxy, indent);
            access = AccessFactory.getInstance(this, defaults, n -> String.format("%d+shift", n - 1));
        } else {
            writeBeginClassGroupProxy(pxy, pxy.getProxy().getName());
            access = AccessFactory.getInstance(this, defaults, pos);
        }
        pushPlusIndent(4);
        pxy.getFields().forEach(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, pos);
            if (it instanceof FieldGroupProxy) generateGroupCode((FieldGroupProxy) it, pos);
        });
        pxy.getFields().forEach(it -> {
            if (it instanceof FloatingField) access.createMethods((FloatingField) it, ga);
        });
        popIndent();
        writeEndClass();
    }

    private void writeBeginClassOccurs(@NotNull FieldOccurs fld) {
        String capName = Tools.capitalize(fld.getName());
        printf("private final %s[] %s = new %1$s[] {%n", capName, fld.getName());
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            printf("    this.new %s(%d),%n", capName, shift);
        }
        printf("};%n");
        printf("public %s %s(int k) { return this.%2$s[k-1]; }%n", capName, fld.getName());
        printf("public class %s {%n", capName);

        printf("private final int shift;%n");
        printf("private %s(int shift) { this.shift = shift; }%n", capName);
    }
    private void writeBeginClassGroup(FieldGroup group) {
        val name = group.getName();
        String capName = Tools.capitalize(name);
        printf("private final %s %s = this.new %1$s();%n", capName, name);
        printf("public %s %s() { return this.%2$s; }%n", capName, name);
        printf("public void with%1$s(WithAction<%1$s> action) { action.call(this.%2$s); }%n", capName, name);

        if (ga.doc)
            javadocGroupDef(group);
        printf("public class %s {%n", capName);
    }

    private void javadocGroupDef(ParentFields group) {
        println("/**");
        tableDoc(group);
        println(" */");
    }
    private void writeBeginClassGroupProxy(FieldGroupProxy group, String proxyName) {
        val name = group.getName();
        String capName = Tools.capitalize(name);
        printf("private final %s %s = this.new %1$s();%n", capName, name);
        printf("public %s %s() { return this.%2$s; }%n", capName, name);
        printf("public void with%1$s(WithAction<%1$s> action) { action.call(this.%2$s); }%n", capName, name);

        if (ga.doc)
            javadocGroupDef(group);
        printf("public class %s implements %s.%s {%n", capName, wrtPackage, proxyName);
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
        struct.onOverflowDefault(LoadOverflowAction.Trunc);
        struct.onUnderflowDefault(LoadUnderflowAction.Pad);
        printf("private %s(String s) {%n", struct.getName());
        printf("    super(s, LRECL, %b, %b);%n",
                struct.onOverflowThrowError(), struct.onUnderflowThrowError());
        closeBrace();
        printf("private %s(FixRecord r) {%n", struct.getName());
        printf("    super(r, LRECL, %b, %b);%n",
                struct.onOverflowThrowError(), struct.onUnderflowThrowError());
        closeBrace();
        printf("private %s(char[] c) {%n", struct.getName());
        printf("    super(c, LRECL, %b, %b);%n",
                struct.onOverflowThrowError(), struct.onUnderflowThrowError());
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
        struct.getFields().forEach(it -> initializer.initialize(it, 1));
        closeBrace();
    }
    private void writeValidator(@NotNull ClassDefine struct) {
        int padWidth = struct.evalPadWidth(6);
        val validator = ValidateFactory.getInstance(this, defaults);
        printf(OVERRIDE_METHOD);
        printf("public boolean validateFails(FieldValidateHandler handler) {%n");
        AtomicBoolean firstCheck = new AtomicBoolean(true);
        for (NakedField fld : struct.getFields()) {
            validator.validate(fld, padWidth, 1, firstCheck);
        }
        if (firstCheck.get()) {
            printf("    return false;%n");
        } else {
            printf("    return error;%n");
        }
        closeBrace();

        printf(OVERRIDE_METHOD);
        printf("public boolean auditFails(FieldValidateHandler handler) {%n");
        AtomicBoolean firstAudit = new AtomicBoolean(true);
        for (NakedField fld : struct.getFields()) {
            if (fld instanceof CheckAware && ((CheckAware) fld).isAudit()) {
                validator.validate(fld, padWidth, 1, firstAudit);
            }
        }
        if (firstAudit.get()) {
            printf("    return false;%n");
        } else {
            printf("    return error;%n");
        }
        closeBrace();
    }
    private void writeDump(List<NakedField> fields) {
        List<DumpInfo> l3 = DumpFactory.getInstance(fields);
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
