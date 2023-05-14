package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.CodeProvider;
import io.github.epi155.recfm.type.ClassDefine;
import io.github.epi155.recfm.type.ClassDefineException;
import io.github.epi155.recfm.type.Defaults;
import io.github.epi155.recfm.util.GenerateArgs;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeGenerator implements CodeProvider {
    private static final String DOT_JAVA = ".java";

    @Override
    public void createInterface(File srcMainJava, String wrtPackage, ClassDefine proxy, GenerateArgs ga) {
        File pkgFolder = new File(srcMainJava, wrtPackage.replace('.', File.separatorChar));
        File clsFile = new File(pkgFolder, proxy.getName()+DOT_JAVA);
        try (PrintWriter pw = new PrintWriter(clsFile)) {
            InterfaceFactory factory = InterfaceFactory.newInstance(pw, wrtPackage, ga);
            factory.writePackage();
            factory.generateInterfaceCode(proxy);
        } catch (IOException e) {
            throw new ClassDefineException(e);
        }
    }

    @Override
    public void createClass(File srcMainJava, String wrtPackage, ClassDefine clazz, GenerateArgs ga, Defaults defaults) {
        File pkgFolder = new File(srcMainJava, wrtPackage.replace('.', File.separatorChar));
        File clsFile = new File(pkgFolder, clazz.getName()+DOT_JAVA);
        try (PrintWriter pw = new PrintWriter(clsFile)) {
            ClassFactory factory = ClassFactory.newInstance(pw, wrtPackage, ga, defaults);
            factory.writePackage();
            factory.writeImport();
            factory.generateClassCode(clazz);
        } catch (IOException e) {
            throw new ClassDefineException(e);
        }
    }
}