package eu.europeana.enrichment.converter;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Conversion progress report
 * @author Borys Omelayenko
 *
 */
public class ProgressReport
{
	Logger log = LoggerFactory.getLogger(getClass().getName());

	private StopWatch timer;
	private long passedDataObjects;
	private long nextStop;
	private long initalMemoryConsumption;

	private long lastStopPassedDataObjects;
	private long lastStopPassedXMLValues;
	
	public ProgressReport()
	throws Exception
	{
		super();
		timer = new StopWatch();
		timer.start();
		nextStop = 100;
		initalMemoryConsumption = 0;//Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		lastStopPassedDataObjects = 0;
		lastStopPassedXMLValues = 0;
	}
	
	private long assignNextStop() {
		final long[] thresholds = {	1000000, 100000, 10000, 1000 };
		for (long threshold : thresholds) {
			if (nextStop >= threshold) return threshold;
		}
		return 250;			
	}
	
	public void process(String currentId, long incrementStep, String signatureOfThingInProgress)
	{
		passedDataObjects ++;
		lastStopPassedDataObjects ++;
		lastStopPassedXMLValues += incrementStep;
		
		if (passedDataObjects >= nextStop)
		{
			nextStop += assignNextStop();			
			long memoryComsumption = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - initalMemoryConsumption;
			log.info(
					String.format("Progress of %s: %d records in %d sec, using %d Mb RAM (%d B/record), sliding av. record length %d lines, now at id: %s ",
							signatureOfThingInProgress,
							passedDataObjects,
							timer.getTime() / 1000,
							memoryComsumption / (1024 * 1024),
							memoryComsumption / passedDataObjects,
							lastStopPassedXMLValues / lastStopPassedDataObjects,
							currentId));
			lastStopPassedDataObjects = 0;
			lastStopPassedXMLValues = 0;
		}
	}
	
}
