/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.common;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import java.io.Serializable;
import org.bson.types.ObjectId;

/**
 *
 * @author ymamakis
 */
@Entity("TaskReport")
public class TaskReport implements Serializable {
    
    @Id
    private ObjectId id;
    
    @Indexed
    private long taskId;
    private String query;
    private long processed;
    private long total;
    private long dateCreated;
    private long dateUpdated;
    private String topology;
    private String queryMark;
    
    public String getQueryMark() {
		return queryMark;
	}

	public void setQueryMark(String queryMark) {
		this.queryMark = queryMark;
	}

	@Indexed
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    

    public long getTaskId() {
        return taskId;
    }

    
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getTopology() {
        return topology;
    }

    public void setTopology(String topology) {
        this.topology = topology;
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
            
