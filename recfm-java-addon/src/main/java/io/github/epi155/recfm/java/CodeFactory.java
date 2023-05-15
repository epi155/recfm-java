package io.github.epi155.recfm.java;

import io.github.epi155.recfm.java.factory.CodeWriter;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.Tools;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public abstract class CodeFactory implements CodeWriter {
    protected static final String SYSTEM_PACKAGE = "io.github.epi155.recfm.java";
    protected static final String OVERRIDE_METHOD = "@Override%n";
    protected static final String ADDON_GROUP = "io.github.epi155";
    protected static final String ADDON_ARTIFACT = "recfm-java-addon";
    protected static final String ADDON_VERSION = versionForClass(CodeGenerator.class);
    private final Deque<String> indentStack = new LinkedList<>();
    private final PrintWriter pw;
    protected final String wrtPackage;
    protected final GenerateArgs ga;

    protected CodeFactory(PrintWriter pw, String wrtPackage, GenerateArgs ga) {
        this.pw = pw;
        this.wrtPackage = wrtPackage;
        this.ga = ga;
    }

    public void pushIndent(int width) {
        String indent = CharBuffer.allocate(width).toString().replace('\0', ' ');
        indentStack.push(indent);
    }
    public void pushPlusIndent(int width) {
        int prevWidth = indentStack.isEmpty() ? 0 : indentStack.peek().length();
        String indent = CharBuffer.allocate(prevWidth + width).toString().replace('\0', ' ');
        indentStack.push(indent);
    }

    public void popIndent() {
        indentStack.pop();
    }

    public void printf(String format, Object... args) {
        if (!indentStack.isEmpty()) {
            pw.write(indentStack.peek());
        }
        pw.printf(format, args);
    }
    public void println(String s) {
        if (!indentStack.isEmpty()) {
            pw.write(indentStack.peek());
        }
        pw.println(s);
    }
    public void println() {
        pw.println();
    }

    protected void writeCopyright() {
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
    public void writePackage() {
        writeCopyright();
        pw.printf("package %s;%n%n", wrtPackage);
    }
    protected void writeEndClass() {
        println("}");
    }
    protected void tableDoc(ParentFields fields) {
        val topName = fields.getName();
        val topWidth = widthOf(fields);
        printf(" * <table class='striped'>%n");
        printf(" * <caption>%s component %s</caption>%n", topName, topWidth);
        printf(" * <tr><th>Field Name</th><th>Type</th><th>Start</th><th>Length</th></tr>%n");
        for (NakedField field: fields.getFields()) {
            if (field instanceof NamedField) {
                val named = (NamedField) field;
                val capit = Tools.capitalize(named.getName());
                if (named.isRedefines())
                    continue;
                if (field instanceof FieldOccurs) {
                    int times = ((FieldOccurs) field).getTimes();
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d}</td><td style='text-align: right'>{@code %5$d}</td><td style='text-align: right'>x{@code %6$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset(), named.getLength(), times);
                } else if (field instanceof FieldOccursProxy) {
                    int times = ((FieldOccursProxy) field).getTimes();
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d}</td><td style='text-align: right'>{@code %5$d}</td><td style='text-align: right'>x{@code %6$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset(), named.getLength(), times);
                } else if (field instanceof FieldGroup || field instanceof FieldGroupProxy) {
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d}</td><td style='text-align: right'>{@code %5$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset(), named.getLength());
                } else {
                    printf(" * <tr><td>{@code %s}</td><td style='text-align: center'>{@code %s}</td><td style='text-align: right'>{@code %d}</td><td style='text-align: right'>{@code %d}</td></tr>%n", named.getName(), typeOf(named), named.getOffset(), named.getLength());
                }
            } else {
                printf(" * <tr><td></td><td style='text-align: center'>{@code %s}</td><td style='text-align: right'>{@code %d}</td><td style='text-align: right'>{@code %d}</td></tr>%n", typeOf(field), field.getOffset(), field.getLength());
            }
        }
        printf(" * </table>%n");
    }
    protected void proxyDoc(ParentFields fields) {
        val topName = fields.getName();
        val topWidth = widthOf(fields);
        int backShift = fields.getFields().isEmpty() ? 0 : fields.getFields().get(0).getOffset();
        printf(" * <table class='striped'>%n");
        printf(" * <caption>%s component %s</caption>%n", topName, topWidth);
        printf(" * <tr><th>Field Name</th><th>Type</th><th>Offset</th><th>Length</th></tr>%n");
        for (NakedField field: fields.getFields()) {
            if (field instanceof NamedField) {
                val named = (NamedField) field;
                val capit = Tools.capitalize(named.getName());
                if (named.isRedefines())
                    continue;
                if (field instanceof FieldOccurs) {
                    int times = ((FieldOccurs) field).getTimes();
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d+}</td><td style='text-align: right'>{@code %5$d}</td><td style='text-align: right'>x{@code %6$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset()-backShift, named.getLength(), times);
                } else if (field instanceof FieldOccursProxy) {
                    int times = ((FieldOccursProxy) field).getTimes();
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d+}</td><td style='text-align: right'>{@code %5$d}</td><td style='text-align: right'>x{@code %6$d}</td></tr>%n",
                            ((FieldOccursProxy) field).getTypeDef().getName(), named.getName(), typeOf(named), named.getOffset()-backShift, named.getLength(), times);
                } else if (field instanceof FieldGroup) {
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d+}</td><td style='text-align: right'>{@code %5$d}</td></tr>%n",
                            capit, named.getName(), typeOf(named), named.getOffset()-backShift, named.getLength());
                } else if (field instanceof FieldGroupProxy) {
                    printf(" * <tr><td>{@link %1$s %2$s}</td><td style='text-align: center'>{@code %3$s}</td><td style='text-align: right'>{@code %4$d+}</td><td style='text-align: right'>{@code %5$d}</td></tr>%n",
                            ((FieldGroupProxy) field).getTypeDef().getName(), named.getName(), typeOf(named), named.getOffset()-backShift, named.getLength());
                } else {
                    printf(" * <tr><td>{@code %s}</td><td style='text-align: center'>{@code %s}</td><td style='text-align: right'>{@code %d+}</td><td style='text-align: right'>{@code %d}</td></tr>%n", named.getName(), typeOf(named), named.getOffset()-backShift, named.getLength());
                }
            } else {
                printf(" * <tr><td></td><td style='text-align: center'>{@code %s}</td><td style='text-align: right'>{@code %d+}</td><td style='text-align: right'>{@code %d}</td></tr>%n", typeOf(field), field.getOffset()-backShift, field.getLength());
            }
        }
        printf(" * </table>%n");
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
        if (field instanceof FieldOccursProxy) return "OCC";
        if (field instanceof FieldGroupProxy) return "GRP";
        return "???";
    }
}
