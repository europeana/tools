/* NewspapersMetadataRecordHandler.java - created on 11/06/2015, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.vocab.getty.aat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class GettyLabelLinkerForAat extends GettyLabelLinker {
    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(GettyLabelLinkerForAat.class.getName());

    /**
     * Creates a new instance of this class.
     * @throws IOException 
     */
    public GettyLabelLinkerForAat(File gettyLabelToIdMapCsvFile) throws IOException {
        super(gettyLabelToIdMapCsvFile);
    }

    

    public LinkResult linkLiteral(String literal) {
        LinkResult res=new LinkResult();
        literal = LabelMatchType.EXACT.normalize(literal);
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
                else if( ! isLabelWithQualifier(literal) ) {
//                else {
                    List<Pair<LabelMatchType, Integer>> matches = matchLiteralWithoutQualifier(labelSim);
                    if (!matches.isEmpty()) 
                        res.addUnqualifiedLabelMatches(matches);
                }
            }
        }
        return res;
        
    }

    /**
     * @param labelSim
     * @return
     */
    private List<Pair<LabelMatchType, Integer>> matchLiteral(String labelSim) {
        MapOfLists<String, Integer>[] allIndexes = new MapOfLists[] {caseSensitiveMap, caseInsensitiveMap, similarMap};
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
        List<Integer> vocabIds = index.get(LabelMatchType.UNQUALIFIED_LABEL.normalize(labelSim));
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