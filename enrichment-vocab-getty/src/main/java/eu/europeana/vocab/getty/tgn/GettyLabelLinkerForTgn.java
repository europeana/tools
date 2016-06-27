package eu.europeana.vocab.getty.tgn;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import eu.europeana.util.data.MapOfLists;
import eu.europeana.util.data.Pair;
import eu.europeana.vocab.getty.GettyLabelLinker;
import eu.europeana.vocab.getty.LabelMatchType;
import eu.europeana.vocab.getty.GettyLabelLinker.LinkResult;

/**
 * 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/06/2015
 */
public class GettyLabelLinkerForTgn extends GettyLabelLinker {
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(GettyLabelLinkerForTgn.class.getName());
    private static Pattern cleanParentesis=Pattern.compile("[\\(\\<][^\\)\\>]+[\\)\\>]");  

    protected HashMap<Integer, String> parentLabels=new HashMap<Integer, String>();
    protected LabelMatchType updateRecordOnMatchAt=LabelMatchType.UNQUALIFIED_LABEL;
    /**
     * Creates a new instance of this class.
     * @throws IOException 
     */
    public GettyLabelLinkerForTgn(File gettyLabelToIdMapCsvFile) throws IOException {
        super("TGN");
//        super(gettyLabelToIdMapCsvFile);
        CSVParser parser = CSVParser.parse(gettyLabelToIdMapCsvFile, Charset.forName("UTF8"), CSVFormat.EXCEL.withDelimiter(';'));
        int cnt=0;
        for(CSVRecord rec: parser) {
            cnt++;
            if(cnt==1) {
                conceptSchemeId=rec.get(0);
                continue;
            }
            String label = rec.get(0);
            Integer vocabId = Integer.parseInt(rec.get(1));
            String labelParent= rec.get(2);
            label = LabelMatchType.EXACT.normalize(label);
            labelParent = LabelMatchType.SIMILARITY.normalize(labelParent);
            parentLabels.put(vocabId, labelParent);
//            System.out.println("caseSensitiveMap - "+label);

//            if (caseInsensitiveMap.containsKey(label) && !caseInsensitiveMap.get(label).equals(vocabId))
//                System.out.println("WARNING: ambiguous sensitive-insensitive case: "+label);
//            if (caseSensitiveMap.containsKey(label) && !caseSensitiveMap.get(label).equals(vocabId))
//                System.out.println("WARNING: ambiguous sensitive-sensitive case: "+label);
//            if (similarMap.containsKey(label) && !similarMap.get(label).equals(vocabId))
//                System.out.println("WARNING: ambiguous sensitive-similar case: "+label);
            
            caseSensitiveMap.put(label, vocabId);
            String labelLc=LabelMatchType.CASE_NORMALIZED.normalize(label);
            if(!labelLc.equals(label)) {
//                System.out.println("caseInsensitiveMap - "+labelLc);
//                if (caseInsensitiveMap.containsKey(labelLc) && !caseInsensitiveMap.get(labelLc).equals(vocabId))
//                    System.out.println("WARNING: ambiguous insensitive-insensitive case: "+labelLc);
//                if (caseSensitiveMap.containsKey(labelLc) && !caseSensitiveMap.get(labelLc).equals(vocabId))
//                    System.out.println("WARNING: ambiguous insensitive-sensitive case: "+labelLc);
//                if (similarMap.containsKey(labelLc) && !similarMap.get(labelLc).equals(vocabId))
//                    System.out.println("WARNING: ambiguous insensitive-similar case: "+labelLc);
                caseInsensitiveMap.put(labelLc, vocabId);
            }
            
            if(isLabelWithQualifier(labelLc)) {
                String unqualifiedLabel=LabelMatchType.UNQUALIFIED_LABEL.normalize(label);
//                if (unqualifiedMap.containsKey(unqualifiedLabel) && !unqualifiedMap.get(unqualifiedLabel).equals(vocabId))
//                    System.out.println("WARNING: ambiguous unqualified-unqualified case: "+unqualifiedLabel);
                unqualifiedMap.put(unqualifiedLabel, vocabId);
            }
            
            String labelSim = LabelMatchType.SIMILARITY.normalize(label);
            if(!labelSim.equals(label) && !labelSim.equals(labelLc)) {
//                System.out.println("similarMap - "+labelSim);
//                if (caseInsensitiveMap.containsKey(labelSim) && !caseInsensitiveMap.get(labelSim).equals(vocabId))
//                    System.out.println("WARNING: ambiguous similar-insensitive case: "+labelSim);
//                if (caseSensitiveMap.containsKey(labelSim) && !caseSensitiveMap.get(labelSim).equals(vocabId))
//                    System.out.println("WARNING: ambiguous similar-sensitive case: "+labelSim);
//                if (similarMap.containsKey(labelSim) && !similarMap.get(labelSim).equals(vocabId))
//                    System.out.println("WARNING: ambiguous similar-similar case: "+labelSim);
                
                similarMap.put(labelSim, vocabId);
            }
        }
        log.info("initialized Linker with "+cnt+ " mappings.");
        parser.close();    
    
    }
    

    public LinkResult linkLiteral(String literalParam) {
        String labelClean = cleanParentesis.matcher(literalParam).replaceAll("");
        String[][] labelsToMatch=new String[2][2];
        
        int strIdx = labelClean.lastIndexOf(',');
        if(strIdx>0) {
            labelsToMatch[0][0]=labelClean.substring(strIdx+1).trim();
            labelsToMatch[0][1]=labelClean.substring(0, strIdx).trim();
             strIdx = labelsToMatch[0][1].lastIndexOf(',');
             if(strIdx>0) {
                 labelsToMatch[1][0]=labelClean.substring(strIdx+1).trim();
                 labelsToMatch[1][1]=labelClean.substring(0, strIdx).trim();
             }
        }
        
        for(String[] litAndParent: labelsToMatch) {
            if(litAndParent[0]==null)
                continue;
            
            LinkResult res=new LinkResult();
            String literal = LabelMatchType.EXACT.normalize(litAndParent[0]);
            String literalParent = LabelMatchType.SIMILARITY.normalize(litAndParent[1]);
            
            List<Pair<LabelMatchType, Integer>> vocabId = matchLiteral(literal);
            if (!vocabId.isEmpty()) {
                res.addCaseSensitiveMatch(vocabId);
            } else {
                literal=LabelMatchType.CASE_NORMALIZED.normalize(literal);
                vocabId = matchLiteral(literal);
                if (vocabId!=null) {
                    res.addCaseInsensitiveMatch(vocabId);
                } else {
                    String labelSim=LabelMatchType.SIMILARITY.normalize(literal);
                    vocabId = matchLiteral(labelSim);
                    if (vocabId!=null) 
                        res.addSimilarityMatch(vocabId);
                }
            }
            
            //link parent
            if(res.isLinked()) {
                LinkResult parentLinkResults=new LinkResult();
                List<Integer> matches = res.getBestMatches();
                for(Integer conceptId: matches) {
                    String labelParent = parentLabels.get(conceptId);
                    if(labelParent==null)
                        continue;
                    if(matchParents(literalParent, labelParent)) 
                        parentLinkResults.addMatch(conceptId, res.getBestMatchType());
                }
            } else
                continue;
            if(res.isLinkedUnambigously(updateRecordOnMatchAt)) 
                return res;
        }        
        return new LinkResult();
    }

    /**
     * @param literalParent
     * @param labelParent
     * @return
     */
    private boolean matchParents(String literalParent, String labelParent) {
        String[] wordsA = literalParent.split(" ");
        String[] wordsb = labelParent.split(" ");
        if (wordsA.length!=wordsb.length)
            return false;
        OUTER: for (int iA = 0; iA < wordsA.length; iA++) {
            String wA = wordsA[iA];
            for (int iB = 0; iB < wordsb.length; iB++) {
                String wB = wordsb[iB];
                if (wB.equals(wA))
                    continue OUTER;
            }
            //a word was not found 
            return false;
        }
        return true;
    }


    /**
     * @param labelSim
     * @return
     */
    private List<Pair<LabelMatchType, Integer>> matchLiteral(String labelSim) {
        MapOfLists<String, Integer>[] allIndexes = new MapOfLists[] {caseInsensitiveMap, caseInsensitiveMap, similarMap};
        LabelMatchType[] allIndexesMatchTypes=new LabelMatchType[] {LabelMatchType.EXACT, LabelMatchType.CASE_NORMALIZED, LabelMatchType.SIMILARITY, LabelMatchType.UNQUALIFIED_LABEL}; 
        for(int i=0 ; i<allIndexes.length ; i++) {
            MapOfLists<String, Integer> index=allIndexes[i];
            List<Integer> vocabIds = index.get(labelSim);
            if (vocabIds!=null && !vocabIds.isEmpty()) {
                List<Pair<LabelMatchType, Integer>> ret=new ArrayList<Pair<LabelMatchType,Integer>>(vocabIds.size());
                for(Integer id: vocabIds) {
                    ret.add(new Pair<LabelMatchType, Integer>(allIndexesMatchTypes[i], id));
                }
                return ret;
            }
        }
        return Collections.emptyList();
    }
    /**
     * @param labelSim
     * @return
     */
    private List<Pair<LabelMatchType, Integer>> matchLiteralWithoutQualifier(String labelSim) {
        MapOfLists<String, Integer> index=unqualifiedMap;
        List<Integer> vocabIds = index.get(labelSim);
        if (vocabIds!=null && !vocabIds.isEmpty()) {
            List<Pair<LabelMatchType, Integer>> ret=new ArrayList<Pair<LabelMatchType,Integer>>(vocabIds.size());
            for(Integer id: vocabIds) {
                ret.add(new Pair<LabelMatchType, Integer>(LabelMatchType.UNQUALIFIED_LABEL, id));
            }
            return ret;
        }
        return Collections.emptyList();
    }




}