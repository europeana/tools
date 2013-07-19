package eu.annocultor.rules;

import org.junit.Test;

import eu.annocultor.path.Path;
import eu.annocultor.triple.Property;

import junit.framework.TestCase;

public class ExpandIntervalRuleTest extends TestCase {

	@Test
	public void test() throws Exception {
		ExpandIntervalRule rule = new ExpandIntervalRule(
				new Path("dc:date"), 
				new Path("dc:date"), 
				new Path("dc:date"), 
				new Property("dc:date"), 
				null);
		assertEquals(664, rule.getYear("664"));
		assertEquals(664, rule.getYear("664-01-01"));
		assertEquals(-664, rule.getYear("-664-03-03"));
		assertEquals(-664, rule.getYear("-664"));
	}
}
