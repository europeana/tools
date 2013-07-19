package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.europeana.datamigration.ese2edm.converters.Ese2EdmRecordConverter;

public class MongoRecordFinder {

	public static void main(String... args){
	//	try {
//			List<String> collections = IOUtils.readLines(new FileInputStream(new File(
//					args[0])));
//			List<String> collections = IOUtils.readLines(new FileInputStream(new File(
//					"/home/gmamakis/Documents/migration_collections/results_mongo.csv")));
//			List<String> inputCollections = new ArrayList<String>();
//			for(String collection:collections){
//				String coll = collection.split(",")[0];
//				inputCollections.add(coll.split("_")[0]);
//			}
			
			Ese2EdmRecordConverter converter = new Ese2EdmRecordConverter();
			converter.setStart(Integer.parseInt(args[0]));
			converter.setEnd(Integer.parseInt(args[1]));
//			converter.setStart(8500000);
//			converter.setEnd(17000000);
//			converter.setCollections(inputCollections);
			Thread t = new Thread(converter);
			t.start();
			
		//} 
//		catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
	}


	
}
