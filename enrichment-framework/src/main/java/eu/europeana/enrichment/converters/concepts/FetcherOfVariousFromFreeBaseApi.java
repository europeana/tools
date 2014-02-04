package eu.europeana.enrichment.converters.concepts;

import eu.europeana.enrichment.tagger.vocabularies.VocabularyOfTerms;

public class FetcherOfVariousFromFreeBaseApi {

	protected VocabularyOfTerms vocabularyOfConcepts = new VocabularyOfTerms("freebase.selected.concepts", null){

        @Override
        public String onNormaliseLabel(String label, NormaliseCaller caller) throws Exception {
            return label;
        }

    };

	
	
}
