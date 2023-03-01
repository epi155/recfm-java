package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.CodeProvider;
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

import java.io.File;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

public class CodeGenerator implements IndentAble, CodeProvider {
    private static final String SYSTEM_PACKAGE = "io.github.epi155.recfm.java";
    private static final String OVERRIDE_METHOD = "    @Override%n";
    static void writeCopyright(@NotNull PrintWriter pw, @NotNull GenerateArgs ga) {
        String now = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        pw.println("/*");
        pw.printf(" * Generated by %s:%s:%s at %s%n", ga.group, ga.artifact, ga.version, now);
        pw.println(" */");
    }

    @Override
    public File fileOf(String cwd, String name) {
        return new File(cwd + File.separator + name + ".java");
    }

    @Override
    public void createClass(PrintWriter pw, String wrtPackage, ClassDefine struct, GenerateArgs ga, Defaults defaults) {
        writePackage(pw, wrtPackage, ga);
        writeImport(pw);
        generateClassCode(pw, struct, ga, defaults, n -> String.format("%d", n - 1));
    }

    private void generateClassCode(PrintWriter pw, ClassDefine struct, GenerateArgs ga, Defaults defaults, IntFunction<String> pos) {
        writeBeginClass(pw, struct);
        writeConstant(pw, struct);
        pw.println();
        struct.getFields().forEach(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, pw, 4, ga, defaults, pos);
        });
        val access = AccessFactory.getInstance(pw, defaults, pos);
        writeCtorVoid(pw, struct.getName());
        writeCtorParm(pw, struct);
        writeInitializer(pw, struct, defaults);
        writeValidator(pw, struct, defaults);
        struct.getFields().forEach(it -> {
            if (it instanceof SettableField) access.createMethods((SettableField) it, 4, ga);
        });
        writeDump(pw, struct.getFields());
        writeEndClass(pw, 0);
    }

    private void writeDump(PrintWriter pw, List<NakedField> fields) {
        List<DumpInfo> l3 = DumpFactory.getInstance(fields);
        if (! l3.isEmpty()) {
            pw.printf(OVERRIDE_METHOD);
            pw.printf("    public String toString() {%n");
            pw.printf("        StringBuilder sb = new StringBuilder();%n");
            pw.printf("        String eol = System.lineSeparator();%n");
            l3.forEach(it -> writeFieldDump(pw, it));
            pw.printf("        return sb.toString();%n");
            closeBrace(pw);
        }
    }

    private void writeFieldDump(PrintWriter pw, DumpInfo it) {
        pw.printf("        sb.append(\"%s : \").append(dump(%d,%d)).append(eol);%n", it.name, it.offset - 1, it.length);
    }

    private void generateGroupCode(FieldGroup fld, PrintWriter pw, int indent, GenerateArgs ga, Defaults defaults, IntFunction<String> pos) {
        AccessFactory access;
        if (fld instanceof FieldOccurs) {
            writeBeginClassOccurs(pw, (FieldOccurs) fld, indent);
            access = AccessFactory.getInstance(pw, defaults, n -> String.format("%d+shift", n - 1));
        } else {
            writeBeginClassGroup(pw, fld.getName(), indent);
            access = AccessFactory.getInstance(pw, defaults, pos);
        }
        fld.getFields().forEach(it -> {
            if (it instanceof SelfCheck) ((SelfCheck) it).selfCheck();
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, pw, indent + 4, ga, defaults, pos);
        });
        fld.getFields().forEach(it -> {
            if (it instanceof FloatingField) access.createMethods((FloatingField) it, indent + 4, ga);
        });
        writeEndClass(pw, indent);
    }

    private void writeBeginClassOccurs(PrintWriter pw, @NotNull FieldOccurs fld, int indent) {
        String capName = Tools.capitalize(fld.getName());
        indent(pw, indent);
        pw.printf("private final %s[] %s = new %1$s[] {%n", capName, fld.getName());
        for (int k = 0, shift = 0; k < fld.getTimes(); k++, shift += fld.getLength()) {
            indent(pw, indent);
            pw.printf("    this.new %s(%d),%n", capName, shift);
        }
        indent(pw, indent);
        pw.printf("};%n");
        indent(pw, indent);
        pw.printf("public %s %s(int k) { return this.%2$s[k-1]; }%n", capName, fld.getName());
        indent(pw, indent);
        pw.printf("public class %s {%n", capName);

        indent(pw, indent);
        pw.printf("    private final int shift;%n");
        indent(pw, indent);
        pw.printf("    private %s(int shift) { this.shift = shift; }%n", capName);
    }

    private void writeBeginClassGroup(PrintWriter pw, String name, int indent) {
        String capName = Tools.capitalize(name);
        indent(pw, indent);
        pw.printf("private final %s %s = this.new %1$s();%n", capName, name);
        indent(pw, indent);
        pw.printf("public %s %s() { return this.%2$s; }%n", capName, name);
        indent(pw, indent);
        pw.printf("public class %s {%n", capName);
    }

    private void writeEndClass(PrintWriter pw, int indent) {
        indent(pw, indent);
        pw.write("}");
        pw.println();
    }

    private void writeValidator(PrintWriter pw, @NotNull ClassDefine struct, Defaults defaults) {
        int padWidth = struct.evalPadWidth(6);
        val validator = ValidateFactory.getInstance(pw, defaults);
        pw.printf(OVERRIDE_METHOD);
        pw.printf("    public boolean validateFails(FieldValidateHandler handler) {%n");
        AtomicBoolean firstCheck = new AtomicBoolean(true);
        for (NakedField fld : struct.getFields()) {
            validator.validate(fld, padWidth, 1, firstCheck);
        }
        if (firstCheck.get()) {
            pw.printf("        return false;%n");
        } else {
            pw.printf("        return error;%n");
        }
        closeBrace(pw);

        pw.printf(OVERRIDE_METHOD);
        pw.printf("    public boolean auditFails(FieldValidateHandler handler) {%n");
        AtomicBoolean firstAudit = new AtomicBoolean(true);
        for (NakedField fld : struct.getFields()) {
            if (fld instanceof CheckAware && ((CheckAware) fld).isAudit()) {
                validator.validate(fld, padWidth, 1, firstAudit);
            }
        }
        if (firstAudit.get()) {
            pw.printf("        return false;%n");
        } else {
            pw.printf("        return error;%n");
        }
        closeBrace(pw);
    }

    private void writeInitializer(@NotNull PrintWriter pw, ClassDefine struct, Defaults defaults) {
        pw.printf("    protected void initialize() {%n");
        val initializer = InitializeFactory.getInstance(pw, defaults);
        struct.getFields().forEach(it -> initializer.initialize(it, 1));
        closeBrace(pw);
    }

    private void writeCtorParm(@NotNull PrintWriter pw, @NotNull ClassDefine struct) {
        struct.onOverflowDefault(LoadOverflowAction.Trunc);
        struct.onUnderflowDefault(LoadUnderflowAction.Pad);
        pw.printf("    private %s(String s) {%n", struct.getName());
        pw.printf("        super(s, LRECL, %b, %b);%n",
                struct.onOverflowThrowError(), struct.onUnderflowThrowError());
        closeBrace(pw);
        pw.printf("    private %s(FixRecord r) {%n", struct.getName());
        pw.printf("        super(r, LRECL, %b, %b);%n",
                struct.onOverflowThrowError(), struct.onUnderflowThrowError());
        closeBrace(pw);
        pw.printf("    private %s(char[] c) {%n", struct.getName());
        pw.printf("        super(c, LRECL, %b, %b);%n",
                struct.onOverflowThrowError(), struct.onUnderflowThrowError());
        closeBrace(pw);

        pw.printf("    public static %s of(FixRecord r) {%n", struct.getName());
        pw.printf("        return new %s(r);%n", struct.getName());
        closeBrace(pw);
        pw.printf("    public static %s decode(String s) {%n", struct.getName());
        pw.printf("        return new %s(s);%n", struct.getName());
        closeBrace(pw);
        pw.printf("    public %s copy() {%n", struct.getName());
        pw.printf("        return new %s(Arrays.copyOf(rawData, LRECL));%n", struct.getName());
        closeBrace(pw);
    }

    private void writeCtorVoid(@NotNull PrintWriter pw, String name) {
        pw.printf("    public %s() {%n", name);
        pw.printf("        super(LRECL);%n");
        pw.printf("        initialize();%n");
        closeBrace(pw);
    }

    private void closeBrace(@NotNull PrintWriter pw) {
        pw.printf("    }%n");
    }

    private void writeConstant(@NotNull PrintWriter pw, @NotNull ParentFields struct) {
        pw.printf("    public static final int LRECL = %d;%n", struct.getLength());
        val preparer = PrepareFactory.getInstance(pw);
        struct.getFields().forEach(it -> preparer.prepare(it, 1));

    }

    private void writeBeginClass(@NotNull PrintWriter pw, @NotNull ClassDefine struct) {
        pw.printf("public class %s extends FixRecord {%n", struct.getName());
    }

    private void writeImport(@NotNull PrintWriter pw) {
        pw.printf("import java.util.Arrays;%n");
        pw.println();
        pw.printf("import %s.FieldValidateHandler;%n", CodeGenerator.SYSTEM_PACKAGE);
        pw.printf("import %s.FixRecord;%n", CodeGenerator.SYSTEM_PACKAGE);
        pw.printf("import %s.Action;%n", CodeGenerator.SYSTEM_PACKAGE);
        pw.println();
    }

    private void writePackage(PrintWriter pw, String packg, @NotNull GenerateArgs ga) {
        writeCopyright(pw, ga);
        pw.printf("package %s;%n%n", packg);
    }

}