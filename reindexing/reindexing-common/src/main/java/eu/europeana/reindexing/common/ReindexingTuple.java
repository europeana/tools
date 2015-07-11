/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.common;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ymamakis
 */
public class ReindexingTuple implements Serializable {

    private long taskId;

    private long numFound;

    private String identifier;

    private String query;
    
    private String entityWrapper;

    public ReindexingTuple(long taskId, String identifier, long numFound, String query, String entityWrapper) {
        this.identifier = identifier;
        this.numFound = numFound;
        this.query = query;
        this.taskId = taskId;
        this.entityWrapper = entityWrapper;
    }

    public long getNumFound() {
        return numFound;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getQuery() {
        return query;
    }

    public long getTaskId() {
        return taskId;
    }
    
    public String getEntityWrapper(){
        return entityWrapper;
    }

    public static ReindexingTuple fromTuple(Tuple tuple) {
        return new ReindexingTuple(tuple.getLongByField(ReindexingFields.TASKID), 
                tuple.getStringByField(ReindexingFields.IDENTIFIER), 
                tuple.getLongByField(ReindexingFields.NUMFOUND), 
                tuple.getStringByField(ReindexingFields.QUERY), tuple.getStringByField(ReindexingFields.ENTITYWRAPPER));
    }

    public Values toTuple() {
        return new Values(taskId, identifier, numFound, query, entityWrapper);
    }
}
