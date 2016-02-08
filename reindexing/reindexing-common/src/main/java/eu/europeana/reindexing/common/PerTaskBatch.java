package eu.europeana.reindexing.common;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by ymamakis on 12/8/15.
 */
public class PerTaskBatch {

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    @Id

    private ObjectId id;

    @Indexed(unique = true)
    private Long taskId;
    @Indexed
    private Long batchId;
    private List<String> recordIds;

    public PerTaskBatch(Long taskId, Long batchId, List<String> recordsIds){
        this.taskId = taskId;
        this.batchId = batchId;
        this.recordIds = recordsIds;
    }

    public PerTaskBatch(){

    }
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public List<String> getRecordIds() {
        return recordIds;
    }

    public void setRecordIds(List<String> recordIds) {
        this.recordIds = recordIds;
    }
}
