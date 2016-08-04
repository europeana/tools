/* NewspapersMetadataRecordHandler.java - created on 11/06/2015, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.vocab.getty;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import eu.europeana.util.data.MapOfLists;
import eu.europeana.util.data.Pair;
/**
 * 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/06/2015
 */
public abstract class GettyLabelLinker {
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(GettyLabelLinker.class.getName());

    protected MapOfLists<String, Integer> caseSensitiveMap=new MapOfLists<String, Integer>();
    protected MapOfLists<String, Integer> caseInsensitiveMap=new MapOfLists<String, Integer>();
    protected MapOfLists<String, Integer> similarMap=new MapOfLists<String, Integer>();
    protected MapOfLists<String, Integer> unqualifiedMap=new MapOfLists<String, Integer>();
    
    protected String conceptSchemeId;
    
    /**
     * Creates a new instance of this class.
     */
    public GettyLabelLinker(String conceptSchemeId) {
        this.conceptSchemeId=conceptSchemeId;
    }
    
    /**
     * Creates a new instance of this class.
     * @throws IOException 
     */
    public GettyLabelLinker(File gettyLabelToIdMapCsvFile) throws IOException {
        CSVParser parser = CSVParser.parse(gettyLabelToIdMapCsvFile, Charset.forName("UTF8"), CSVFormat.EXCEL.withDelimiter(';'));
        int cnt=0;
        for(CSVRecord rec: parser) {
            cnt++;
            if(cnt==1) {
                conceptSchemeId=rec.get(0);
                continue;
            }
            String label = rec.get(0);
            label = LabelMatchType.EXACT.normalize(label);
//            System.out.println("caseSensitiveMap - "+label);

            Integer vocabId = Integer.parseInt(rec.get(1));
            if (caseInsensitiveMap.containsKey(label) && !caseInsensitiveMap.get(label).equals(vocabId))
                System.out.println("WARNING: ambiguous sensitive-insensitive case: "+label);
            if (caseSensitiveMap.containsKey(label) && !caseSensitiveMap.get(label).equals(vocabId))
                System.out.println("WARNING: ambiguous sensitive-sensitive case: "+label);
            if (similarMap.containsKey(label) && !similarMap.get(label).equals(vocabId))
                System.out.println("WARNING: ambiguous sensitive-similar case: "+label);
            
            caseSensitiveMap.put(label, vocabId);
            String labelLc=LabelMatchType.CASE_NORMALIZED.normalize(label);
            if(!labelLc.equals(label)) {
//                System.out.println("caseInsensitiveMap - "+labelLc);
                if (caseInsensitiveMap.containsKey(labelLc) && !caseInsensitiveMap.get(labelLc).equals(vocabId))
                    System.out.println("WARNING: ambiguous insensitive-insensitive case: "+labelLc);
                if (caseSensitiveMap.containsKey(labelLc) && !caseSensitiveMap.get(labelLc).equals(vocabId))
                    System.out.println("WARNING: ambiguous insensitive-sensitive case: "+labelLc);
                if (similarMap.containsKey(labelLc) && !similarMap.get(labelLc).equals(vocabId))
                    System.out.println("WARNING: ambiguous insensitive-similar case: "+labelLc);
                caseInsensitiveMap.put(labelLc, vocabId);
            }
            
            if(isLabelWithQualifier(labelLc)) {
                String unqualifiedLabel=LabelMatchType.UNQUALIFIED_LABEL.normalize(label);
                if (unqualifiedMap.containsKey(unqualifiedLabel) && !unqualifiedMap.get(unqualifiedLabel).equals(vocabId))
                    System.out.println("WARNING: ambiguous unqualified-unqualified case: "+unqualifiedLabel);
                unqualifiedMap.put(unqualifiedLabel, vocabId);
            }
            
            String labelSim = LabelMatchType.SIMILARITY.normalize(label);
            if(!labelSim.equals(label) && !labelSim.equals(labelLc)) {
//                System.out.println("similarMap - "+labelSim);
                if (caseInsensitiveMap.containsKey(labelSim) && !caseInsensitiveMap.get(labelSim).equals(vocabId))
                    System.out.println("WARNING: ambiguous similar-insensitive case: "+labelSim);
                if (caseSensitiveMap.containsKey(labelSim) && !caseSensitiveMap.get(labelSim).equals(vocabId))
                    System.out.println("WARNING: ambiguous similar-sensitive case: "+labelSim);
                if (similarMap.containsKey(labelSim) && !similarMap.get(labelSim).equals(vocabId))
                    System.out.println("WARNING: ambiguous similar-similar case: "+labelSim);
                
                similarMap.put(labelSim, vocabId);
            }
        }
        log.info("initialized Linker with "+cnt+ " mappings.");
        parser.close();
    }

    
    protected static boolean isLabelWithQualifier(String label){
        return(label.endsWith(")") && label.contains("("));
    }


    abstract public LinkResult linkLiteral(String literal) ;

    public class LinkResult {
        
        List<Integer> caseSensitiveMatches;
        List<Integer> caseInsensitiveMatches;
        List<Integer> similarityMatches;
        List<Integer> unqualifiedLabelMatches;
//        List<Integer> unqualifiedLiteraMatches;
        
//        public void addUnqualifiedLiteralMatch(Integer aatId) {
//            getUnqualifiedLiteralMatches().add(aatId);
//        }
        
        public void addUnqualifiedLabelMatch(Pair<LabelMatchType, Integer> vocabId) {
            switch (vocabId.getV1()) {
            case EXACT:
//                getCaseSensitiveMatches().add(aatId.getV2());                
                break;
            case CASE_NORMALIZED:
//                getCaseInsensitiveMatches().add(aatId.getV2());
                break;
            case SIMILARITY:
//                getSimilarityMatches().add(aatId.getV2());
                break;
            case UNQUALIFIED_LABEL:
                getUnqualifiedLabelMatches().add(vocabId.getV2());
                break;
            default:
                break;
            }
        }

        /**
         * @param vocabId
         */
        public void addSimilarityMatch(List<Pair<LabelMatchType, Integer>> matches) {
            for (Pair<LabelMatchType, Integer> match : matches)
                addSimilarityMatch(match);
        }

        /**
         * @param vocabId
         */
        public void addCaseInsensitiveMatch(List<Pair<LabelMatchType, Integer>> matches) {
            for (Pair<LabelMatchType, Integer> match : matches)
                addCaseInsensitiveMatch(match);
        }

        /**
         * @param vocabId
         */
        public void addCaseSensitiveMatch(List<Pair<LabelMatchType, Integer>> matches) {
            for (Pair<LabelMatchType, Integer> match : matches)
                addCaseSensitiveMatch(match);
        }

        /**
         * @param matches
         */
        public void addUnqualifiedLabelMatches(List<Pair<LabelMatchType, Integer>> matches) {
            for (Pair<LabelMatchType, Integer> match : matches)
                addUnqualifiedLabelMatch(match);
        }

        public void addCaseSensitiveMatch(Pair<LabelMatchType,Integer> vocabId) {
            switch (vocabId.getV1()) {
            case EXACT:
                getCaseSensitiveMatches().add(vocabId.getV2());                
                break;
            case CASE_NORMALIZED:
                getCaseInsensitiveMatches().add(vocabId.getV2());
                break;
            case SIMILARITY:
                getSimilarityMatches().add(vocabId.getV2());
                break;
            case UNQUALIFIED_LABEL:
                getUnqualifiedLabelMatches().add(vocabId.getV2());
                break;
            default:
                break;
            }
        }
        
        public void addCaseInsensitiveMatch(Pair<LabelMatchType,Integer> vocabId) {
            switch (vocabId.getV1()) {
            case EXACT:
                getCaseInsensitiveMatches().add(vocabId.getV2());                
                break;
            case CASE_NORMALIZED:
                getCaseInsensitiveMatches().add(vocabId.getV2());
                break;
            case SIMILARITY:
                getSimilarityMatches().add(vocabId.getV2());
                break;
            case UNQUALIFIED_LABEL:
                getUnqualifiedLabelMatches().add(vocabId.getV2());
                break;
            default:
                break;
            }
        }
        
        public void addSimilarityMatch(Pair<LabelMatchType,Integer> vocabId) {
            switch (vocabId.getV1()) {
            case EXACT:
                getSimilarityMatches().add(vocabId.getV2());                
                break;
            case CASE_NORMALIZED:
                getSimilarityMatches().add(vocabId.getV2());
                break;
            case SIMILARITY:
                getSimilarityMatches().add(vocabId.getV2());
                break;
            case UNQUALIFIED_LABEL:
                //do not link these
//                getUnqualifiedLabelMatches().add(aatId.getV2());
                break;
            default:
                break;
            }
        }
        
//        public List<Integer> getUnqualifiedLiteralMatches() {
//            if(unqualifiedLiteraMatches==null)
//                unqualifiedLiteraMatches=new ArrayList<Integer>();
//            return unqualifiedLiteraMatches;
//        }
//        
        public List<Integer> getUnqualifiedLabelMatches() {
            if(unqualifiedLabelMatches==null)
                unqualifiedLabelMatches=new ArrayList<Integer>();
            return unqualifiedLabelMatches;
        }

        public List<Integer> getCaseSensitiveMatches() {
            if(caseSensitiveMatches==null)
                caseSensitiveMatches=new ArrayList<Integer>();
            return caseSensitiveMatches;
        }

        public List<Integer> getCaseInsensitiveMatches() {
            if(caseInsensitiveMatches==null)
                caseInsensitiveMatches=new ArrayList<Integer>();
            return caseInsensitiveMatches;
        }

        public List<Integer> getSimilarityMatches() {
            if(similarityMatches==null)
                similarityMatches=new ArrayList<Integer>();
            return similarityMatches;
        }

        /**
         * @return
         */
        public boolean isLinked() {
            return !(caseSensitiveMatches==null || caseSensitiveMatches.isEmpty())
                    || !(caseInsensitiveMatches==null || caseInsensitiveMatches.isEmpty())
                    || !(similarityMatches==null || similarityMatches.isEmpty())
                    || !(unqualifiedLabelMatches==null || unqualifiedLabelMatches.isEmpty());
        }
        
        public LabelMatchType getBestMatchType() {
            if (caseSensitiveMatches!=null && !caseSensitiveMatches.isEmpty())
                return LabelMatchType.EXACT;
            if (!(caseInsensitiveMatches==null || caseInsensitiveMatches.isEmpty()))
                return LabelMatchType.CASE_NORMALIZED;
            if (!(similarityMatches==null || similarityMatches.isEmpty()))
                return LabelMatchType.SIMILARITY;
            if( !(unqualifiedLabelMatches==null || unqualifiedLabelMatches.isEmpty()))
                return LabelMatchType.UNQUALIFIED_LABEL;
            return null;
        }

        /**
         * @return
         */
        public String getMatchedUri() {
            String prefix = conceptSchemeId.equals("AAT") ? "http://vocab.getty.edu/aat/" : "http://vocab.getty.edu/tgn/";
            if (caseSensitiveMatches!=null && !caseSensitiveMatches.isEmpty())
                return prefix+caseSensitiveMatches.get(0);
            if (!(caseInsensitiveMatches==null || caseInsensitiveMatches.isEmpty()))
                return prefix+caseInsensitiveMatches.get(0);
            if (!(similarityMatches==null || similarityMatches.isEmpty()))
                return prefix+similarityMatches.get(0);
            if( !(unqualifiedLabelMatches==null || unqualifiedLabelMatches.isEmpty()))
                return prefix+unqualifiedLabelMatches.get(0);
            return null;
        }

        /**
         * @return
         */
        public boolean isLinkedUnambigously(LabelMatchType minimumMatch) {
            if (caseSensitiveMatches!=null && !caseSensitiveMatches.isEmpty())
                return caseSensitiveMatches.size()==1;
            if (!(caseInsensitiveMatches==null || caseInsensitiveMatches.isEmpty()))
                return caseInsensitiveMatches.size()==1 && LabelMatchType.CASE_NORMALIZED.isMatchBetterOrEqualThan(minimumMatch);
            if (!(similarityMatches==null || similarityMatches.isEmpty()))
                return similarityMatches.size()==1 && LabelMatchType.SIMILARITY.isMatchBetterOrEqualThan(minimumMatch);               
            if( !(unqualifiedLabelMatches==null || unqualifiedLabelMatches.isEmpty()))
                return unqualifiedLabelMatches.size()==1 && LabelMatchType.UNQUALIFIED_LABEL.isMatchBetterOrEqualThan(minimumMatch);               
            return false;
        }

        /**
         * @return
         */
        public List<Integer> getBestMatches() {
            if (caseSensitiveMatches!=null && !caseSensitiveMatches.isEmpty())
                return caseSensitiveMatches;
            if (!(caseInsensitiveMatches==null || caseInsensitiveMatches.isEmpty()))
                return caseInsensitiveMatches;
            if (!(similarityMatches==null || similarityMatches.isEmpty()))
                return similarityMatches;
            if( !(unqualifiedLabelMatches==null || unqualifiedLabelMatches.isEmpty()))
                return unqualifiedLabelMatches;
            return null;        }

        /**
         * @param conceptId
         * @param bestMatchType
         */
        public void addMatch(Integer conceptId, LabelMatchType c) {
            switch (c) {
            case EXACT:
                getCaseSensitiveMatches().add(conceptId);                
                break;
            case CASE_NORMALIZED:
                getCaseInsensitiveMatches().add(conceptId);
                break;
            case SIMILARITY:
                getSimilarityMatches().add(conceptId);
                break;
            case UNQUALIFIED_LABEL:
                getSimilarityMatches().add(conceptId);
                //do not link these
//                getUnqualifiedLabelMatches().add(aatId.getV2());
                break;
            default:
                break;
            }
        }
        
    }
    
    /**
     * @return
     */
    public String getConceptSchemeId() {
         return conceptSchemeId;
    }
}