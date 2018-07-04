package com.coder4.lmsia.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.ref.WeakReference;

/**
 * @author coder4
 */
public abstract class RabbitSender<T> implements DisposableBean {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private ObjectMapper jsonObjectMapper;

    private RabbitClient rabbitClient;

    private RabbitResendThread<T> resendThread;

    private Channel senderChannel;

    private void init() {

        jsonObjectMapper = new ObjectMapper();

        try {
            senderChannel = rabbitClient.createChannel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        startResendThread();
    }

    public void stop() {

        // retry thread
        resendThread.joinAndStop();

        // Close all resources
        RabbitClient.closeChannel(senderChannel);
        rabbitClient.stop();
    }

    private Channel createChannel() throws Exception {
        return rabbitClient.createChannel();
    }

    private void startResendThread() {
        resendThread = new RabbitResendThread<>(new RabbitResendThreadDelegate<T>() {

            private WeakReference<RabbitSender> weakSender = new WeakReference<>(RabbitSender.this);

            @Override
            public Channel createChannel() throws Exception {
                return weakSender.get().createChannel();
            }

            @Override
            public void doSend(Channel retryChannel, T msg) throws Exception {
                weakSender.get().doSend(retryChannel, msg);
            }
        });
        resendThread.start();
    }

    public void send(T msg) {
        try {
            doSend(senderChannel, msg);
        } catch (Exception e) {
            LOG.error("RabbitSender send exception, will resend", e);
            resendThread.resendLater(msg);
        }
    }

    private void doSend(Channel channel, T msg) throws Exception {
        byte[] payload = serialize(msg);
        channel.basicPublish(
                getExchangeName(),
                getRoutingKey(msg),
                false,
                false,
                MessageProperties.MINIMAL_PERSISTENT_BASIC,
                payload);

        LOG.info("RabbitSender success send a msg.");
    }

    protected byte[] serialize(T msg) {
        try {
            return jsonObjectMapper.writeValueAsBytes(msg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String getExchangeName();

    protected String getRoutingKey(T msg) {
        // Default
        return "#";
    }

    @Autowired
    public void setRabbitClient(RabbitClient rabbitClient) {
        this.rabbitClient = rabbitClient;
        // after got rabbit client, init it
        init();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }
}