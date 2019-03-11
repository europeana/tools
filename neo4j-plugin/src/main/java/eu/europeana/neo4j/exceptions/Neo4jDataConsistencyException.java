package eu.europeana.neo4j.exceptions;

/**
 * im, Lúthien, hain echant na 18/01/2017
 */
public class Neo4jDataConsistencyException  extends Exception {

    private String rdfAbout;

    public Neo4jDataConsistencyException(String message, String rdfAbout) {
        super(message);
        this.rdfAbout = rdfAbout;
    }

    public String getRdfAbout() {
        return rdfAbout;
    }

}
