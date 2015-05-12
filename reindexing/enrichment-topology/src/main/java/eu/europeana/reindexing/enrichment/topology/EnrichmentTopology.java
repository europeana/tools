/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.enrichment.topology;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import eu.europeana.reindexing.enrichment.EnrichmentBolt;
import eu.europeana.reindexing.recordread.ReadSpout;
import eu.europeana.reindexing.recordwrite.RecordWriteBolt;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymamakis
 */
public class EnrichmentTopology {

    public StormTopology buildTopology() {

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("readSpout", new ReadSpout(null, null, null), 1);
        builder.setBolt("enrichment", new EnrichmentBolt(null, null), 10);
        builder.setBolt("saverecords", new RecordWriteBolt(null, null, null, null), 1);
        return builder.createTopology();
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, 2000);

        StormTopology topology = new EnrichmentTopology().buildTopology();
        //MORE TODO HERE
        try {
            StormSubmitter.submitTopology("enrichment", config, topology);
        } catch (AlreadyAliveException ex) {
            Logger.getLogger(EnrichmentTopology.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidTopologyException ex) {
            Logger.getLogger(EnrichmentTopology.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
