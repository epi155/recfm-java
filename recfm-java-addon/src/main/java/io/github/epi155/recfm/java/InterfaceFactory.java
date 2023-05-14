package io.github.epi155.recfm.java;

import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.GenerateArgs;
import io.github.epi155.recfm.util.Tools;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

public class InterfaceFactory extends CodeFactory{
    private static final String TAG_DOM = "Dom";
    private static final String TAG_CUS = "Cus";
    private static final String TAG_ABC = "Abc";
    private static final String TAG_NUM = "Num";
    private static final String STRING_SETTER = "public void set%s(String s);%n";
    private static final String STRING_GETTER = "public String get%s();%n";
    private static final String JAVADOC_OPEN = "/**%n";
    private static final String JAVADOC_CLOSE = " */%n";

    public InterfaceFactory(PrintWriter pw, String wrtPackage, GenerateArgs ga) {
        super(pw, wrtPackage, ga);
    }

    public static InterfaceFactory newInstance(PrintWriter pw, String wrtPackage, GenerateArgs ga) {
        return new InterfaceFactory(pw, wrtPackage, ga);
    }

    public void generateInterfaceCode(ClassDefine proxy) {
        if (ga.doc) {
            println("/**");
            proxyDoc(proxy);
            println(" */");
        }
        writeBeginProxy(proxy);
        println();
        pushIndent(4);
//        proxy.getFields().forEach(it -> {
//            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it, 4);
//            if (it instanceof FieldGroupProxy) generateGroupCode((FieldGroupProxy) it, 4);
//        });
        proxy.getFields().forEach(it -> {
            if (it instanceof SettableField) createMethods((SettableField) it);
        });
        popIndent();
        writeEndClass();
    }

    private void writeBeginProxy(@NotNull ClassDefine proxy) {
        printf("public interface %s {%n", proxy.getName());
    }

    private void createMethods(SettableField fld) {
        if (fld instanceof FieldAbc) {
            createMethodsAbc((FieldAbc) fld);
        } else if (fld instanceof FieldNum) {
            createMethodsNum((FieldNum) fld);
        } else if (fld instanceof FieldCustom) {
            createMethodsCustom((FieldCustom) fld);
        } else if (fld instanceof FieldDomain) {
            createMethodsDomain((FieldDomain) fld);
        } else {
            throw new IllegalStateException("Unknown field type " + fld.getName() +": " + fld.getClass().getSimpleName());
        }
    }

    private void createMethodsAbc(FieldAbc fld) {
        val wrkName = Tools.getWrkName(fld.getName());
        if (ga.doc) docGetter(fld, TAG_ABC);
        printf(STRING_GETTER, wrkName);
        if (ga.doc) docSetter(fld, TAG_ABC);
        printf(STRING_SETTER, wrkName);
    }

    private void createMethodsNum(FieldNum fld) {
        val wrkName = Tools.getWrkName(fld.getName());
        if (ga.doc) docGetter(fld, TAG_NUM);
        printf(STRING_GETTER, wrkName);
        if (ga.doc) docSetter(fld, TAG_NUM);
        printf(STRING_SETTER, wrkName);
        if (fld.isNumericAccess()) {
            if (fld.getLength() > 19)
                throw new IllegalStateException("Field "+fld.getName()+" too large "+fld.getLength()+"-digits for numeric access");
            else if (fld.getLength() > 9) useLong(fld, wrkName);    // 10..19
            else if (fld.getLength() > 4 || ga.align == 4) useInt(fld, wrkName);     // 5..9
            else if (fld.getLength() > 2 || ga.align == 2) useShort(fld, wrkName);   // 3..4
            else useByte(fld, wrkName);  // ..2
        }
    }

    private void useLong(FieldNum fld, String wrkName) {
        if (ga.doc) docNumGetter(fld, "long");
        printf("public long long%s();%n", wrkName);
        if (ga.doc) docNumSetter(fld, "n long");
        printf("public void set%s(long n);%n", wrkName);
    }

    private void useInt(FieldNum fld, String wrkName) {
        if (ga.doc) docNumGetter(fld, "integer");
        printf("public int int%s();%n", wrkName);
        if (ga.doc) docNumSetter(fld, "n integer");
        printf("public void set%s(int n);%n", wrkName);
    }

    private void useShort(FieldNum fld, String wrkName) {
        if (ga.doc) docNumGetter(fld, "short");
        printf("public short short%s();%n", wrkName);
        if (ga.doc) docNumSetter(fld, "n short");
        printf("public void set%s(short n);%n", wrkName);
    }

    private void useByte(FieldNum fld, String wrkName) {
        if (ga.doc) docNumGetter(fld, "byte");
        printf("public byte byte%s();%n", wrkName);
        if (ga.doc) docNumSetter(fld, "n byte");
        printf("public void set%s(byte n);%n", wrkName);
    }

    private void docNumGetter(FieldNum fld, String dsType) {
        printf(JAVADOC_OPEN);
        printf(" * Num(@%d)%n", fld.getLength());
        printf(" * @return %s value%n", dsType);
        printf(JAVADOC_CLOSE);
    }

    private void docNumSetter(FieldNum fld, String dsResult) {
        printf(JAVADOC_OPEN);
        printf(" * Num(@%d)%n", fld.getLength());
        printf(" * @param %s value%n", dsResult);
        printf(JAVADOC_CLOSE);
    }

    private void createMethodsCustom(FieldCustom fld) {
        val wrkName = Tools.getWrkName(fld.getName());
        if (ga.doc) docGetter(fld, TAG_CUS);
        printf(STRING_GETTER, wrkName);
        if (ga.doc) docSetter(fld, TAG_CUS);
        printf(STRING_SETTER, wrkName);
    }

    private void createMethodsDomain(FieldDomain fld) {
        val wrkName = Tools.getWrkName(fld.getName());
        if (ga.doc) docGetter(fld, TAG_DOM);
        printf(STRING_GETTER, wrkName);
        if (ga.doc) docSetter(fld, TAG_DOM);
        printf(STRING_SETTER, wrkName);
    }

    private void docGetter(NakedField fld, String tag) {
        printf(JAVADOC_OPEN);
        printf(" * %s(%d)%n", tag, fld.getLength());
        printf(" * @return string value%n");
        printf(JAVADOC_CLOSE);
    }

    private void docSetter(NakedField fld, String tag) {
        printf(JAVADOC_OPEN);
        printf(" * %s(%d)%n", tag, fld.getLength());
        printf(" * @param s string value%n");
        printf(JAVADOC_CLOSE);
    }

}
