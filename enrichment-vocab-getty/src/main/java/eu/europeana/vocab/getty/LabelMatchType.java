/* LabelMatchType.java - created on 11/04/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.vocab.getty;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/04/2016
 */
public enum LabelMatchType {
    EXACT,
    CASE_NORMALIZED,
    SIMILARITY,
//    UNQUALIFIED_LITERAL;
    UNQUALIFIED_LABEL;
    

    private static final Pattern normalizeWhiteSpacePattern1st=Pattern.compile("\\s+");// to replace with " "
    private static final Pattern normalizeWhiteSpacePattern2nd=Pattern.compile("(\\p{Punct})(\\S)");// to replace with "\1 \2" 
    private static final Pattern normalizeWhiteSpacePattern3rd=Pattern.compile("(\\S)(\\p{Punct})");// to replace with "\1 \2"

    private static final Pattern normalizeToSimilarPattern1st=Pattern.compile("\\p{InCombiningDiacriticalMarks}+");// to replace with ""
    private static final Pattern normalizeToSimilarPattern2nd=Pattern.compile("[\\p{Punct}\\s]+");// to replace with " "

    
    
    public boolean isMatchBetterOrEqualThan(LabelMatchType m) {
        switch (m) {
        case EXACT:
            return this==m;                
        case CASE_NORMALIZED:
            return this==m || this==LabelMatchType.EXACT;                
        case SIMILARITY:
            return this==m || this==LabelMatchType.CASE_NORMALIZED || this==LabelMatchType.EXACT;                
        case UNQUALIFIED_LABEL:
            return true;
        default:
            throw new RuntimeException("Needs to be implemented");
        }
    }
    
    public String normalize(String literal) {
        switch (this) {
        case EXACT:
            return normalizeWhitespace(literal);                
        case CASE_NORMALIZED:
            return normalizeCase(normalizeWhitespace(literal));                
        case SIMILARITY:
            return normalizeToAscii(normalizeCase(normalizeWhitespace(literal)));                
        case UNQUALIFIED_LABEL:
          return normalizeCase(normalizeWhitespace( literal.substring(0, literal.indexOf('(')).trim()));
        default:
            throw new RuntimeException("Needs to be implemented");
        }
    }

    /**
     * @param labelLc
     * @return
     */
    protected String normalizeToAscii(String labelLc) {
        String labelSim=Normalizer.normalize(labelLc, Form.NFD);
        labelSim=normalizeToSimilarPattern1st.matcher(labelSim).replaceAll("");
        labelSim=normalizeToSimilarPattern2nd.matcher(labelSim).replaceAll(" ");
        return labelSim;
    }

    /**
     * @param label
     * @return
     */
    protected String normalizeWhitespace(String label) {
        label.trim();
        label=normalizeWhiteSpacePattern1st.matcher(label.trim()).replaceAll(" ");
        label=normalizeWhiteSpacePattern2nd.matcher(label).replaceAll("$1 $2");
        label=normalizeWhiteSpacePattern3rd.matcher(label.trim()).replaceAll("$1 $2");
        return label;
    }
    protected String normalizeCase(String label) {
        return label.toLowerCase();
    }
}
