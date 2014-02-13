package eu.europeana.enrichment.model.internal;

import net.vz.mongodb.jackson.Id;

public class MongoTerm {

	@Id
	private String id;
	private String codeUri;
	private String label;
	private String originalLabel;
	private String lang;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCodeUri() {
		return codeUri;
	}

	public void setCodeUri(String codeUri) {
		this.codeUri = codeUri;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getOriginalLabel() {
		return originalLabel;
	}

	public void setOriginalLabel(String originalLabel) {
		this.originalLabel = originalLabel;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
}
