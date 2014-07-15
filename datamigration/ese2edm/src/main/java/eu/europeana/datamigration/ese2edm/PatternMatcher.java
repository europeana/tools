package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class PatternMatcher {
	public final static String HEADER = "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"><responseDate>2013-11-05T08:34:23Z</responseDate><request set=\"story\" metadataPrefix=\"oai_edm\" verb=\"ListRecords\">http://test.1418.eanadev.org/oai</request><ListRecords><record><header><identifier>oai:test.1418.eanadev.org/96</identifier><datestamp>2012-05-10T08:22:43Z</datestamp><setSpec>story</setSpec><setSpec>story:ugc</setSpec></header><metadata>";
	public final static String METADATA_RECORD = "</metadata></record><record><header>";
	public final static String IDENTIFIER = "<identifier>";
	public final static String METADATA = "<metadata>";
	public final static String IDENTIFIERMETADATA = "<identifier><metadata>";
	public final static String FOOTER = "</metadata></record><resumptionToken>oai_edm.s(story).f(2012-05-10T08:22:43Z).u(2013-03-28T10:08:46Z):298</resumptionToken></ListRecords></OAI-PMH>";
	public final static String RDF = "<rdf:RDF";
	public final static String RDF_CLOSE=">";
	public final static String RDF_END="</rdf:RDF>";
	public static void main(String... args) {
		try {
			String str = FileUtils.readFileToString(new File(
					"/home/gmamakis/Documents/judaica/oai-test/oai.xml"));
			// String newStr = StringUtils
			// .replace(
			// str,
			// "</rdf:RDF><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\" xmlns:ore=\"http://www.openarchives.org/ore/terms/\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">",
			// "");
			// newStr =
			// StringUtils.replace(newStr,"<repox:exportedRecords set=\"09309\" batchsize=\"-1\" total=\"378\" xmlns:repox=\"http://repox.ist.utl.pt\">"
			// , "");
			// newStr = StringUtils.replace(newStr, "</repox:exportedRecords>",
			// "");
			// String newStr =
			// StringUtils.replace(str,"file://C:/fakepath/09302-1-newer.xml","");
			String newStr = str;
			newStr = StringUtils.replace(newStr, HEADER, "");
			newStr = StringUtils.replace(newStr, FOOTER, "");
			newStr = StringUtils.replace(newStr, METADATA_RECORD, "");
			String[] bet = StringUtils.substringsBetween(str, IDENTIFIER,
					METADATA);
			for (String rem : bet) {
				newStr = StringUtils.replace(newStr, rem, "");
			}

			newStr = StringUtils.replace(newStr, IDENTIFIERMETADATA, "");
			String[]bet2 = StringUtils.substringsBetween(str, RDF, RDF_CLOSE);
			for(String rem2:bet2){
				newStr = StringUtils.replace(newStr, rem2, "");
			}
			newStr = StringUtils.replace(newStr,RDF+RDF_CLOSE,"");
			newStr = StringUtils.replace(newStr, RDF_END, "");
			FileUtils.writeStringToFile(new File(
					"/home/gmamakis/Documents/judaica/oai-test/oai-new-without.xml"),
					newStr);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
