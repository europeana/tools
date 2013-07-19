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
package eu.annocultor.objects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;

import eu.annocultor.api.Reporter;
import eu.annocultor.data.destinations.AbstractGraph;
import eu.annocultor.triple.Property;
import eu.annocultor.triple.Triple;


@Ignore
public class TestNamedGraph extends AbstractGraph
{

	TestNamedGraph() {
		// hide
		super(null);
	}

	@Override
	public int getVolume() {
		return 1;
	}

	List<Triple> triples = new ArrayList<Triple>();

	public void add(Triple triple) throws Exception
	{
		triples.add(triple);
	}

	public Triple getLastAddedTriple(int offset)
	{
		return triples.get(triples.size() - offset);
	}

	public void addNamedGraphAddListener(NamedGraphAddListener addListener)
	{

	}

	public List<Triple> getTriples()
	{
		return triples;
	}

	public File getWorkingFile() throws IOException
	{
		return null;
	}

	public Reporter getReporter()
	{
		return null;
	}

	public void startRdf() throws Exception
	{
		// TODO Auto-generated method stub

	}

	public void setFinalDir(File baseDir)
	{
		// TODO Auto-generated method stub

	}

	public long size()
	{
		return triples.size();
	}

	public File getFinalFile(int volume) throws IOException
	{
		return null; //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getReport(Property property) throws Exception
	{
		return null; //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean writingHappened() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public void endRdf() throws Exception
	{
		//
	}

	@Override
	public Set<Property> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
