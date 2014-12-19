package eu.europeana.enrichment.harvester.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.enrichment.harvester.api.AgentMap;
import eu.europeana.enrichment.harvester.database.DataManager;

public class ContentChecker {

	private final DataManager dm = new DataManager();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ContentChecker coce= new ContentChecker();
		coce.compareEntities();
	}

	public void compareEntities() {

		int resultsize = 1000;
        int limit = 1000;
        int offset = 0;
        int sameAs=0;
        int noSameAs=0;
        while (resultsize == limit) {

            List<AgentMap> agents = dm.extractAllAgentsFromLocalStorage(limit, offset);
            resultsize = agents.size();
            for (AgentMap am : agents) {
                if (compareData(am.getAgentUri().toASCIIString(), am.getSameAs())){
                	sameAs++;
                }
                else
                	noSameAs++;
            }
            if (agents.size() == limit) {
            	
                offset = offset + limit;
            }
        }
        
        System.out.println("sameAs n.: "+sameAs+", no sameAs n.: "+noSameAs);

    }
	private void compareDates(Map <String, List<String>> dates, Map <String, List<String>> saDates){
		//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
		String[] formatStrings = {"yyyy-mm-dd", "yyyy"};
		for (String dobai:(dates.keySet())){
			List <String> aiBD=dates.get(dobai);
			for (String bd:aiBD){
				Date date = null;
				Date sadate=null;
				try {
					
					for (String formatString : formatStrings)
				    {
				        try
				        {
				        	date = new SimpleDateFormat(formatString).parse(bd);//formatter.parse(sabd);
				        }
				        catch (ParseException e) {}
				    }
					
					for (String sadobai:(saDates.keySet())){
						List <String> saaiBD=saDates.get(sadobai);
						for (String sabd:saaiBD){
							
							
							for (String formatString : formatStrings)
						    {
						        try
						        {
						        	sadate = new SimpleDateFormat(formatString).parse(sabd);//formatter.parse(sabd);
						        }
						        catch (ParseException e) {}
						    }
							
							//System.out.println("Then: "+date.compareTo(sadate));
							if 	(date.compareTo(sadate)!=0){
								//System.out.println(date +" - "+ sadate);
								 System.out.println(date +" - "+ sadate + " ("+date.compareTo(sadate)+")");
							}
							//else System.out.println(date+" - "+ sadate + " ("+date.compareTo(sadate)+")");
								
						}
					}
				} catch (Exception e) {
					//e.printStackTrace();
					//System.out.println("Exception: "+date +" - "+ saDates);
				}
			}
			
		
			
			
		}
	}
	
	public boolean compareData(String id, List<String>sameAs) {
		
		if (sameAs !=null && sameAs.size()==1){
			//System.out.println("Checking: "+id+" "+sameAs.toString());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
			String[] formatStrings = {"yyyy-mm-dd", "yyyy"};
			AgentImpl ai= dm.getAgent(id);
			AgentImpl saai= dm.getAgent(sameAs.get(0));
			if (ai!=null && saai!=null && ai.getRdaGr2DateOfBirth()!=null && saai.getRdaGr2DateOfBirth()!=null){
				System.out.println(id+ ": "+sameAs.get(0));
				compareDates(ai.getRdaGr2DateOfBirth(), saai.getRdaGr2DateOfBirth());
				
			}
				
			return true;
		}
			
		else{
			System.out.println("Checking*************************************: "+id);
			return false;
		}
			
		
	}
}
