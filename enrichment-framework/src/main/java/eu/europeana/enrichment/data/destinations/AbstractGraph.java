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
package eu.europeana.enrichment.data.destinations;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.enrichment.xconverter.api.Graph;

public abstract class AbstractGraph implements Graph
{
	Logger log = LoggerFactory.getLogger(getClass().getName());

	public AbstractGraph(String graphId, String... comments) {
		super();
		this.graphId = graphId;
		for (String comment : comments) {
			this.comments.add(comment);
		}
	}

	/* May be wrapped into another graph */
	private Graph realGraph = null;

	@Override
	public Graph getRealGraph()
	{
		return realGraph;
	}

	@Override
	public void setRealGraph(Graph realGraph)
	{
		this.realGraph = realGraph;
	}

	/* May be stored in volumes of limited size */
	private int volume = 1;

	@Override
	public int getVolume() {
		return volume;
	}

	protected void incVolume() {
		volume ++;
	}
	
	/* Graph has an id (name) */
	private String graphId;

	@Override
	public String getId()
	{
		return graphId;
	}

	@Override
	public int hashCode()
	{
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Graph))
			return false;
		Graph tObj = (Graph) obj;
		return getId().equals(tObj.getId());
	}

	/* graph may have a comments that will be placed in the file */
	private List<String> comments = new ArrayList<String>();

	@Override
	public List<String> getComments()
	{
		return comments;
	}

	/* A listener on adding a triple */
	private NamedGraphAddListener addListener;

	@Override
	public void addNamedGraphAddListener(NamedGraphAddListener addListener)
	{
		this.addListener = addListener;
	}

	protected NamedGraphAddListener getListener() {
		return this.addListener;
	}
}
