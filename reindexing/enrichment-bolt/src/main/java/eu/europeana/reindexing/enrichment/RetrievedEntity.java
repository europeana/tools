/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment;

public class RetrievedEntity<T> {
    private String originalField;
     private String uri;
    private String originalLabel;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getOriginalLabel() {
        return originalLabel;
    }

    public void setOriginalLabel(String originalLabel) {
        this.originalLabel = originalLabel;
    }
    private T entity;

    public String getOriginalField() {
        return originalField;
    }

    public void setOriginalField(String originalField) {
        this.originalField = originalField;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
    
    
}
