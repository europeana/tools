package eu.europeana.enrichment.gui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;

public interface EnrichmentServiceAsync {
	
	void enrich(List<InputValueDTO> values, boolean toEdm, AsyncCallback<List<EntityWrapperDTO>> entities);
	
}
