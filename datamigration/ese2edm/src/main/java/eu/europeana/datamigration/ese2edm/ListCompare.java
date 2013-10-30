package eu.europeana.datamigration.ese2edm;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class ListCompare {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
//			List<String> fileA = IOUtils.readLines(new FileInputStream(args[0]));
//			List<String> fileB = IOUtils.readLines(new FileInputStream(args[1]));
			List<String> fileA = IOUtils.readLines(new FileInputStream("/home/gmamakis/data_migration sets/final/91637_original"));
			System.out.println("read first file");
			List<String> fileB = IOUtils.readLines(new FileInputStream("/home/gmamakis/data_migration sets/final/91637_migrated"));
			System.out.println("read second file");
			List<String> normalizedA = new ArrayList<String>();
			List<String> normalizedB = new ArrayList<String>();
			List<String> diff = new ArrayList<String>();
			int inc=0;
			for (String a: fileA){
				normalizedA.add(StringUtils.substringAfterLast(a, "/"));
			}
			System.out.println("normalized first file");
			for (String b: fileB){
				normalizedB.add(StringUtils.substringAfterLast(b, "/"));
			}
			System.out.println("normalized second file");
			for(String c: normalizedA){
				if(!normalizedB.contains(c)){
					diff.add(fileA.get(inc));
				}
				if(inc % 10000 == 0)
					System.out.println("Passed through " + inc);
				inc++;
				
			}
			System.out.println("saving");
			IOUtils.writeLines(diff, "\n",  new FileOutputStream("/home/gmamakis/data_migration sets/final/discrepancies_91637_diff"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
