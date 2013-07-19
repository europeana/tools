/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.datamigration.ese2edm.image.watermark.enums;


/**
 * @author Georgios Markakis <gwarkx@hotmail.com>
 *
 * 11 Jul 2012
 */
public enum  EDMXMPValues {
	
	dc_title("dc:title"),// dc:title (with xml:lang attribute set to "x-default") from dc:title
	// dc:rights from edm:rights
	dc_rights("dc:rights"),
	// cc:attributionName from dc:creator
	cc_attributionName("cc:attributionName"),
	// dc:rights from edm:rights
	edm_rights("dc:rights"),
	// edm:dataProvider from europeana:dataProvider
	edm_dataProvider("edm:dataProvider"),
	// edm:provider from europeana:provider
	edm_provider("edm:provider"),
	// xmpRights:Marked from europeana:rights: "False" if europeana:rights
	// is http://creativecommons.org/publicdomain/mark/1.0/ or
	// http://creativecommons.org/publicdomain/zero/1.0/, "True" otherwise.
	xmpRights_Marked("xmpRights:Marked"),
	// xmpRights:WebStatement from europeana:isShownAt
	xmpRights_WebStatement("xmpRights:WebStatement"),
	// cc:morePermissions from europeana:isShownAt (as a value for the
	// rdf:resource attribute)
	cc_morePermissions("cc:morePermissions"),
	// xmpMM:OriginalDocumentID from europeana:object
	xmpMM_OriginalDocumentID("xmpMM:OriginalDocumentID"),
	// cc:useGuidelines with http://www.europeana.eu/rights/pd-usage-guide/
	// (as a value for the rdf:resource attribute) if europeana:rights is
	// http://creativecommons.org/publicdomain/mark/1.0/ or
	// http://creativecommons.org/publicdomain/zero/
	cc_useGuidelines("cc:useGuidelines");
	
	
	private String fieldId;
	
	EDMXMPValues(String id) {
		this.setFieldId(id);
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}


}
