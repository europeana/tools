/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.reindexing.common;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import java.io.Serializable;

/**
 *
 * @author ymamakis
 */
public class ReindexingTuple implements Serializable {

    private long taskId;

    private long numFound;

    private String identifier;

    private String query;

    public ReindexingTuple(long taskId, String identifier, long numFound, String query) {
        this.identifier = identifier;
        this.numFound = numFound;
        this.query = query;
        this.taskId = taskId;
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

    public static ReindexingTuple fromTuple(Tuple tuple) {
        return new ReindexingTuple(tuple.getLongByField(ReindexingFields.TASKID), 
                tuple.getStringByField(ReindexingFields.IDENTIFIER), 
                tuple.getLongByField(ReindexingFields.NUMFOUND), 
                tuple.getStringByField(ReindexingFields.QUERY));
    }

    public Values toTuple() {
        return new Values(taskId, identifier, numFound, query);
    }
}
