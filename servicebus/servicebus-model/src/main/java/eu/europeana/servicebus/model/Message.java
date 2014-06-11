/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.servicebus.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The message delivered in the service bus
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class Message implements Serializable {
    
    private Status status;
    private String jobId;

    /**
     * The status
     * @return 
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * The jobId
     * @return 
     */
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    
    /**
     * Byte serialization of a Message
     * @return
     * @throws IOException 
     */
    public byte[] getBytes() throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(this);
        }
         return bout.toByteArray();
    }
}
