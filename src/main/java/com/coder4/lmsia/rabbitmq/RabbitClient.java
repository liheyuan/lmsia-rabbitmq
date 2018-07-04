package com.coder4.lmsia.rabbitmq;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author coder4
 */
public class RabbitClient {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private String host;

    private int port;

    private String username;

    private String password;

    private String vhost;

    private ConnectionFactory connectionFactory;

    private volatile Connection connection;

    public void init() {
        // Init Connection Factory
        setConnectionFactory(new ConnectionFactory());
        getConnectionFactory().setUsername(getUsername());
        getConnectionFactory().setPassword(getPassword());
        getConnectionFactory().setHost(getHost());
        getConnectionFactory().setPort(getPort());
        getConnectionFactory().setVirtualHost(getVhost());
        getConnectionFactory().setAutomaticRecoveryEnabled(true);

        // Connect
        connect();
    }

    public void stop() {
        try {
            if (getConnection() != null) {
                getConnection().close();
            }
            LOG.info("RabbitClient stopped");
        } catch (AlreadyClosedException e1) {
            LOG.info("rabbit client already closed.");
        } catch (Exception e) {
            LOG.warn("RabbitClient stop excepton", e);
        }
    }

    public Channel createChannel() throws IOException {
        // Make Channel
        Channel channel = getConnection().createChannel();
        channel.basicQos(1);
        return channel;
    }

    private void connect() {
        try {
            setConnection(getConnectionFactory().newConnection());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeChannel(Channel channel) {
        try {
            channel.close();
        } catch (Exception e) {

        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}