package eu.europeana.enrichment.api.external;

public class  EnrichmentError {

	public EnrichmentError(){
		
	}
	
	public EnrichmentError(String cause,String details){
		this.cause = cause;
		this.details = details;
		
	}
	
	private String details;

	private String cause;
	
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}
	
	
}
