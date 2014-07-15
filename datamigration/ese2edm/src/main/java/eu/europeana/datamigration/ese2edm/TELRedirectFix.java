package eu.europeana.datamigration.ese2edm;

import java.net.UnknownHostException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdMongoServerImpl;
import eu.europeana.corelib.utils.EuropeanaUriUtils;

public class TELRedirectFix {

    private final static String EUROPEANA_ID = "europeana_id";
    private static HttpSolrServer solrOld;
    private static EuropeanaIdMongoServer idServer;
    private final static String QUERY = "europeana_collectionName:9200274*";

    public static void main(String... args) {
        try {
            idServer = new EuropeanaIdMongoServerImpl(new Mongo("localhost",
                    27017), "EuropeanaId", "", "");
            solrOld = new HttpSolrServer("http://localhost:9999/solr/search");

//            int i = 0;
//            while (i < 1000) {
//                ModifiableSolrParams solrParams = new ModifiableSolrParams();
//                solrParams.set("q", QUERY);
//                solrParams.set("start", i);
//                solrParams.set("rows", 1000);
//                QueryResponse qr = solrOld.query(solrParams);
//                for (SolrDocument solrDoc : qr.getResults()) {

                    EuropeanaId id = new EuropeanaId();
//                    id.setOldId(solrDoc.getFieldValue(EUROPEANA_ID)
//                            .toString() + "_source");
//                    id.setNewId(solrDoc.getFieldValue(EUROPEANA_ID)
//                            .toString());
                    id.setOldId("/92037/_http___www_bl_uk_onlinegallery_onlineex_crace_t_007000000000001u00031000_html");
                    id.setNewId("/92037/_http___www_bl_uk_onlinegallery_onlineex_crace_t_zoomify87870_html");
                    idServer.saveEuropeanaId(id);

//                }
                //i += 1000;
//            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MongoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
//        catch (SolrServerException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

}
