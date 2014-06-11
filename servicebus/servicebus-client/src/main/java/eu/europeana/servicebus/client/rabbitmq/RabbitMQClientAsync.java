/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.servicebus.client.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import eu.europeana.servicebus.client.ESBClient;
import eu.europeana.servicebus.model.Message;
import java.io.IOException;

/**
 *
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class RabbitMQClientAsync extends AbstractRabbitMQClient implements ESBClient {

    private Consumer consumer;

    /**
     * RabbitMQ implementation of ESBClient
     *
     * @param host The host to connect to
     * @param incomingQueue The incoming queue
     * @param outgoingQueue The outgoing queue
     * @param username Username
     * @param password Password
     * @param consumer The consumer implementation - It can be null. It allows asynchronous consumers as well as enables
     * custom behaviour handling upon incoming message. The default message handling is and should be agnostic of the
     * method semantics to be implemented
     * @throws IOException
     */
    public RabbitMQClientAsync(String host, String incomingQueue, String outgoingQueue, String username, String password,
            Consumer consumer)
            throws IOException {
        this.host = host;
        this.incomingQueue = incomingQueue;
        this.outgoingQueue = outgoingQueue;
        this.username = username;
        this.password = password;
        if (consumer == null) {
            this.consumer = new DefaultConsumer(receiveChannel);
        } else {
            this.consumer = consumer;
        }
        ConnectionFactory factory = new ConnectionFactory();
        builder = new AMQP.BasicProperties.Builder();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = factory.newConnection();
        sendChannel = connection.createChannel();
        receiveChannel = connection.createChannel();
        sendChannel.queueDeclare(outgoingQueue, true, false, false, null);
        receiveChannel.queueDeclare(incomingQueue, true, false, false, null);
        receiveChannel.basicConsume(incomingQueue, true, consumer);

    }

    @Override
    public void send(Message message) {
        sendMessage(message, message.getJobId());
    }

}
