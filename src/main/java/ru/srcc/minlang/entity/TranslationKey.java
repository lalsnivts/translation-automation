package ru.srcc.minlang.entity;

public class TranslationKey {
    private String russian;
    private String translationIntoLanguage;
    private String keyId;
    private String description = "";
    private boolean isTranslated = false;

    public String getRussian() {
        return russian;
    }

    public void setRussian(String russian) {
        this.russian = russian;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isTranslated() {
        return isTranslated;
    }

    public void setTranslated(boolean translated) {
        isTranslated = translated;
    }

    public String getTranslationIntoLanguage() {
        return translationIntoLanguage;
    }

    public void setTranslationIntoLanguage(String translationIntoLanguage) {
        this.translationIntoLanguage = translationIntoLanguage;
    }
}
