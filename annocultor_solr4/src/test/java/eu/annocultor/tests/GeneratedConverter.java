package eu.annocultor.tests;

/*
 * This is an automatically generated converter to be executed with AnnoCultor, 
 * a conversion tool from XML to RDF.
 *
 * Author: Borys Omelayenko
 * E-mail: omelayenko@yahoo.com
 * WWW: http://annocultor.sourceforge.net
 *  
 */
import java.io.PrintWriter;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.annocultor.api.CustomConverter;
import eu.annocultor.api.DataObjectPreprocessor;
import eu.annocultor.api.Factory;
import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Task;
import eu.annocultor.context.Environment;
import eu.annocultor.context.EnvironmentImpl;
import eu.annocultor.data.sources.XmlDataSource;
import eu.annocultor.rules.ObjectRuleImpl;
import eu.annocultor.tagger.vocabularies.VocabularyOfPeople;
import eu.annocultor.tagger.vocabularies.VocabularyOfPlaces;
import eu.annocultor.tagger.vocabularies.VocabularyOfTerms;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.GeneratedConverterInt;
import eu.annocultor.xconverter.api.GeneratedCustomConverterInt;
import eu.annocultor.xconverter.impl.XConverterFactory;

/**
 * Converter for project
 * id: Ese
 * institution: Europeana project
 * publisherId: 000
 *
 **/
@Ignore
public class GeneratedConverter 
implements GeneratedConverterInt
{
	Environment environment = new EnvironmentImpl()
	{
		@Override
		public void init()
		{
			// environment defined in XML
			setParameter(
					Environment.PARAMETERS.ANNOCULTOR_VOCABULARY_DIR, 
					"vocabularies");
			setParameter(
					Environment.PARAMETERS.ANNOCULTOR_TMP_DIR, 
					"tmp");
			setParameter(
					Environment.PARAMETERS.ANNOCULTOR_INPUT_DIR, 
					"input_source/");
			setParameter(
					Environment.PARAMETERS.ANNOCULTOR_OUTPUT_DIR, 
					"output_rdf");
			setParameter(
					Environment.PARAMETERS.ANNOCULTOR_DOC_DIR, 
					"doc");
		}
	};

	XConverterFactory factory = new XConverterFactory(environment)
	{
		@Override
		public void init() 	throws Exception
		{
			// Namespaces defined in XML
			addNamespace("dc", "http://purl.org/dc/elements/1.1/");
			addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			addNamespace("oldskos", "http://www.w3.org/2004/02/skos/core#");
			addNamespace("ac", "http://annocultor.eu/XConverter/");
			addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			addNamespace("ese", "http://www.europeana.eu/ese");
			addNamespace("europeana", "http://www.europeana.eu");
			addNamespace("skos", "http://www.w3.org/2008/05/skos#");
			addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			addNamespace("dcterms", "http://purl.org/dc/terms/");

			// Vocabulary general terminology based on AAT
			addVocabularyOfTerms("terms",
					new VocabularyOfTerms("terms", 
							null
					)
			{ 

				@Override
				public void init() throws Exception
				{
					// loading terms
					/*    loadTermsSPARQL(
       "SELECT ?code ?label \n\tWHERE {\n   {\n      ?code <http://www.w3.org/2008/05/skos#prefLabel> ?label\n   }  \n   UNION \n   {\n    \t?code <http://www.w3.org/2008/05/skos#altLabel> ?label\n   } \n\t}", 
       environment.getTmpDir(),
       environment.getVocabularyDir()
       , "/../../vocabularies/terms/*.rdf"
       , "/../../vocabularies/terms/*.rdfs"
       );

					 */					// loading term properties



				}


				@Override
				public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception
				{
					/* Custom code from XML */
					//    	  if (caller == NormaliseCaller.query)
					//    	    System.out.println(">terms query>" + label);
					return label;
					/* End of custom code from XML */
				}


			}); // vocabulary VocabularyOfTerms
			// Vocabulary directory of people based on ULAN
			addVocabularyOfPeople("people",
					new VocabularyOfPeople("people", 
							null
					)
			{ 

				@Override
				public void init() throws Exception
				{
					// loading terms
					/*    loadTermsSPARQL(
       "SELECT ?code ?label  \n  WHERE { \n   {\n      ?code <http://www.w3.org/2008/05/skos#prefLabel> ?label\n   }  \n   UNION \n   {\n    \t?code <http://www.w3.org/2008/05/skos#altLabel> ?label\n   } \n  }", 
       environment.getTmpDir(),
       environment.getVocabularyDir()
       , "../../vocabularies/people/*.rdf"
       );
					 */


					// loading term properties
					/*
    loadTermPropertiesSPARQL("birth",      
       "SELECT ?code ?birth "
 + "  WHERE { "
 + "    ?code <http://e-culture.multimedian.nl/ns/getty/ulan#birthDate> ?birth "
 + "  }", 
       environment.getTmpDir(),
       environment.getVocabularyDir()
       , "../../vocabularies/people/*.rdf" 
       );
    loadTermPropertiesSPARQL("death",      
       "SELECT ?code ?death "
 + "  WHERE { "
 + "     ?code <http://e-culture.multimedian.nl/ns/getty/ulan#birthDate> ?death "
 + "  }", 
       environment.getTmpDir(),
       environment.getVocabularyDir()
       , "../../vocabularies/people/*.rdf" 
       );

					 */
				}


				@Override
				public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception
				{
					/* Custom code from XML */
					//   	  if (caller == NormaliseCaller.query)
					//   	    System.out.println(">people query>" + label);

					return  super.onNormaliseLabel(label, caller);
					/* End of custom code from XML */
				}


			}); // vocabulary VocabularyOfPeople
			// Vocabulary geographical vocabulary based on Geonames
			addVocabularyOfPlaces("places",
					new VocabularyOfPlaces("places", 
							null
					)
			{ 

				@Override
				public void init() throws Exception
				{
					// loading terms
					/*
    loadTermsSPARQL(
       "SELECT ?code ?label  \n  WHERE { \n    {\n    \t?code <http://www.w3.org/2008/05/skos#prefLabel> ?label\n    }  \n    UNION \n    {\n    \t?code <http://www.w3.org/2008/05/skos#altLabel> ?label\n    } \n  }", 
       environment.getTmpDir(),
       environment.getVocabularyDir()
       , "../../vocabularies/places/*.rdf"
       );

					// loading term properties

					 */

				}


				@Override
				public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception
				{
					/* Custom code from XML */
					//  	  if (caller == NormaliseCaller.query)
					//  	    System.out.println(">places query>" + label);
					return label;
					/* End of custom code from XML */
				}


			}); // vocabulary VocabularyOfPlaces
		} // init of XMLAPIFactory
	} // XMLAPIFactory
	;

	// one converter class per source file
	private class Ese
	extends CustomConverter
	implements GeneratedCustomConverterInt
	{  

		Logger log = LoggerFactory.getLogger(Ese .class.getName());

		// conversion task: a container for things that belong to this file and conversion result
		Task task = Factory.makeTask(
				"Ese", 
				"Ese", 
				"Europeana project", 
				factory.makeNamespace("europeana"),
				environment);

		ObjectRule subjectRule ; // should be coming from the rdf:about attribute of an ObjectRule

		public Ese() throws Exception
		{
			super();

			/*
			 * Graphs
			 */
			factory.addGraph("People", Factory.makeGraph(task, "People", "" 
			));	  		
			factory.addGraph("Places", Factory.makeGraph(task, "Places", "" 
			));	  		
			factory.addGraph("PeopleLinks", Factory.makeGraph(task, "PeopleLinks", "" 
			));	  		
			factory.addGraph("PlacesLinks", Factory.makeGraph(task, "PlacesLinks", "" 
			));	  		

			/*
			 * Object extraction rule
			 */
			subjectRule =
				ObjectRuleImpl.makeObjectRule(task,
						factory.makePath("/metadata/record"),
						factory.makePath("/metadata/record/europeana:uri"),
						factory.makePath("/metadata/record/dc:identifier"),
						null,
						true);

			subjectRule .addPreprocessor(new DataObjectPreprocessor()
			{


				public boolean onPreCondition(DataObject sourceDataObject) throws Exception
				{
					/* Custom override from XML */
					// local identifier is a complete URI, it is already prefixed with a namespace
					//subjectRule.assumeQualifiedLocalRecordIdentifier();
					return true;      
				}

				@Override
				public boolean preCondition(DataObject sourceDataObject) throws Exception
				{
					return onPreCondition(sourceDataObject);
				}


			});

			/*
			 * Property conversion rules
			 */
			subjectRule .addRule(
					new eu.annocultor.rules.RenameLiteralPropertyRule ( 
							factory.makePath(
							"dc:creator1") // ac:srcPath
							, factory.makeProperty(
							"dc:creator2") // ac:dstProperty
							, factory.makeGraph(
							"People") // ac:dstGraph
					)
			);
			subjectRule .addRule(
					new eu.annocultor.rules.RenameLiteralPropertyRule ( 
							factory.makePath(
							"dc:contributor1") // ac:srcPath
							, factory.makeProperty(
							"dc:contributor") // ac:dstProperty
							, factory.makeGraph(
							"People") // ac:dstGraph
					)
			);
			subjectRule .addRule(
					new eu.annocultor.tagger.rules.LookupPersonRule ( 
							factory.makePath(
							"dc:creator") // ac:srcPath
							, factory.makeProperty(
							"dc:creator") // ac:dstProperty
							, factory.makeGraph(
							"People") // ac:dstGraphLiterals
							, factory.makeGraph(
							"PeopleLinks") // ac:dstGraphLinks
							, factory.makePath(
							"dates") // ac:birthPath
							, factory.makePath(
							"dates") // ac:deathPath
							, factory.makeProperty(
							"dc:creator") // ac:termsProperty
							, factory.makeString(
							"country") // ac:termsSignature
							, factory.makeString(
							"( *; *)|( *, *)") // ac:termsSplitPattern
							, factory.makeVocabularyOfPeople(
							"people") // ac:termsVocabulary
					)
			);
			subjectRule .addRule(
					new eu.annocultor.tagger.rules.LookupPersonRule ( 
							factory.makePath(
							"dc:contributor") // ac:srcPath
							, factory.makeProperty(
							"dc:contributor") // ac:dstProperty
							, factory.makeGraph(
							"People") // ac:dstGraphLiterals
							, factory.makeGraph(
							"PeopleLinks") // ac:dstGraphLinks
							, factory.makePath(
							"dates") // ac:birthPath
							, factory.makePath(
							"dates") // ac:deathPath
							, factory.makeProperty(
							"dc:contributor") // ac:termsProperty
							, factory.makeString(
							"contributor") // ac:termsSignature
							, factory.makeString(
							"( *; *)|( *, *)") // ac:termsSplitPattern
							, factory.makeVocabularyOfPeople(
							"people") // ac:termsVocabulary
					)
			);
			subjectRule .addRule(
					new eu.annocultor.rules.RenameLiteralPropertyRule ( 
							factory.makePath(
							"dc:country2") // ac:srcPath
							, factory.makeProperty(
							"dc:country") // ac:dstProperty
							, factory.makeGraph(
							"Places") // ac:dstGraph
					)
			);
			subjectRule .addRule(
					new eu.annocultor.rules.RenameLiteralPropertyRule ( 
							factory.makePath(
							"dcterms:spatial1") // ac:srcPath
							, factory.makeProperty(
							"dcterms:spatial") // ac:dstProperty
							, factory.makeGraph(
							"Places") // ac:dstGraph
					)
			);
			subjectRule .addRule(
					new eu.annocultor.tagger.rules.LookupPlaceRule ( 
							factory.makePath(
							"dc:country") // ac:srcPath
							, factory.makeProperty(
							"dc:country") // ac:dstProperty
							, factory.makeGraph(
							"Places") // ac:dstGraphLiterals
							, factory.makeGraph(
							"PlacesLinks") // ac:dstGraphLinks
							, factory.makeProperty(
							"dc:country") // ac:termsProperty
							, factory.makeString(
							"country") // ac:termsSignature
							, factory.makeString(
							"( *; *)|( *, *)") // ac:termsSplitPattern
							, factory.makeVocabularyOfPlaces(
							"places") // ac:termsVocabulary
					)
			);
			subjectRule .addRule(
					new eu.annocultor.tagger.rules.LookupPlaceRule ( 
							factory.makePath(
							"dcterms:spatial") // ac:srcPath
							, factory.makeProperty(
							"dcterms:spatial") // ac:dstProperty
							, factory.makeGraph(
							"Places") // ac:dstGraphLiterals
							, factory.makeGraph(
							"PlacesLinks") // ac:dstGraphLinks
							, factory.makeProperty(
							"dc:country") // ac:termsProperty
							, factory.makeString(
							"spatial") // ac:termsSignature
							, factory.makeString(
							"( *; *)|( *, *)") // ac:termsSplitPattern
							, factory.makeVocabularyOfPlaces(
							"places") // ac:termsVocabulary
					)
			);
		}  

		PrintWriter console;
		// execution point
		public int run(PrintWriter out) 
		throws Exception
		{
			this.console = out;
			selectDataSource(); 	  	
			onConversionStarts();
			out.println("Starting generated converter");
			out.flush();
			int result;
			try 
			{
				result = super.run(
						task, 
						null
						, 50
				);
			}
			catch (Exception e)
			{
				console.flush();
				throw new Exception("Exception running generated converter", e);
			}
			onConversionEnds();
			return result;
		}


		public void selectDataSource() 
		throws Exception
		{
			/* XML source */
			XmlDataSource source = new XmlDataSource(environment, "003*.xml");
			source.setMergeSourceFiles(true);
			task.setDataSource(source);
		}



		public void onConversionStarts() 
		throws Exception
		{

		}

		public void onConversionEnds() 
		throws Exception
		{
		}

	} // class Ese

	// umbrella converter
	public int run(PrintWriter out) throws Exception
	{
		int result = 0;
		Ese c = new Ese();
		if (c == null)
		{
			throw new Exception("failed to create a Ese");
		}
		result = c.run(out);
		if (result != 0)
		{
			throw new Exception("Converter 'Ese' failed. Execution of other converters from this profile is terminated"); 
		}
		return result;
	}

	public static void main(String args[]) 
	throws Exception
	{	  
		GeneratedConverter converter = new GeneratedConverter();	 
		converter.run(new PrintWriter(System.out));
	}

}