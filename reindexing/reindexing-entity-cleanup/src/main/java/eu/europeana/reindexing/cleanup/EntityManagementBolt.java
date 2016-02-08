package eu.europeana.reindexing.cleanup;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.reindexing.common.ReindexingFields;
import eu.europeana.reindexing.common.ReindexingTuple;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ymamakis on 2/8/16.
 */
public class EntityManagementBolt extends BaseRichBolt {

    private EdmMongoServerImpl mongoServer;
    private String[] mongoAddreses;
    private String dbName;
    private String user;
    private String password;
    private EntityAppender ea;

    public EntityManagementBolt(String[] prodMongoAddresses, String ingstDbName, String ingstDbUser, String ingstDbPassword){
       this.mongoAddreses = prodMongoAddresses;
        this.dbName = ingstDbName;
        this.user = ingstDbUser;
        this.password = ingstDbPassword;
    }

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        ea = new EntityAppender();
        List<ServerAddress> addressList = new ArrayList<>();
        for(String str:mongoAddreses){
            try {
                ServerAddress address = new ServerAddress(str,27017);
                addressList.add(address);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        MongoClient mongoClient = new MongoClient(addressList);
        try {
            mongoServer = new EdmMongoServerImpl(mongoClient,dbName,user,password);
            ea = new EntityAppender();
        } catch (MongoDBException e) {
            e.printStackTrace();
        }
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        ReindexingTuple rt = ReindexingTuple.fromTuple(tuple);
        try {
            FullBean fBean = mongoServer.getFullBean(rt.getIdentifier());
            if(fBean!=null) {
                ea.cleanFullBean((FullBeanImpl) fBean);
                ea.appendEntities((FullBeanImpl) fBean, rt.getEntityWrapper());

            collector.emit(new ReindexingTuple(rt.getTaskId(), rt.getBatchId(), rt.getIdentifier(), rt.getNumFound(), rt.getQuery(), null, EdmUtils.toEDM((FullBeanImpl)fBean,false)).toTuple());
            } else {
                collector.emit(new ReindexingTuple(rt.getTaskId(), rt.getBatchId(), rt.getIdentifier(), rt.getNumFound(), rt.getQuery(), null, null).toTuple());
            }

        } catch (MongoDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(ReindexingFields.TASKID, ReindexingFields.BATCHID,
                ReindexingFields.IDENTIFIER, ReindexingFields.NUMFOUND, ReindexingFields.QUERY,
                ReindexingFields.ENTITYWRAPPER,ReindexingFields.EDMXML));
    }
}
