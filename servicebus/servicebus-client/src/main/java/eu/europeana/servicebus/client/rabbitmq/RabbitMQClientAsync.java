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
import com.rabbitmq.client.Envelope;
import eu.europeana.servicebus.client.ESBClient;
import eu.europeana.servicebus.model.Message;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author gmamakis
 */
public class RabbitMQClientAsync extends AbstractRabbitMQClient implements ESBClient {

    public Consumer consumer;

    public RabbitMQClientAsync(String host, String incomingQueue, String outgoingQueue, String username, String password)
            throws IOException {
        this.host = host;
        this.incomingQueue = incomingQueue;
        this.outgoingQueue = outgoingQueue;
        this.username = username;
        this.password = password;
        consumer = new DefaultConsumer(receiveChannel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                try {
                    receive(body);
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }

        };
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

    public Message receive(byte[] body) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(body);
        return (Message) new ObjectInputStream(bin).readObject();
    }

}
