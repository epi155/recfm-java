package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.AccesMode;
import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.GenerateArgs;
import io.github.epi155.recfm.api.WordWidth;
import io.github.epi155.recfm.type.*;
import io.github.epi155.recfm.util.Tools;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;

import static io.github.epi155.recfm.util.Tools.notNullOf;

public class InterfaceFactory extends CodeHelper {
    private static final String TAG_DOM = "Dom";
    private static final String TAG_CUS = "Cus";
    private static final String TAG_ABC = "Abc";
    private static final String TAG_NUM = "Num";
    private static final String STRING_SETTER = "void set%s(String s);%n";
    private static final String STRING_GETTER = "String get%s();%n";
    private static final String JAVADOC_OPEN = "/**%n";
    private static final String JAVADOC_CLOSE = " */%n";
    private final FieldDefault defaults;
    private boolean doc;

    public InterfaceFactory(PrintWriter pw, String wrtPackage, GenerateArgs ga, FieldDefault defaults) {
        super(pw, wrtPackage, ga);
        this.defaults = defaults;
    }

    public static InterfaceFactory newInstance(PrintWriter pw, String wrtPackage, GenerateArgs ga, FieldDefault fieldDefault) {
        return new InterfaceFactory(pw, wrtPackage, ga, fieldDefault);
    }

    public void generateInterfaceCode(TraitDefine trait) {
        this.doc = notNullOf(trait.getDoc(), defaults.getCls().isDoc());
        if (doc) {
            println("/**");
            traitDoc(trait);
            println(" */");
        }
        writeBeginProxy(trait);
        println();
        pushIndent(4);
        trait.forEachField(it -> {
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it);
            if (it instanceof FieldGroupTrait) createMethodsGroupProxy((FieldGroupTrait) it);
        });
        trait.forEachField(it -> {
            if (it instanceof SettableField) createMethods((SettableField) it);
        });
        popIndent();
        writeEndClass();
    }

    private void generateGroupCode(FieldGroup fld) {
        if (fld instanceof FieldOccurs) {
            writeBeginClassOccurs((FieldOccurs) fld);
        } else {
            writeBeginClassGroup(fld);
        }
        pushPlusIndent(4);
        fld.forEachField(it -> {
            if (it instanceof FieldGroup) generateGroupCode((FieldGroup) it);
        });
        fld.forEachField(it -> {
            if (it instanceof FloatingField) createMethods((FloatingField) it);
        });
        popIndent();
        writeEndClass();
    }

    private void writeBeginClassOccurs(FieldOccurs occurs) {
        String capName = Tools.capitalize(occurs.getName());
        printf("%s %s(int k);%n", capName, occurs.getName());
        if (doc) javadocGroupDef(occurs);
        printf("interface %s {%n", capName);
    }

    private void writeBeginClassGroup(FieldGroup group) {
        val name = group.getName();
        String capName = Tools.capitalize(name);
        printf("%s %s();%n", capName, name);
        if (doc) javadocGroupDef(group);
        printf("interface %s {%n", capName);
    }

    private void javadocGroupDef(FieldGroup group) {
        println("/**");
        traitDoc(group);
        println(" */");
    }

    private void createMethodsGroupProxy(FieldGroupTrait fld) {
        val clsName = fld.getTypedef().getName();
        if (doc) docProxyGetter(fld);
        printf("%s %s();%n", clsName, fld.getName());
    }

    private void docProxyGetter(FieldGroupTrait fld) {
        val clsName = fld.getTypedef().getName();
        printf(JAVADOC_OPEN);
        printf(" * {@link %s}(%d)%n", clsName, fld.getLength());
        printf(" * @return %s value%n", clsName);
        printf(JAVADOC_CLOSE);
    }

    private void writeBeginProxy(@NotNull TraitDefine proxy) {
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
        if (doc) docGetter(fld, TAG_ABC);
        printf(STRING_GETTER, wrkName);
        if (doc) docSetter(fld, TAG_ABC);
        printf(STRING_SETTER, wrkName);
    }

    private void createMethodsNum(FieldNum fld) {
        val wrkName = Tools.getWrkName(fld.getName());
        val access = notNullOf(fld.getAccess(), defaults.getNum().getAccess());
        if (access != AccesMode.Number) {
            if (doc) docGetter(fld, TAG_NUM);
            printf(STRING_GETTER, wrkName);
            if (doc) docSetter(fld, TAG_NUM);
            printf(STRING_SETTER, wrkName);
        }
        if (access != AccesMode.String) {
            val ww = notNullOf(fld.getWordWidth(), defaults.getNum().getWordWidth());
            boolean isNumber = access == AccesMode.Number;
            if (fld.getLength() > 19) useBigInt(fld, wrkName, isNumber);
            else if (fld.getLength() > 9 || ww == WordWidth.Long) useLong(fld, wrkName, isNumber);    // 10..19
            else if (fld.getLength() > 4 || ww == WordWidth.Int) useInt(fld, wrkName, isNumber);     // 5..9
            else if (fld.getLength() > 2 || ww == WordWidth.Short) useShort(fld, wrkName, isNumber);   // 3..4
            else useByte(fld, wrkName, isNumber);  // ..2
        }
    }

    private void useBigInt(FieldNum fld, String wrkName, boolean isNumber) {
        if (doc) docNumGetter(fld, "BigInteger");
        if (isNumber) {
            printf("BigInteger get%s();%n", wrkName);
        } else {
            printf("BigInteger bigInteger%s();%n", wrkName);
        }
        if (doc) docNumSetter(fld, "n BigInteger");
        printf("void set%s(BigInteger n);%n", wrkName);
    }
    private void useLong(FieldNum fld, String wrkName, boolean isNumber) {
        if (doc) docNumGetter(fld, "long");
        if (isNumber) {
            printf("long get%s();%n", wrkName);
        } else {
            printf("long long%s();%n", wrkName);
        }
        if (doc) docNumSetter(fld, "n long");
        printf("void set%s(long n);%n", wrkName);
    }

    private void useInt(FieldNum fld, String wrkName, boolean isNumber) {
        if (doc) docNumGetter(fld, "integer");
        if (isNumber) {
            printf("int get%s();%n", wrkName);
        } else {
            printf("int int%s();%n", wrkName);
        }
        if (doc) docNumSetter(fld, "n integer");
        printf("void set%s(int n);%n", wrkName);
    }

    private void useShort(FieldNum fld, String wrkName, boolean isNumber) {
        if (doc) docNumGetter(fld, "short");
        if (isNumber) {
            printf("short get%s();%n", wrkName);
        } else {
            printf("short short%s();%n", wrkName);
        }
        if (doc) docNumSetter(fld, "n short");
        printf("void set%s(short n);%n", wrkName);
    }

    private void useByte(FieldNum fld, String wrkName, boolean isNumber) {
        if (doc) docNumGetter(fld, "byte");
        if (isNumber) {
            printf("byte get%s();%n", wrkName);
        } else {
            printf("byte byte%s();%n", wrkName);
        }
        if (doc) docNumSetter(fld, "n byte");
        printf("void set%s(byte n);%n", wrkName);
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
        if (doc) docGetter(fld, TAG_CUS);
        printf(STRING_GETTER, wrkName);
        if (doc) docSetter(fld, TAG_CUS);
        printf(STRING_SETTER, wrkName);
    }

    private void createMethodsDomain(FieldDomain fld) {
        val wrkName = Tools.getWrkName(fld.getName());
        if (doc) docGetter(fld, TAG_DOM);
        printf(STRING_GETTER, wrkName);
        if (doc) docSetter(fld, TAG_DOM);
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
