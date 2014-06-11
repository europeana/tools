package eu.europeana.servicebus.client;
import eu.europeana.servicebus.model.Message;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gmamakis
 */
public interface ESBClient {
    
    void send(Message message);

}
