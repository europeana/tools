package eu.europeana.datamigration.ese2edm.helpers;

/**
 * Hash Incrementor utility
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class HashIncrementor {

	private final static int ZERO = 48;
	private final static int A = 65;
	private final static int G= 70;
	private final static int NINE = 57;
	private final static String FINISHED = "finished";
	
	/**
	 * Increment a hash
	 * @param The hash to increment
	 * @return The new hash
	 */
	public static String incrementHash(String hash){
		
		char[] chars = hash.toCharArray();
		chars[chars.length-1]++;
		for (int i=chars.length-1;i>-1;i--){
			
			if(chars[i]>G){
				try{
					chars[i]=ZERO;
					chars[i-1]++;
				} catch (ArrayIndexOutOfBoundsException e){
					return FINISHED;
				}
			} else if(chars[i]>NINE && chars[i]<A){
				chars[i]=A;
			} 
			
		}
		
		return new String(chars);
	}
	
}
