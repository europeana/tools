/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.recordwrite.reporting;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;

/**
 *
 * @author ymamakis
 */
@Entity("TaskReport")
public class TaskReport {
    
    @Indexed
    private long taskId;
    private String query;
    private long processed;
    private long total;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getProcessed() {
        return processed;
    }

    public void setProcessed(long processed) {
        this.processed = processed;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
    
    
}
            
