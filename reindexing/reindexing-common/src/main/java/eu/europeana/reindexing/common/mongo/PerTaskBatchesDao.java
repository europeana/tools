package eu.europeana.reindexing.common.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import eu.europeana.reindexing.common.PerTaskBatch;

import java.util.List;

/**
 * DAO for managing batches
 *
 * Created by ymamakis on 12/8/15.
 */
public class PerTaskBatchesDao {

    private Datastore datastore;

    public PerTaskBatchesDao(List<ServerAddress> addresses, String dbname, String... credentials) {
        Mongo client = new Mongo(addresses);
        Morphia morphia = new Morphia();
        morphia.map(PerTaskBatch.class);
        if (credentials.length>0) {
            datastore = morphia.createDatastore(client, dbname, credentials[0], credentials[1].toCharArray());
        } else {
            datastore = morphia.createDatastore(client, dbname);
        }
    }

    /**
     * Get a PerTaskBatch from taskID and batchId
     * @param taskId
     * @param batchId
     * @return
     */
    public PerTaskBatch findByTaskIdAndBatchId(Long taskId, Long batchId) {
        return getBatch(taskId, batchId).get();
    }


    private Query<PerTaskBatch> getBatch(Long taskId, Long batchId) {
        return datastore.find(PerTaskBatch.class).filter("taskId", taskId).filter("batchId", batchId);
    }


    /**
     * Get all the batches for all a task
     * @param taskId
     * @return
     */
    public List<PerTaskBatch> findByTaskId(Long taskId) {
        return datastore.find(PerTaskBatch.class).filter("taskId", taskId).asList();
    }

    /**
     * Create a PerTaskBatch given the task id, the batches and the records
     * @param taskId
     * @param batchId
     * @param recordIds
     */
    public void createPerTaskBatch(Long taskId, Long batchId, List<String> recordIds) {
        datastore.save(new PerTaskBatch(taskId, batchId, recordIds));
    }

    private void updateOrDeletePerTaskBatch(Long taskId, Long batchId, List<String> recordIds) {
        if (recordIds == null || recordIds.size() == 0) {
            datastore.delete(datastore.find(PerTaskBatch.class).filter("taskId", taskId).filter("batchId", batchId));
            return;
        }
        UpdateOperations<PerTaskBatch> ops = datastore.createUpdateOperations(PerTaskBatch.class);
        Query<PerTaskBatch> query = getBatch(taskId, batchId);
        ops.set("recordIds",recordIds);
        datastore.update(query,ops);
    }

    public void removeBatch(Long taskId, Long batchId){
        datastore.delete(datastore.find(PerTaskBatch.class).filter("taskId", taskId).filter("batchId", batchId));
    }
    /**
     * Remove a record from the PerTaskBatch. If the resulting PerTaskBatch record ids is empty we remove
     * the PerTaskBatch or else we update
     *
     * @param taskId
     * @param batchId
     * @param recordId
     */
    public void removeRecordIdFromBatch(Long taskId, Long batchId, String recordId){
        PerTaskBatch batch = getBatch(taskId,batchId).get();
        if(batch!=null){
            List<String> recordIds = batch.getRecordIds();
            if(recordIds.contains(recordId)){
                recordIds.remove(recordId);
            }
            updateOrDeletePerTaskBatch(taskId,batchId,recordIds);
        }
    }

    /**
     * Delete by taskId
     * @param taskId
     */
    public void deleteByTaskId(Long taskId){
        datastore.delete(datastore.createQuery(PerTaskBatch.class).filter("taskId",taskId));
    }
}
