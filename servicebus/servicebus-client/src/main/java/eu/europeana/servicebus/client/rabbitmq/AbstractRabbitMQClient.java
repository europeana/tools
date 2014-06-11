/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.servicebus.client.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import eu.europeana.servicebus.model.Message;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author gmamakis
 */
public class AbstractRabbitMQClient {

    protected String host;
    protected String incomingQueue;
    protected String outgoingQueue;
    protected String username;
    protected String password;
    protected Channel receiveChannel;
    protected Channel sendChannel;

    protected Builder builder;
    protected Connection connection;

    protected void sendMessage(Message message, String correlationId) {
        builder.deliveryMode(2);
        HashMap<String, Object> heads = new HashMap<String, Object>();

        builder.headers(heads);
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .replyTo(incomingQueue)
                .build();

        try {
            sendChannel.basicPublish("", outgoingQueue,
                    properties,
                    message.getBytes());
        } catch (IOException e) {

        }
    }

    
}
