package eu.annocultor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import eu.annocultor.common.Helper;

public class AllTests
{

	public static Test suite()
	{
		// create 
		TestSuite suite = new TestSuite("Core tests");
		//$JUnit-BEGIN$
		try
		{
			for (Class test : Helper.findAllClasses(TestCase.class,
					// objects should go first so that parent TestCase is resolved first
					"eu.annocultor.objects",
					"eu.annocultor.tests",
					"eu.annocultor.xconverter.api",
					"eu.annocultor.xconverter.impl",
					"eu.annocultor.api",
					"eu.annocultor.converter",
					"eu.annocultor.path",
					"eu.annocultor.rules",
					"eu.annocultor.data.sources"
					))
			{
				suite.addTestSuite(test);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		//$JUnit-END$
		return suite;
	}

}
