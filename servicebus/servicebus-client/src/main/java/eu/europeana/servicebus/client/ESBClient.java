package eu.europeana.servicebus.client;
import eu.europeana.servicebus.model.Message;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Interface for message delivery client
 * @author Yorgos.Mamakis@ europeanna.eu
 */
public interface ESBClient {
    
    /**
     * Send a custom message to the service bus
     * @param message 
     */
    void send(Message message);

}
