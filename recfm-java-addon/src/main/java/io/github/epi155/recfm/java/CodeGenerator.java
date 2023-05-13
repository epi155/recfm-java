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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class CodeGenerator implements IndentAble, CodeProvider {
    private static final String SYSTEM_PACKAGE = "io.github.epi155.recfm.java";
    private static final String OVERRIDE_METHOD = "    @Override%n";
    private static final String ADDON_GROUP = "io.github.epi155";
    private static final String ADDON_ARTIFACT = "recfm-java-addon";
    private static final String ADDON_VERSION = versionForClass(CodeGenerator.class);
    static void writeCopyright(@NotNull PrintWriter pw, @NotNull GenerateArgs ga) {
        String now = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        pw.println("/*");
        pw.printf(" * Code Generated at %s%n", now);
        pw.printf(" * Plugin: %s:%s:%s%n", ga.group, ga.artifact, ga.version);
        pw.printf(" * Add-On: %s:%s:%s%n", ADDON_GROUP, ADDON_ARTIFACT, ADDON_VERSION);
        pw.println(" */");
    }
    public static String versionForClass(Class<?> cls) {
        String implementationVersion = cls.getPackage().getImplementationVersion();
        if (implementationVersion != null) {
            return implementationVersion;
        }
        URL codeSourceLocation = cls.getProtectionDomain().getCodeSource().getLocation();
        try {
            URLConnection connection = codeSourceLocation.openConnection();
            if (connection instanceof JarURLConnection) {
                JarURLConnection jarURLConnection = (JarURLConnection) connection;
                return getImplementationVersion(jarURLConnection.getJarFile());
            }
            try (JarFile jarFile = new JarFile(new File(codeSourceLocation.toURI()))) {
                return getImplementationVersion(jarFile);
            }
        }
        catch (Exception ex) {
            return null;
        }
    }
    private static String getImplementationVersion(JarFile jarFile) throws IOException {
        return jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
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
        if (ga.doc) {
            pw.println("/**");
            tableDoc(pw, 0, struct);
            pw.println(" */");
        }
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

    private void tableDoc(PrintWriter pw, int indent, ParentFields fields) {
        class IndentPrint {
            void printf(String fmt, Object...args) {
                indent(pw, indent);
                pw.printf(fmt, args);
            }
        }
        val topName = fields.getName();
        val topWidth = widthOf(fields);
        val p = new IndentPrint();
        p.printf(" * <table class='striped'>%n");
        p.printf(" * <caption>%s component %s</caption>%n", topName, topWidth);
        p.printf(" * <tr><th>Field Name</th><th>Type</th><th>Start</th><th>Length</th></tr>%n");
        for (NakedField field: fields.getFields()) {
            if (field instanceof NamedField) {
                val named = (NamedField) field;
                val capit = Tools.capitalize(named.getName());
                if (named.isRedefines())
                    continue;
                if (field instanceof FieldOccurs) {
                    int times = ((FieldOccurs) field).getTimes();
                    p.printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d}</td><td style='text-align: right'>{@code %5$d}</td><td style='text-align: right'>x{@code %6$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset(), named.getLength(), times);
                } else if (field instanceof FieldGroup) {
                    p.printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d}</td><td style='text-align: right'>{@code %5$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset(), named.getLength());
                } else {
                    p.printf(" * <tr><td>{@code %s}</td><td style='text-align: center'>{@code %s}</td><td style='text-align: right'>{@code %d}</td><td style='text-align: right'>{@code %d}</td></tr>%n", named.getName(), typeOf(named), named.getOffset(), named.getLength());
                }
            } else {
                p.printf(" * <tr><td></td><td style='text-align: center'>{@code %s}</td><td style='text-align: right'>{@code %d}</td><td style='text-align: right'>{@code %d}</td></tr>%n", typeOf(field), field.getOffset(), field.getLength());
            }
        }
        p.printf(" * </table>%n");
    }

    private String widthOf(ParentFields fields) {
        int len = fields.getLength();
        if (fields instanceof FieldOccurs) {
            val o = (FieldOccurs) fields;
            int mul = o.getTimes();
            return String.format("[%d x %d]", len, mul);
        }
        return String.format("[%d]", len);
    }

    private String typeOf(NakedField field) {
        if (field instanceof FieldOccurs) return "Occ";
        if (field instanceof FieldGroup) return "Grp";
        if (field instanceof FieldAbc) return "Abc";
        if (field instanceof FieldNum) return "Num";
        if (field instanceof FieldCustom) return "Cus";
        if (field instanceof FieldDomain) return "Dom";
        if (field instanceof FieldFiller) return "Fil";
        if (field instanceof FieldConstant) return "Val";
        return "???";
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
            writeBeginClassOccurs(pw, (FieldOccurs) fld, indent, ga);
            access = AccessFactory.getInstance(pw, defaults, n -> String.format("%d+shift", n - 1));
        } else {
            writeBeginClassGroup(pw, fld, indent, ga);
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

    private void writeBeginClassOccurs(PrintWriter pw, @NotNull FieldOccurs fld, int indent, GenerateArgs ga) {
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
        pw.printf("public void with%1$s(int k, WithAction<%1$s> action) { action.call(this.%2$s[k-1]); }%n", capName, fld.getName());
        if (ga.doc)
            javadocGroup(pw, fld, capName, indent);
        indent(pw, indent);
        pw.printf("public class %s {%n", capName);

        indent(pw, indent);
        pw.printf("    private final int shift;%n");
        indent(pw, indent);
        pw.printf("    private %s(int shift) { this.shift = shift; }%n", capName);
    }

    private void writeBeginClassGroup(PrintWriter pw, FieldGroup group, int indent, GenerateArgs ga) {
        val name = group.getName();
        String capName = Tools.capitalize(name);
        indent(pw, indent);
        pw.printf("private final %s %s = this.new %1$s();%n", capName, name);
        indent(pw, indent);
        pw.printf("public %s %s() { return this.%2$s; }%n", capName, name);
        indent(pw, indent);
        pw.printf("public void with%1$s(WithAction<%1$s> action) { action.call(this.%2$s); }%n", capName, name);
        if (ga.doc)
            javadocGroupWith(pw, group, capName, indent);
        indent(pw, indent);
        pw.printf("public class %s {%n", capName);
    }

    private void javadocGroupWith(PrintWriter pw, FieldGroup group, String capName, int indent) {
        indent(pw, indent);
        pw.println("/**");
        indent(pw, indent);
        pw.printf(" * Consume %s component %n", capName);
        indent(pw, indent);
        pw.println(" * <p>");
        tableDoc(pw, indent, group);
        indent(pw, indent);
        pw.printf(" * @param action {@link %s} consumer%n", capName);
        indent(pw, indent);
        pw.println(" */");
    }

    private void javadocGroup(PrintWriter pw, FieldGroup group, String capName, int indent) {
        indent(pw, indent);
        pw.println("/**");
        indent(pw, indent);
        pw.printf(" * Access %s component %n", capName);
        indent(pw, indent);
        pw.println(" * <p>");
        tableDoc(pw, indent, group);
        indent(pw, indent);
        pw.printf(" * @return {@link %s} instance%n", capName);
        indent(pw, indent);
        pw.println(" */");
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
        pw.printf("import %s.*;%n", CodeGenerator.SYSTEM_PACKAGE);
        pw.println();
    }

    private void writePackage(PrintWriter pw, String packg, @NotNull GenerateArgs ga) {
        writeCopyright(pw, ga);
        pw.printf("package %s;%n%n", packg);
    }

}