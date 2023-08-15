package io.github.epi155.recfm.java;

import io.github.epi155.recfm.api.CodeFactory;
import io.github.epi155.recfm.api.CodeProvider;

public class CodeGenerator implements CodeProvider {
    @Override
    public CodeFactory getInstance() {
        return CodeFactoryImpl.getInstance();
    }
}