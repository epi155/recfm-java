package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.*;
import io.github.epi155.recfm.java.ClassFactory;
import io.github.epi155.recfm.util.Tools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static io.github.epi155.recfm.type.TraitDefine.DOT_JAVA;

@Data
@Slf4j
public class ClassDefine implements ParentFields, ClassModel {
    private String name;
    private int length;
    private LoadOverflowAction onOverflow;
    private LoadUnderflowAction onUnderflow;
    private Boolean doc;
    private List<FieldModel> fields = new ArrayList<>();

    @Override
    public void create(String namespace, GenerateArgs ga, FieldDefault defaults) {
        log.info("- Prepare class {} ...", getName());

        checkForVoid();

        boolean checkSuccesful = noBadName();
        checkSuccesful &= checkLength();
        checkSuccesful &= noDuplicateName(Tools::testCollision);
        checkSuccesful &= noHole();
        checkSuccesful &= noOverlap();
        if (checkSuccesful) {
            log.info("  [#####o] Creating ...");
            File srcMainJava = new File(ga.sourceDirectory);
            File pkgFolder = new File(srcMainJava, namespace.replace('.', File.separatorChar));
            File clsFile = new File(pkgFolder, getName()+DOT_JAVA);
            try (PrintWriter pw = new PrintWriter(clsFile)) {
                ClassFactory factory = ClassFactory.newInstance(pw, namespace, ga, defaults);
                factory.writePackage();
                factory.writeImport();
                factory.generateClassCode(this);
            } catch (IOException e) {
                throw new ClassDefineException(e);
            }
            log.info("  [######] Created.");
        } else {
            throw new ClassDefineException("Class <" + getName() + "> bad defined");
        }


    }
}
