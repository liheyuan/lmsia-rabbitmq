package com.coder4.lmsia.rabbitmq;

import com.rabbitmq.client.Channel;

/**
 * @author coder4
 */
interface RabbitResendThreadDelegate<T> {

    Channel createChannel() throws Exception;

    void doSend(Channel retryChannel, T msg) throws Exception;

}