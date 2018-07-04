package com.coder4.lmsia.rabbitmq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author coder4
 */
public class RabbitResendThread<T> extends Thread {

    private static final int RESEND_QUEUE_MAX_SIZE = 1024 * 1024;

    private static final int RETRY_CREATE_CHANNEL_SLEEP_MS = 1000;

    private RabbitResendThreadDelegate<T> delegate;

    private Channel retryChannel;

    protected LinkedBlockingDeque<T> retryQueue;

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public RabbitResendThread(RabbitResendThreadDelegate<T> delegate) {
        retryQueue = new LinkedBlockingDeque<>(RESEND_QUEUE_MAX_SIZE);
        this.delegate = delegate;
    }

    public void resendLater(T msg) {
        retryQueue.offer(msg);
    }

    public void joinAndStop() {
        // wait for empty
        while (!retryQueue.isEmpty()) {
            try {
                LOG.info("retryQueue is not empty, sleep.");
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        RabbitClient.closeChannel(retryChannel);

        try {
            interrupt();
            join();
        } catch (InterruptedException e) {

        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    if (retryChannel == null || !retryChannel.isOpen()) {
                        retryChannel = delegate.createChannel();
                    }
                } catch (Exception e) {
                    LOG.error("Create retryChannel failed.", e);
                    Thread.sleep(RETRY_CREATE_CHANNEL_SLEEP_MS);
                    continue;
                }

                T msg = null;
                try {
                    msg = retryQueue.take();
                    resend(msg);
                    LOG.info("RabbitSender resend success");
                } catch (Exception t) {
                    if (t instanceof InterruptedException) {
                        throw (InterruptedException) t;
                    }

                    LOG.error("RabbitSender resend exception", t);
                    if (msg != null) {
                        retryQueue.putLast(msg);
                    }
                    if (retryChannel != null) {
                        RabbitClient.closeChannel(retryChannel);
                        retryChannel = null;
                    }
                }
            }
        } catch (InterruptedException e) {
            // will exit
            LOG.info("RabbitSenderThread will exit");
        }
    }

    private void resend(T msg) throws Exception {
        delegate.doSend(retryChannel, msg);
    }
}