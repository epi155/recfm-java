package io.github.epi155.recfm.type;

import io.github.epi155.recfm.api.FieldDefault;
import io.github.epi155.recfm.api.FieldModel;
import io.github.epi155.recfm.api.GenerateArgs;
import io.github.epi155.recfm.api.TraitModel;
import io.github.epi155.recfm.java.InterfaceFactory;
import io.github.epi155.recfm.util.Tools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class TraitDefine implements ParentFields, TraitModel {
    private String name;
    private int length;
    private List<FieldModel> fields = new ArrayList<>();
    private Boolean doc;

    protected static final String DOT_JAVA = ".java";
    @Override
    public void create(String namespace, GenerateArgs ga, FieldDefault defaults) {
        if (getFields().isEmpty()) return;
        val base = getFields().get(0).getOffset();

        log.info("- Prepare interface {} ...", getName());

        checkForVoid();
        boolean checkSuccesful = noBadName();
        checkSuccesful &= checkLength();
        checkSuccesful &= noDuplicateName(Tools::testCollision);
        checkSuccesful &= noHole(base);
        checkSuccesful &= noOverlap(base);
        if (checkSuccesful) {
            log.info("  [#####o] Creating ...");
            File srcMainJava = ga.sourceDirectory;
            File pkgFolder = new File(srcMainJava, namespace.replace('.', File.separatorChar));
            File clsFile = new File(pkgFolder, getName()+DOT_JAVA);
            try (PrintWriter pw = new PrintWriter(clsFile)) {
                InterfaceFactory factory = InterfaceFactory.newInstance(pw, namespace, ga, defaults);
                factory.writePackage();
                factory.generateInterfaceCode(this);
            } catch (IOException e) {
                throw new ClassDefineException(e);
            }
            log.info("  [######] Created.");
        } else {
            throw new ClassDefineException("Class <" + getName() + "> bad defined");
        }



    }
}
