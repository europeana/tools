package eu.europeana.record.management.shared.exceptions;

public class NoRecordException extends RuntimeException{
	String record;
	public NoRecordException() {
		// TODO Auto-generated constructor stub
	}
	public NoRecordException(String message){
		this.record = message;
	}
	@Override
	public String getMessage() {
		return this.record;
	}
}
