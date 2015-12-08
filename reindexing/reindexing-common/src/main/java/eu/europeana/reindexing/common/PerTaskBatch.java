package eu.europeana.reindexing.common;

import com.google.code.morphia.annotations.Indexed;

import java.util.List;

/**
 * Created by ymamakis on 12/8/15.
 */
public class PerTaskBatch {

    @Indexed
    private String taskId;
    @Indexed
    private Long batchId;
    private List<String> recordIds;

    public PerTaskBatch(String taskId, Long batchId, List<String> recordsIds){
        this.taskId = taskId;
        this.batchId = batchId;
        this.recordIds = recordsIds;
    }
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
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
