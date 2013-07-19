package eu.annocultor.tagger.preprocessors;

import junit.framework.TestCase;

import org.junit.Assert;

import eu.annocultor.tagger.preprocessors.EuropeanaTimeUtils;
import eu.annocultor.tagger.rules.PairOfStrings;

public class EuropeanaTimeUtilsTest  extends TestCase {

	public void testPeriodMarkedWithYears() throws Exception {
		Assert.assertEquals(new PairOfStrings("1777", "1824"), EuropeanaTimeUtils.splitToStartAndEnd("1777-1824"));
		Assert.assertEquals(new PairOfStrings("1777", "1824"), EuropeanaTimeUtils.splitToStartAndEnd("1777 -1824"));
	}

}