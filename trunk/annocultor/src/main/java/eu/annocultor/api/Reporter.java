package eu.annocultor.api;

import java.io.IOException;

import eu.annocultor.tagger.rules.VocabularyMatchResult;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;


/**
 * Collector of various counters and messages for reporting.
 * 
 * @author Borys Omelayenko
 * 
 */
public interface Reporter
{

	public static final String CATEGORY_CONVERTER = "Converter";

	/**
	 * 
	 * Should collected counters be sorted? May take a while.
	 */
	public abstract void setSortCounters(boolean sortCounters);

	/**
	 * Increments a named counter.
	 * 
	 * @param counterName
	 */
	public abstract void incTotals(Rule rule, final String categoryName, final String counterName)
			throws Exception;

	/**
	 * Increments a named counter.
	 * 
	 * @param rule
	 *          rule we are counting for
	 * @param categoryOrContextProperty
	 *          counter category, typically the context property the rule is
	 *          applied to
	 * @param subCategoryOrVocabulary
	 *          counter subcategory
	 * @param counterName
	 *          name of the counter
	 * @param term
	 *          term label
	 * @param incrementOffset
	 *          typically, 1.
	 */
	public abstract void inc(
			Rule rule,
			String categoryOrContextProperty,
			String subCategoryOrVocabulary,
			String counterName,
			String term,
			long incrementOffset) throws Exception;

	/**
	 * Adds a message. Duplicating messages are ignored.
	 * 
	 * @param msg
	 * @throws IOException 
	 */
	public abstract void log(String msg) throws IOException;

	public abstract void addTask(Task task);

	public abstract void addTestReport(ConverterTester tester) throws Exception;

	/**
	 * Analysis tool.
	 * 
	 */
	public abstract String printTermOccurranceMatrix();

	/**
	 * Generates a report.
	 * 
	 */
	public abstract void report() throws Exception;

	/**
   * 
   */
	void reportRuleInstantiation(Rule rule);

	void reportRuleInvocation(Rule rule, Triple triple, DataObject dataObject) throws IOException;

	public void reportLookupRuleInvocation(Rule rule, String label, String code, VocabularyMatchResult result) throws Exception;

}