package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.datamigration.ese2edm.converters.Ese2EdmSolrCollectionChecker;

public class SolrCollectionFixer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> collections;
		try {
			collections = IOUtils.readLines(new FileInputStream(new File(
					args[0])));
//			collections = IOUtils.readLines(new FileInputStream(new File(
//					"/home/gmamakis/Documents/final_migration_diff/check")));
			List<String> inputCollections = new ArrayList<String>();
			List<Integer> starts = new ArrayList<Integer>();
			List<Integer> ends = new ArrayList<Integer>();
			List<Integer> diff = new ArrayList<Integer>();
			
//			int i = 0;
			for (String collection : collections) {
				String[] arguments = StringUtils.split(collection, " ");
				
				inputCollections.add(arguments[0]);
				starts.add(Integer.parseInt(arguments[1]));
				ends.add(Integer.parseInt(arguments[2]));
				diff.add(Integer.parseInt(arguments[3]));
//				if (inputCollections.size() == Integer.parseInt(args[2])
//						|| i== collections
//								.size()) {
//				if (inputCollections.size() ==4
//						|| i== collections
//								.size()) {
					
					
//					System.out.println("# of Threads:" +i/4);
//					System.out.println("In collection:" +i);
//					inputCollections = new ArrayList<String>();
//					starts = new ArrayList<Integer>();
//					ends = new ArrayList<Integer>();
				}
			Ese2EdmSolrCollectionChecker converter = new Ese2EdmSolrCollectionChecker();
			converter.setCollections(inputCollections);
			converter.setStarts(starts);
			converter.setEnds(ends);
			converter.setThreadName(1);
			converter.setDiff(diff);
			converter.setCreateRDF(false);
			Thread t = new Thread(converter);
			t.start();
//				i++;
//			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
