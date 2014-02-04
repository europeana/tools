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

import org.apache.commons.lang.StringUtils;

import eu.europeana.enrichment.context.Environment;
import eu.europeana.enrichment.triple.Triple;
import eu.europeana.enrichment.utils.SesameWriter;
import eu.europeana.enrichment.xconverter.api.Graph;

public class RdfGraph extends AbstractFileWritingGraph
{
	private SesameWriter persistenceWriter;

	/**
	 * Target that corresponds to an output RDF file.
	 * 
	 * @param datasetId 
	 *          signature of a dataset (conversion task). null indicates that this graph is not a result of a conversion task.
	 * @param objectType
	 *          signature, typically the type (class) of objects stored in this
	 *          RDF.
	 * @param propertyType
	 *          signature, typically the type of properties stored in this RDF.
	 * @param comment
	 *          Descriptive text put into the RDF file header
	 */
	public RdfGraph(
			String datasetId,
			Environment environment,
			String datasetModifier,
			String objectType,
			String propertyType,
			String... comment)
	{
		super(datasetId, environment, datasetModifier, objectType, propertyType, "rdf", comment);
	}

	@Override
	public void startRdf() throws Exception
	{
		super.startRdf();

		persistenceWriter = SesameWriter.createRDFXMLWriter(getFinalFile(getVolume()), getEnvironment().getNamespaces(), getId(), StringUtils.join(getComments(), "\n"), 1024, 100 * 1024);
		persistenceWriter.startRDF();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Graph))
			return false;
		Graph tObj = (Graph) obj;
		return getId().equals(tObj.getId());
	}

	@Override
	public void writeTriple(Triple triple) throws Exception {
		persistenceWriter.handleTriple(triple);
	}

	@Override
	public void endRdf() throws Exception
	{
		if (writingHappened()) {
			persistenceWriter.handleComment("Triples all volumes (all triples if this is the last volume): " + size());
			persistenceWriter.handleComment("Properties: "
					+ getProperties().size()
					+ "\n"
					+ getProperties().toString().replaceAll(", http://", ",\n http://"));
			persistenceWriter.endRDF();
		}
	}
}
