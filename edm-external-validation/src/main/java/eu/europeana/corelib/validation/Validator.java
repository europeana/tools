package eu.europeana.corelib.validation;


public class Validator {
	
	public static void main(String[] args) {
		
		if (args == null || args.length == 0) {
			throw new IndexOutOfBoundsException(
					"Did not provide correct number of arguments.\nExpecting file");
		}
		EDMValidator validator = new EDMValidator();
		validator.validate(args[0]);

	}
	
	 
}
