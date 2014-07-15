package eu.europeana.datamigration.ese2edm;

import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.tools.lookuptable.EuropeanaId;

public class MimoRedirectUpdater {

	public final static String PORTAL = "/portal/";
	public final static String RESOLVE = "/resolve/";

	public static void main(String[] args) {
		Morphia morphia = new Morphia();
		morphia.map(EuropeanaId.class);

		try {
			Datastore datastore = morphia.createDatastore(new Mongo(
					"127.0.0.1", 29017), "EuropeanaId");
			List<EuropeanaId> europeanaIds = datastore
					.createQuery(EuropeanaId.class).field("newId")
					.equal(Pattern.compile("^/09102/")).asList();
			for (EuropeanaId europeanaId : europeanaIds) {
				UpdateOperations<EuropeanaId> ops = datastore
						.createUpdateOperations(EuropeanaId.class);
				ops.set("oldId", StringUtils.replace(europeanaId.getOldId(),
						PORTAL, RESOLVE));
				Query<EuropeanaId> query = datastore
						.createQuery(EuropeanaId.class).field("newId")
						.equal(europeanaId.getNewId());
				datastore.update(query, ops);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
