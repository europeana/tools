package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class MappingIssueFinder {

	public static final String REPOSITORY = "/export/repository";

	public final static String SIP_CREATOR = "createEuropeanaURI(";

	public final static String PRE_SIP_CREATOR = "(ID)";

	private final static String BEGIN_HASH_FUNCTION_RECORD = "createEuropeanaURI(input.record.";
	private final static String BEGIN_HASH_FUNCTION_NO_RECORD = "createEuropeanaURI(input.";
	private final static String END_HASH_FUNCTION = ")";
	private final static String IDENTIFIER = "(ID)";
	private final static String RECORD_SPLITTER = "=>";
	private final static String LINE_SPLITTER = "\n";

	private static List<String> normal = new ArrayList<String>() {
		{
			add("europeana_isShownAt");
			add("europeana_object");
			add("dc_identifier");
			add("europeana_fake");
			add("europeana_isShownBy");
		}
	};

	public static String getHashFieldPreSip(File file) {
		String inputString;
		try {
			inputString = FileUtils.readFileToString(file);
			if (inputString != null) {
				String[] lines = StringUtils.split(inputString, LINE_SPLITTER);
				for (String line : lines) {
					if (StringUtils.contains(line, IDENTIFIER)
							&& StringUtils.contains(line, RECORD_SPLITTER)) {
						return StringUtils.replace(
								StringUtils.substringBetween(line,
										RECORD_SPLITTER, IDENTIFIER).trim(),
								":", "_");
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String... args) {

		StringBuffer sb = new StringBuffer();

		for (File file : search("/export/repository")) {
			
			if (file.getAbsolutePath().contains("/mappings/")) {
				String hash = getHashFieldPreSip(file);
				int contains = 0;
				for (String compare : normal) {
					if (!StringUtils.contains(hash, compare)) {
						contains++;
					}
				}
				if (contains == 5) {
					sb.append(file.getName());
					sb.append("|");
					sb.append(hash);
					sb.append("\n");
				}
			} else {
				String hash = getHashFieldSipCreator(file);
				int contains = 0;
				for (String compare : normal) {
					if (!StringUtils.contains(hash, compare)) {
						contains++;
					}
				}
				if (contains == 5) {
					sb.append(file.getName());
					sb.append("|");
					sb.append(hash);
					sb.append("\n");
				}

			}
			

		}

		try {
			FileUtils.writeStringToFile(new File(
					"/home/gmamakis/Desktop/dataset_ids.csv"), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getHashFieldSipCreator(File file) {

		String inputString;
		try {
			inputString = FileUtils.readFileToString(file);
			if (inputString != null) {
				if (StringUtils.substringBetween(inputString,
						BEGIN_HASH_FUNCTION_RECORD, END_HASH_FUNCTION) == null) {
					return StringUtils.substringBetween(inputString,
							BEGIN_HASH_FUNCTION_NO_RECORD, END_HASH_FUNCTION);
				}
				return StringUtils.substringBetween(inputString,
						BEGIN_HASH_FUNCTION_RECORD, END_HASH_FUNCTION);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public static List<File> search(String root) {
		System.out.println(root);
		File folder = new File(root);
		File[] listOfFiles = folder.listFiles();

		List<File> files = new ArrayList<File>();
		for (File file : listOfFiles) {
			String path = file.getPath().replace('\\', '/');
			System.out.println(path);
			if (new File(path + "/mappings/").exists()) {
				files.addAll(Arrays.asList(new File(path + "/mappings/")
						.listFiles()));
			} else if (new File(path + "/input_source/").exists()) {
				files.addAll(Arrays.asList(new File(path + "/input_source/")
						.listFiles()));
			}
		}
		return files;
	}
}
