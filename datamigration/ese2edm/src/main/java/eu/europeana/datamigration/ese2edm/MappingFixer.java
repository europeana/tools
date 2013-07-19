package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class MappingFixer {
	private final static String TXT = "txt";
	private final static String MAPPING = "mapping";
	private final static String INPUT_SOURCE = "input_source";
	private final static String MAPPINGS = "mappings";

	public static void main(String... args) {
//		File[] files = new File(args[0]).listFiles();
		File[] files = new File("/home/gmamakis/Documents/sip_creator_mapping").listFiles();
		File root = new File("root_folder");
		root.mkdir();
		for (File file : files) {
			String[] nameAnalysis = file.getName().split("\\.");
			
			File folder = new File(root.getName()+"/"+StringUtils.substringBefore(nameAnalysis[0],"_mapping").trim());
			folder.mkdir();
			File input_source = null;
			if (nameAnalysis[nameAnalysis.length - 1].equals(TXT)) {
				input_source = new File(folder.getAbsolutePath() + "/"
						+ MAPPINGS);
			} else if (nameAnalysis[nameAnalysis.length - 1].equals(MAPPING)) {
				input_source = new File(folder.getAbsolutePath() + "/"
						+ INPUT_SOURCE);
			}
			if (input_source != null) {
				input_source.mkdir();
				try {
					FileUtils.copyFileToDirectory(file, input_source);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
