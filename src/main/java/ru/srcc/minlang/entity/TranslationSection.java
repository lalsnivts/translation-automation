package ru.srcc.minlang.entity;

import java.util.ArrayList;
import java.util.List;

public class TranslationSection {
    private String sectionName;
    private List<TranslationKey> keys = new ArrayList<TranslationKey>();

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<TranslationKey> getKeys() {
        return keys;
    }

    public void setKeys(List<TranslationKey> keys) {
        this.keys = keys;
    }
}
