/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.enrichment.service;

import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import java.util.List;

/**
 *
 * @author ymamakis
 */
public class EntityRemover {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 27017;

    public void remove(List<String> uris, String... args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args != null && args.length > 1) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        MongoDatabaseUtils.dbExists(host, port);
        
        MongoDatabaseUtils.delete(uris);
    }

}
