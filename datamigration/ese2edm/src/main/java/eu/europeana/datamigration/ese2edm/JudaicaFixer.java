package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class JudaicaFixer {

	private final static String EDM_AGENT_TEMPLATE = "<edm:Agent rdf:about=\"%s\">";
	private final static String EDM_AGENT_CLOSETAG = "</edm:Agent>";
	private final static String EDM_PLACE_TEMPLATE = "<edm:Place rdf:about=\"%s\">";
	private final static String EDM_PLACE_CLOSETAG = "</edm:Place>";
	private final static String SKOS_CONCEPT_TEMPLATE = "<skos:Concept rdf:about=\"%s\">";
	private final static String SKOS_CONCEPT_CLOSETAG = "</skos:Concept>";
	private final static String EDM_PROVIDEDCHO_TEMPLATE = "<edm:ProvidedCHO rdf:about=\"%s\">";
	private final static String EDM_PROVIDEDCHO_CLOSETAG = "</edm:ProvidedCHO>";
	private final static String ORE_AGGREGATION_TEMPLATE = "<ore:Aggregation rdf:about=\"%s\">";
	private final static String ORE_AGGREGATION_CLOSETAG = "</ore:Aggregation>";
	private final static String ITEM = ":item:";
	private final static String CONCEPT = ":concept:";
	private final static String AGGREGATION = ":aggregation:";
	private final static String AGENT = ":agent:";
	private final static String PLACE = ":place:";
	private final static String RDF_DESCRIPTION = "<rdf:Description";
	private final static String RDF_DESCRIPTION_END = "</rdf:Description>";
	private final static String RDF_TYPE = "<rdf:type";
	private final static String RDF_ABOUT = "rdf:about=\"";
	private final static String RDF_ABOUT_END = "\">";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			List<String> lines = FileUtils.readLines(new File("/home/gmamakis/Documents/judaica/judaica_new/Export_workbench/09302-1-newer.xml"));
			List<String> newLines = new ArrayList<String>();
			String endTag = "";
			for (String line : lines) {
				if (!line.isEmpty()) {
					if (line.startsWith(RDF_DESCRIPTION)) {
						if (line.contains(AGENT)) {
							newLines.add(String.format(EDM_AGENT_TEMPLATE,
									StringUtils.substringBetween(line,
											RDF_ABOUT, RDF_ABOUT_END)));
							endTag = EDM_AGENT_CLOSETAG;
						} else if (line.contains(ITEM)) {
							newLines.add(String.format(
									EDM_PROVIDEDCHO_TEMPLATE, StringUtils
											.substringBetween(line, RDF_ABOUT,
													RDF_ABOUT_END)));
							endTag = EDM_PROVIDEDCHO_CLOSETAG;
						} else if (line.contains(CONCEPT)) {
							newLines.add(String.format(SKOS_CONCEPT_TEMPLATE,
									StringUtils.substringBetween(line,
											RDF_ABOUT, RDF_ABOUT_END)));
							endTag = SKOS_CONCEPT_CLOSETAG;
						} else if (line.contains(AGGREGATION)) {
							newLines.add(String.format(
									ORE_AGGREGATION_TEMPLATE, StringUtils
											.substringBetween(line, RDF_ABOUT,
													RDF_ABOUT_END)));
							endTag = ORE_AGGREGATION_CLOSETAG;
						} else if (line.contains(PLACE)) {
							newLines.add(String.format(EDM_PLACE_TEMPLATE,
									StringUtils.substringBetween(line,
											RDF_ABOUT, RDF_ABOUT_END)));
							endTag = EDM_PLACE_CLOSETAG;
						}
					} else if (line.equalsIgnoreCase(RDF_DESCRIPTION_END)) {
						newLines.add(endTag);
					} else if (!line.startsWith(RDF_TYPE)) {
						newLines.add(line);
					}
					
				}
				
			}
			FileUtils.writeLines(new File("/home/gmamakis/Documents/judaica/judaica_new/Export_workbench/09302-1-newest.xml"), newLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
