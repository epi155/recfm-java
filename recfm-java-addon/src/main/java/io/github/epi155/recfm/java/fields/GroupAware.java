package io.github.epi155.recfm.java.fields;

import io.github.epi155.recfm.api.FieldModel;

import java.util.List;

public interface GroupAware {
    boolean isOverride();
    String getName();
    List<FieldModel> getFields();

}
