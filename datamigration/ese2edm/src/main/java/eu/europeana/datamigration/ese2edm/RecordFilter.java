package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class RecordFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			List<String> allRecords = IOUtils.readLines(new FileInputStream(
					new File(args[0])));
			List<String> fixedCollections = IOUtils
					.readLines(new FileInputStream(new File(args[1])));
			
			for (String collection : fixedCollections) {
				for (String record : allRecords) {
					if(record.startsWith("/"+collection+"/")){
						allRecords.remove(record);
					}
				}
			}
			IOUtils.writeLines(allRecords, "\n", new FileOutputStream(new File(args[2])));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
