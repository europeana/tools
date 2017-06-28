package eu.europeana.migration;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-28
 */
public interface LogMarker {
  Marker currentStateMarker = MarkerFactory.getMarker("CURRENT_STATE");
  Marker errorIdsMarker = MarkerFactory.getMarker("ERROR_IDS");
}
