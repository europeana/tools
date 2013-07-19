/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.annocultor.reports;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import eu.annocultor.api.ConverterTester;
import eu.annocultor.api.ObjectRule;
import eu.annocultor.api.Reporter;
import eu.annocultor.api.Rule;
import eu.annocultor.api.Task;
import eu.annocultor.context.Environment;
import eu.annocultor.context.Environment.PARAMETERS;
import eu.annocultor.path.Path;
import eu.annocultor.reports.parts.CounterMap;
import eu.annocultor.tagger.rules.VocabularyMatchResult;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.Triple;
import eu.annocultor.xconverter.api.DataObject;
import eu.annocultor.xconverter.api.PropertyRule;

/**
 * Reporter: a store for named counters and messages.
 * 
 * @author Borys Omelayenko
 * 
 */
public class ReporterImpl extends AbstractReporter implements Reporter
{

	private Environment env;

	// categorized
	private Map<String, Counter> counters = new HashMap<String, Counter>();

	private boolean sortCounters = true;

	public void setSortCounters(boolean sortCounters)
	{
		this.sortCounters = sortCounters;
	}

	/**
	 * Increments a named counter.
	 * 
	 * @see #inc
	 * @param counterName
	 */
	public void incTotals(Rule rule, final String categoryName, final String counterName) throws Exception
	{
		inc(rule, categoryName, "", "", counterName, 1);
	}

	public static class TermCounter implements Comparable<TermCounter>
	{
		private String term;
		private long count;

		public TermCounter(String term, long count)
		{
			super();
			this.term = term;
			this.count = count;
		}

		public String getTerm()
		{
			return term;
		}

		public long getCount()
		{
			return count;
		}

		public void increment(long offset)
		{
			count += offset;
		}

		@Override
		public String toString()
		{
			return term + " " + count;
		}

		public int compareTo(TermCounter o)
		{
			if (count < o.count)
				return -1;
			if (count > o.count)
				return 1;
			return 0;
		}
	}

	public static class Counter
	{
		public Rule getRule()
		{
			return rule;
		}

		public Path getSrcPath()
		{
			return srcPath;
		}

		private Path srcPath;
		private Property trgProperty;
		public Rule rule;
		public String categoryOrContextProperty;
		public String subCategoryOrVocabulary;
		public String counterName;
		Map<String, TermCounter> termCounters = new HashMap<String, TermCounter>();

		public Counter(
				Path srcPath,
				Property trgProperty,
				Rule rule,
				String categoryOrContextProperty,
				String subCategoryOrVocabulary,
				String counterName)
		{
			super();
			this.srcPath = srcPath;
			this.trgProperty = trgProperty;
			this.rule = rule;
			this.categoryOrContextProperty = categoryOrContextProperty;
			this.subCategoryOrVocabulary = subCategoryOrVocabulary;
			this.counterName = counterName;
		}

		@Override
		public String toString()
		{
			return mergeString(rule.getClass().getSimpleName(),
					categoryOrContextProperty,
					subCategoryOrVocabulary,
					counterName);
		}

		public static String mergeString(
				String writer,
				String categoryOrContextProperty,
				String subCategoryOrVocabulary,
				String counterName)
		{
			return writer + "#" + categoryOrContextProperty + "#" + subCategoryOrVocabulary + "#" + counterName;
		}

		@Override
		public int hashCode()
		{
			return toString().hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			return toString().equals(obj);
		}

		/**
		 * Returns the sorted list of counters and their names.
		 * 
		 * @return
		 */
		List<TermCounter> getSortedTermCounters(boolean sortCounters)
		{
			List<TermCounter> sorted = new LinkedList<TermCounter>();
			sorted.addAll(termCounters.values());
			if (sortCounters)
			{
				Collections.sort(sorted);
				Collections.reverse(sorted);
			}
			return sorted;
		}
	}

	/**
	 * Increments a named counter.
	 * 
	 * @see #inc(String)
	 * @param rule
	 *          rule we are counting for
	 * @param categoryOrContextProperty
	 *          counter category, typically the context property the rule is
	 *          applied to
	 * @param subCategoryOrVocabulary
	 *          counter subcategory
	 * @param counterName
	 *          name of the counter
	 * @param
	 * @param incrementOffset
	 */
	public void inc(
			Rule rule,
			String categoryOrContextProperty,
			String subCategoryOrVocabulary,
			String counterName,
			String term,
			long incrementOffset) 
	throws Exception
	{

		String counterKey =
			Counter.mergeString(rule.getClass().getSimpleName(),
					categoryOrContextProperty,
					subCategoryOrVocabulary,
					counterName);

		if (!counters.containsKey(counterKey))
			counters.put(counterKey, new Counter(rule.getSourcePath(),
					null,
					rule,
					categoryOrContextProperty,
					subCategoryOrVocabulary,
					counterName));

		Counter counterThisCategory = counters.get(counterKey);

		if (counterThisCategory.termCounters.containsKey(term))
		{
			counterThisCategory.termCounters.get(term).increment(incrementOffset);
		}
		else
		{
			counterThisCategory.termCounters.put(term, new TermCounter(term, incrementOffset));
		}
	}

	/**
	 * Prints all counters in format <code>counter, counterName</code>, sorted by
	 * counter.
	 * 
	 * @see #printSortedCounters(long)
	 * @return
	 */
	public String printSortedCounters()
	{
		return printSortedCounters(Long.MAX_VALUE);
	}

	/**
	 * Prints counters in format <code>counter, counterName</code>, sorted by
	 * counter.
	 * 
	 * @param itemsToShow
	 *          the number of top items to show
	 * @return
	 */
	public String printSortedCounters(long itemsToShow)
	{
		StringBuffer result = new StringBuffer();
		for (Iterator<String> iterator = counters.keySet().iterator(); iterator.hasNext();)
		{
			String counterKey = iterator.next();
			Counter counter = counters.get(counterKey);
			long values = counter.termCounters.size();
			if (values > 1)
			{
				result.append("-+ " + counter.categoryOrContextProperty + " (total " + values + " distinct)\n");
			}
			else
			{
				result.append("-- " + counter.categoryOrContextProperty + "");
			}

			List<TermCounter> sorted = counter.getSortedTermCounters(sortCounters);
			for (TermCounter term : sorted)
			{
				itemsToShow--;
				if (itemsToShow < 0)
					break;
				result.append(term + "\n");
			}
		}
		return result.toString();
	}

	/**
	 * Adds a message. Duplicating messages are ignored.
	 * 
	 * @param msg
	 * @throws IOException 
	 */
	public void log(String msg) throws IOException
	{
		messages.add(msg);
	}

	public String printTermOccurranceMatrix()
	{
		StringBuffer result = new StringBuffer();

		// getting all distinct terms to create a table header
		Set<String> allTerms = new HashSet<String>();
		Map<String, Long> diversityCount = new HashMap<String, Long>();
		Map<String, Long> totalsCount = new HashMap<String, Long>();

		for (Iterator<String> iterator = counters.keySet().iterator(); iterator.hasNext();)
		{
			String counterKey = iterator.next();
			Counter counter = counters.get(counterKey);
			allTerms.addAll(counter.termCounters.keySet());
		}

		// create header
		result.append("\nRule groups\tConversion rules / datasets\t");
		for (String term : allTerms)
		{
			result.append(term + "\t");
			diversityCount.put(term, 0L);
			totalsCount.put(term, 0L);
		}
		result.append("Total\tDependent datasets\n");

		long totalTotal = 0;
		long totalDepending = 0;

		// iterating distinct terms
		for (Iterator<String> iterator = counters.keySet().iterator(); iterator.hasNext();)
		{
			String counterKey = iterator.next();
			Counter counter = counters.get(counterKey);
			long dependingDatasets = counter.termCounters.size();
			long total = 0;
			String rule[] = counter.categoryOrContextProperty.split(", ext. ");
			result.append(rule[1] + "\t" + rule[0] + "\t");

			for (String term : allTerms)
			{
				Long termCnt = counter.termCounters.get(term).getCount();
				if (termCnt == null)
					result.append("\t");
				else
				{
					result.append(termCnt + "\t");
					total += termCnt;
					if (termCnt > 0)
						diversityCount.put(term, diversityCount.get(term) + 1);
					totalsCount.put(term, totalsCount.get(term) + termCnt);
				}
			}
			result.append(total + "\t" + dependingDatasets + "\n");
			totalTotal += total;
			totalDepending += dependingDatasets;
		}

		// totals
		result.append("\tTotals\t");
		for (String term : allTerms)
		{
			Long termCnt = totalsCount.get(term);
			if (termCnt == null)
				result.append("\t");
			else
				result.append(termCnt + "\t");
		}
		result.append(totalTotal + "\t\n");

		// diversity
		result.append("\tDiversity\t");
		for (String term : allTerms)
		{
			Long termCnt = diversityCount.get(term);
			if (termCnt == null)
				result.append("\t");
			else
				result.append(termCnt + "\t");
		}
		result.append("\t\n");
		return result.toString();
	}

	public ReporterImpl(String datasetId, Environment environment) throws Exception
	{
		super(environment.getDocDir(), datasetId);
		FileUtils.cleanDirectory(getReportDir());
		init();
		this.env = environment;
	}

	public void reportRuleInstantiation(Rule rule)
	{
		// TODO Auto-generated method stub

	}

	private CounterMap<Rule> invokedRulesCounter = new CounterMap<Rule>();

	public void reportRuleInvocation(Rule rule, Triple triple, DataObject dataObject) throws IOException
	{
		invokedRulesCounter.inc(rule, 1);
	}

	@Override
	public void reportLookupRuleInvocation(Rule rule, String label, String code, VocabularyMatchResult result) throws Exception
	{
		if (rule == null)
			throw new NullPointerException("Rule is null");
		if (rule.getSourcePath() == null)
			throw new NullPointerException("Rule.getsourcepath is null");
		if (rule.getSourcePath().getPath() == null)
			throw new NullPointerException("Rule source path value is null");
		if (label == null)
			throw new NullPointerException("value is null");

		AbstractReporter.Lookup lookupCounterKey = new AbstractReporter.Lookup(
				"dataset",
				"vocabulary",
				rule.getClass().getSimpleName(), 
				Path.formatPath(rule.getSourcePath(), env.getNamespaces()),
				result.getName(),
				label,
				code
		);

		lookupCounters.inc(lookupCounterKey);
	}

	private List<Task> tasks = new LinkedList<Task>();

	public void addTask(Task task)
	{
		this.tasks.add(task);
	}

	private void findAllChildRules(Rule parent, List<Rule> result)
	{
		for (PropertyRule rule : parent.getChildRules())
		{
			result.add(rule);
			findAllChildRules(rule, result);
		}
	}



	public void addTestReport(ConverterTester tester) throws Exception
	{
		if (tester == null)
		{
			log("No tester is implemented. Please, consider developing a few test cases.");
		}
		else
		{
			log(tester.loadDataAndTest());
		}
	}

	@Override
	public void report() throws Exception {

		// persistent reports
		for (Task task : tasks)	{

			// env
			for (PARAMETERS param : Environment.PARAMETERS.values()) {
				String value = task.getEnvironment().getParameter(param);
				environment.add(new KeyValuePair(param.name(), value));
			}

			List<Rule> rules = new LinkedList<Rule>();
			for (ObjectRule rule : task.getObjectRules())
			{
				// forgotten paths
				for (Map.Entry<Path, Integer> e : rule.getMissedPaths().entrySet())
				{
					List<Path> dataPaths = e.getKey().explicate();
					for (Path p : dataPaths) {
						forgottenPaths.inc(new Id(Path.formatPath(p, env.getNamespaces())), e.getValue());
					}
				}

				// rule invocation
				rules.add(rule);
				findAllChildRules(rule, rules);
			}

			// rule invocation
			for (Rule rule : rules)
			{
				if (invokedRulesCounter.containsKey(rule)) {
					invokedRules.inc(
							new RuleInvocation(
									rule.getAnalyticalRuleClass(),
									"<abbr class='namespaceAbbr' title='"
									+ (rule.getClass().getPackage() == null ? "" : rule.getClass().getPackage().getName())
									+ "'>"
									+ rule.getClass().getSimpleName()
									+ "</abbr>",
									Path.formatPath(rule.getSourcePath(), env.getNamespaces())),
									invokedRulesCounter.get(rule)
					);
				}
			}

		}		
		environment.add(new KeyValuePair("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));

		flush();
	}

}
