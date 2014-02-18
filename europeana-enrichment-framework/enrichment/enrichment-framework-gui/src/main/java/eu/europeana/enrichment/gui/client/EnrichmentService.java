package eu.europeana.enrichment.gui.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;

@RemoteServiceRelativePath("enrich")
public interface EnrichmentService extends RemoteService {

	List<EntityWrapperDTO> enrich(List<InputValueDTO> values, boolean toEdm);
}
