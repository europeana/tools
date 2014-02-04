package eu.europeana.enrichment.tagger.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.common.Utils;

public class RubbishTermDetector
{

	private static abstract class PotentialProblemPredicate
	implements Predicate
	{
		Logger log = LoggerFactory.getLogger(getClass().getName());

		public PotentialProblemPredicate(String name)
		{
			super();
			this.name = name;
		}

		protected Set<String> occurrences = new HashSet<String>();
		protected final String name;
		protected boolean warned = false;

		protected final int threshold = 10;
		public void warn(String term)
		{
			if (!warned && evaluate(term))
			{
				occurrences.add(term);
				if (occurrences.size() > threshold)
				{
					log.warn("Potential problem '" + name + "' with terms that may cause OutOfMemory exception and more. Happened on \n" +
							Utils.show(occurrences, "\n"));
					log.warn("This problem has happened aready with " + threshold + " different terms and may happen with others, but no more warnings will be made: YOU ARE WARNED");
					warned = true;
				}
			}
		}
	}

	private static class BracketProblemPredicate extends PotentialProblemPredicate {

		public BracketProblemPredicate(String opening, String closing)
		{
			super("Missing " + opening + " xor " + closing);
			this.opening = opening;
			this.closing = closing;
		}

		private String opening; 
		private String closing; 

		@Override
		public boolean evaluate(Object arg)
		{
			return arg.toString().contains(opening) != arg.toString().contains(closing);
		}

	};

	protected PotentialProblemPredicate isLongTermProblem = new PotentialProblemPredicate("Long term (80)") {

		@Override
		public boolean evaluate(Object arg)
		{
			return arg.toString().length() > 80;
		}

	};


	protected PotentialProblemPredicate isManyWordsProblem = new PotentialProblemPredicate("Too many words (7)") {

		@Override
		public boolean evaluate(Object arg)
		{
			return StringUtils.countMatches(" ", arg.toString()) > 7;
		}

	};

	protected PotentialProblemPredicate isSemicolonProblem = new PotentialProblemPredicate("Contains ';'") {

		@Override
		public boolean evaluate(Object arg)
		{
			return arg.toString().contains(";");
		}

	};

	protected PotentialProblemPredicate isRoundBracketProblem = new BracketProblemPredicate("(", ")");

	protected PotentialProblemPredicate isAngleBracketProblem = new BracketProblemPredicate("<", ">");

	protected PotentialProblemPredicate isSquareBracketProblem = new BracketProblemPredicate("[", "]");

	protected PotentialProblemPredicate isCurlyBracketProblem = new BracketProblemPredicate("{", "}");

	protected List<PotentialProblemPredicate> predicates = new ArrayList<PotentialProblemPredicate>();
	{
		predicates.add(isLongTermProblem);
		predicates.add(isManyWordsProblem);
		predicates.add(isSemicolonProblem);
		predicates.add(isRoundBracketProblem);
		predicates.add(isAngleBracketProblem);
		predicates.add(isSquareBracketProblem);
		predicates.add(isCurlyBracketProblem);
	}

	public void warnOnStrangeTerms(String term)
	{
		// here we need 'give all problems' function that is missing in jakarta commons
		for (PotentialProblemPredicate predicate : predicates)
		{
			predicate.warn(term);
		}
	}

}
