
/*
 * This is an automatically generated converter to be executed with AnnoCultor,
 * an RDF conversion tool, http://AnnoCultor.eu
 *
 */
import eu.annocultor.xconverter.api.GeneratedConverterInt;

import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import java.io.*;
import java.text.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;

import eu.annocultor.tagger.vocabularies.*;
import eu.annocultor.tagger.terms.*;
import eu.annocultor.tagger.preprocessors.*;
import eu.annocultor.tagger.postprocessors.*;
import eu.annocultor.tagger.rules.*;
import eu.annocultor.rules.*;
import eu.annocultor.path.*;
import eu.annocultor.triple.*;
import eu.annocultor.tagger.rules.*;
import eu.annocultor.xconverter.api.*;
import eu.annocultor.xconverter.impl.*;
import eu.annocultor.data.sources.*;
import eu.annocultor.data.destinations.*;
import eu.annocultor.data.filters.*;
import eu.annocultor.common.*;

import eu.annocultor.api.*;
import eu.annocultor.context.*;

import org.apache.commons.lang.*;
import org.apache.commons.io.*;
import org.apache.commons.io.input.*;
import org.apache.commons.codec.digest.*;

/**
 * Converter for project
 * id: ak
 * institution: Multimedian e-Culture project
 * publisherId: 000
 *
 **/
public class GeneratedConverter
implements GeneratedConverterInt {

  Environment environment = new EnvironmentImpl() {

    @Override
    public void init() {
      // environment defined in XML
      setParameter(
        Environment.PARAMETERS.ANNOCULTOR_VOCABULARY_DIR,
        "vocabularies");
      setParameter(
        Environment.PARAMETERS.ANNOCULTOR_TMP_DIR,
        "tmp");
      setParameter(
        Environment.PARAMETERS.ANNOCULTOR_INPUT_DIR,
        "input_source");
      setParameter(
        Environment.PARAMETERS.ANNOCULTOR_OUTPUT_DIR,
        "output_rdf");
      setParameter(
        Environment.PARAMETERS.ANNOCULTOR_DOC_DIR,
        "doc");
    }
  };

  XConverterFactory factory = new XConverterFactory(environment) {
  
    @Override
    public void init() throws Exception {
      // Namespaces defined in XML
      addNamespace("dc", "http://purl.org/dc/elements/1.1/");
      addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
      addNamespace("inm", "http://www.inmagic.com/webpublisher/query");
      addNamespace("geo", "http://www.geonames.org/ontology#");
      addNamespace("vra", "http://www.vraweb.org/vracore/vracore3#");
      addNamespace("ac", "http://annocultor.eu/converter/");
      addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
      addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

      // Vocabulary AK-thesauri
      addVocabularyOfTerms("local",
        new VocabularyOfTerms("local",
      null
     ) {

        @Override
        public void init() throws Exception {
          // loading terms
          loadTermsSPARQL(
            "SELECT ?code ?label \n            WHERE \n            {\n              ?code <http://www.w3.org/2004/02/skos/core#prefLabel> ?label . \n              FILTER (REGEX(?label, \"(.+)\"))\n            }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "thesauri.rdf"
          );

          // loading term properties



      }

      
   }); // vocabulary VocabularyOfTerms
      // Vocabulary general terminology based on AAT
      addVocabularyOfTerms("terms",
        new VocabularyOfTerms("terms",
      null
     ) {

        @Override
        public void init() throws Exception {
          // loading terms
          loadTermsSPARQL(
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \t\n\tSELECT ?code ?label \n\tWHERE {\n      { ?code skos:prefLabel ?label }  \n    UNION \n      { ?code skos:altLabel ?label } \n\t}",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "/../../vocabularies/terms/*.rdf"
            , "/../../vocabularies/terms/*.rdfs"
          );

          // loading term properties



              }

          
      @Override
      public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
        /* Custom code from XML */
        //    	  if (caller == NormaliseCaller.query)
//    	    System.out.println(">terms query>" + label);
    		return label.toLowerCase();
        /* End of custom code from XML */
      }

        
   }); // vocabulary VocabularyOfTerms
      // Vocabulary directory of people based on ULAN
      addVocabularyOfPeople("people",
        new VocabularyOfPeople("people",
      null
     ) {

        @Override
        public void init() throws Exception {
          // loading terms
          loadTermsSPARQL(
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \t\n  SELECT ?code ?label  \n  WHERE { \n    { ?code skos:prefLabel ?label }  \n  UNION \n    { ?code skos:altLabel ?label } \n  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/people/*.rdf"
          );



          // loading term properties

          loadTermPropertiesSPARQL("birth",
            "PREFIX ulan: <http://e-culture.multimedian.nl/ns/getty/ulan#> 	"
 + "  SELECT ?code ?birth "
 + "  WHERE { "
 + "    ?code ulan:birthDate ?birth "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/people/*.rdf"
          );
          loadTermPropertiesSPARQL("death",
            "PREFIX ulan: <http://e-culture.multimedian.nl/ns/getty/ulan#> 	"
 + "  SELECT ?code ?death "
 + "  WHERE { "
 + "    ?code ulan:birthDate ?death "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/people/*.rdf"
          );


              }

          
      @Override
      public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
        /* Custom code from XML */
        //   	  if (caller == NormaliseCaller.query)
 //   	    System.out.println(">people query>" + label);
      	    
    		return  super.onNormaliseLabel(label.toLowerCase(), caller);
        /* End of custom code from XML */
      }

        
   }); // vocabulary VocabularyOfPeople
      // Vocabulary geographical vocabulary based on Geonames
      addVocabularyOfPlaces("places",
        new VocabularyOfPlaces("places",
      null
     ) {

        @Override
        public void init() throws Exception {
          // loading terms
          loadTermsSPARQL(
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \t\n  SELECT ?code ?label  \n  WHERE { \n    { ?code skos:prefLabel ?label }  \n  UNION \n    { ?code skos:altLabel ?label } \n  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/places/EU/*.rdf"
            , "../../vocabularies/places/AS/ID.rdf"
          );






          // loading term properties

          loadTermPropertiesSPARQL("population",
            "PREFIX places: <http://www.europeana.eu/resolve/ontology/> 	"
 + "  SELECT ?code ?population "
 + "  WHERE { "
 + "    ?code places:population ?population "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/places/EU/*.rdf"
            , "../../vocabularies/places/AS/ID.rdf"
          );
          loadTermPropertiesSPARQL("division",
            "PREFIX places: <http://www.europeana.eu/resolve/ontology/> 	"
 + "  SELECT ?code ?division "
 + "  WHERE { "
 + "    ?code places:division ?division "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/places/EU/*.rdf"
            , "../../vocabularies/places/AS/ID.rdf"
          );
          loadTermPropertiesSPARQL("latitude",
            "PREFIX places: <http://www.europeana.eu/resolve/ontology/> 	"
 + "  SELECT ?code ?latitude "
 + "  WHERE { "
 + "    ?code places:latitude ?latitude "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/places/EU/*.rdf"
            , "../../vocabularies/places/AS/ID.rdf"
          );
          loadTermPropertiesSPARQL("longitude",
            "PREFIX places: <http://www.europeana.eu/resolve/ontology/> 	"
 + "  SELECT ?code ?longitude "
 + "  WHERE { "
 + "    ?code places:longitude ?longitude "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/places/EU/*.rdf"
            , "../../vocabularies/places/AS/ID.rdf"
          );
          loadTermPropertiesSPARQL("country",
            "PREFIX places: <http://www.europeana.eu/resolve/ontology/> 	"
 + "  SELECT ?code ?country "
 + "  WHERE { "
 + "    ?code places:country ?country "
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/places/EU/*.rdf"
            , "../../vocabularies/places/AS/ID.rdf"
          );


              }

          
      @Override
      public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
        /* Custom code from XML */
        //  	  if (caller == NormaliseCaller.query)
  //  	    System.out.println(">places query>" + label);
    		return label.toLowerCase();
        /* End of custom code from XML */
      }

        
   }); // vocabulary VocabularyOfPlaces
      // Vocabulary AnnoCultor time ontology
      addVocabularyOfTime("time",
        new VocabularyOfTime("time",
      null
     ) {

        @Override
        public void init() throws Exception {
          // loading terms
          loadTermsSPARQL(
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \t\n  SELECT ?code ?label  \n  WHERE { \n    { ?code skos:prefLabel ?label }  \n  UNION \n    { ?code skos:altLabel ?label } \n  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/time/*.rdf"
          );



          // loading term properties

          loadTermPropertiesSPARQL("begin",
            "PREFIX time: <http://annocultor.eu/time/> 	"
 + "  SELECT ?code ?endpoint "
 + "  WHERE { "
 + "    ?code time:beginDate ?endpoint"
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/time/*.rdf"
          );
          loadTermPropertiesSPARQL("end",
            "PREFIX time: <http://annocultor.eu/time/> 	"
 + "  SELECT ?code ?endpoint "
 + "  WHERE { "
 + "    ?code time:endDate ?endpoint"
 + "  }",
            environment.getTmpDir(),
            environment.getVocabularyDir()
            , "../../vocabularies/time/*.rdf"
          );


      }

      
   }); // vocabulary VocabularyOfTime
  } // init of XMLAPIFactory
}; // XMLAPIFactory

// one converter class per source file
private class ak
extends CustomConverter
implements GeneratedCustomConverterInt {

  Logger log = LoggerFactory.getLogger(ak .class.getName());

  // conversion task: a container for things that belong to this file and conversion result
  Task task = Factory.makeTask(
    "ak",
    "ak",
    "Multimedian e-Culture project",
    Namespaces.ANNOCULTOR_CONVERTER,
    environment);

  ObjectRule defaultObjectRule ; // should be coming from the rdf:about attribute of an ObjectRule

    /*
     * Destinations
     */
    Graph Works; 
    Graph Links; 

  public ak() throws Exception {
    
    super();

    /*
     * Destinations
     */
    Works = 
    new RdfGraph ( task.getDatasetId(), task.getEnvironment(), "Works", "", "", 
      // ac:comment
        factory.makeString("Museum objects, one RDF resource per object")
      )
    ;
    task.addGraph(Works);
    factory.addGraph("Works", Works);
    Links = 
    new RdfGraph ( task.getDatasetId(), task.getEnvironment(), "Links", "", "", 
      // ac:comment
        factory.makeString("Links from museum objects to terms from external vocabularies")
      )
    ;
    task.addGraph(Links);
    factory.addGraph("Links", Links);

   /*
    * Object extraction rule
    */
   defaultObjectRule =
      ObjectRuleImpl.makeObjectRule(task,
        factory.makePath("/records/object"),
        factory.makePath("/records/object/id"),
        factory.makePath("Name"),
        null,
        true);


    /*
     * Property conversion rules
     */
    defaultObjectRule .addRule(
    new eu.annocultor.rules.RenameLiteralPropertyRule ( 
      // ac:srcPath
        factory.makePath("Name")
      // ac:dstProperty
      , factory.makeProperty("dc:title")
      // ac:dstGraph
      , factory.makeGraph("Works")
      )
    );
    defaultObjectRule .addRule(
    new eu.annocultor.rules.RenameLiteralPropertyRule ( 
      // ac:srcPath
        factory.makePath("id")
      // ac:dstProperty
      , factory.makeProperty("dc:identifier")
      // ac:dstGraph
      , factory.makeGraph("Works")
      )
    );
    defaultObjectRule .addRule(
    new eu.annocultor.rules.BatchRule ( 
            new eu.annocultor.rules.RenameLiteralPropertyRule ( 
      // ac:dstGraph
        factory.makeGraph("Works")
      )
      // ac:dstNamespace
      , factory.makeNamespace("dc")
      // ac:srcPath
      , factory.makePath("Description")
      // ac:srcPath
      , factory.makePath("ExtendedDescription")
      // ac:srcPath
      , factory.makePath("TimePeriod")
      // ac:srcPath
      , factory.makePath("Style")
      // ac:srcPath
      , factory.makePath("Organisation")
      // ac:srcPath
      , factory.makePath("Size")
      // ac:srcPath
      , factory.makePath("Image")
      )
    );
    defaultObjectRule .addRule(
    new eu.annocultor.rules.BatchRule ( 
            new eu.annocultor.rules.RenameLiteralPropertyRule ( 
      // ac:dstGraph
        factory.makeGraph("Works")
      )
      // ac:map
      , factory.makeMap(       
       factory.makePath("TextSigns"),
       factory.makeProperty("vra:inscription"))
      // ac:map
      , factory.makeMap(       
       factory.makePath("MaterialDescription"),
       factory.makeProperty("vra:material.note"))
      // ac:map
      , factory.makeMap(       
       factory.makePath("CulturalOrigin"),
       factory.makeProperty("vra:culture"))
      )
    );
    defaultObjectRule .addRule(
    new eu.annocultor.rules.BatchRule ( 
            new eu.annocultor.tagger.rules.LookupTermRule ( 
      // ac:srcPath
        factory.makePath("Material")
      // ac:dstProperty
      , factory.makeProperty("dc:material")
      // ac:dstGraphLiterals
      , factory.makeGraph("Links")
      // ac:dstGraphLinks
      , factory.makeGraph("Links")
      // ac:termsProperty
      , factory.makeProperty("dc:material")
      // ac:termsSignature
      , factory.makeString("material")
      // ac:termsSplitPattern
      , factory.makeString("( *; *)|( *, *)")
      // ac:termsVocabulary
      , factory.makeVocabularyOfTerms("local")
      )
      // ac:map
      , factory.makeMap(       
       factory.makePath("Material"),
       factory.makeProperty("dc:material"))
      // ac:map
      , factory.makeMap(       
       factory.makePath("Keyword"),
       factory.makeProperty("dc:subject"))
      )
    );
    defaultObjectRule .addRule(
    new eu.annocultor.tagger.rules.LookupPlaceRule ( 
      // ac:srcPath
        factory.makePath("GeographicalOrigin")
      // ac:dstProperty
      , factory.makeProperty("vra:location.creationSite")
      // ac:dstGraphLiterals
      , factory.makeGraph("Links")
      // ac:dstGraphLinks
      , factory.makeGraph("Links")
      // ac:termsProperty
      , factory.makeProperty("vra:location.creationSite")
      // ac:termsSignature
      , factory.makeString("creationSite")
      // ac:termsSplitPattern
      , factory.makeString("( *; *)|( *, *)")
      // ac:termsVocabulary
      , factory.makeVocabularyOfPlaces("places")
      )
    );
  }

  PrintWriter console;
  
  // execution point
  @Override
  public int run(PrintWriter out) throws Exception {
    this.console = out;
    selectDataSource();
    onConversionStarts();
    out.println("Starting generated converter");
    out.flush();
    int result;
    
    try {
      result = super.run(
        task,
        null
        , -1
      );
    } catch (Exception e) {
      console.flush();
      throw new Exception("Exception running generated converter", e);
    }
    
    onConversionEnds();
    return result;
  }

  @Override
  public void selectDataSource() throws Exception {

    DataSource dataSource = 
    new XmlDataSource ( environment,
      // ac:file
        factory.makeString("ak.xml")
      )
    ;
    task.setDataSource(dataSource);
  }



  @Override
  public void onConversionStarts() throws Exception {

  }

  @Override
  public void onConversionEnds() throws Exception {
   }

} // class ak

  // umbrella converter
  public int run(PrintWriter out) throws Exception {
  
    out.println("Running AnnoCultor build " + environment.getBuildSignature());
    int result = 0;
      ak c = new ak();
      if (c == null) {
        throw new Exception("failed to create a ak");
      }
      result = c.run(out);
      if (result != 0) {
        throw new Exception("Converter 'ak' failed. Execution of other converters from this profile is terminated");
      }
    return result;
  }

  public static void main(String args[]) throws Exception {
    GeneratedConverter converter = new GeneratedConverter();
    converter.run(new PrintWriter(System.out));
  }

}