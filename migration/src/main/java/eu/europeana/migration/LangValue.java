package eu.europeana.migration;

import java.util.List;

/**
 * Created by ymamakis on 4/5/16.
 */
public class LangValue {
    private String language;
    private List<String> value;
    private String vocabulary;
    private String originalField;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

    public String getOriginalField() {
        return originalField;
    }

    public void setOriginalField(String originalField) {
        this.originalField = originalField;
    }
}
