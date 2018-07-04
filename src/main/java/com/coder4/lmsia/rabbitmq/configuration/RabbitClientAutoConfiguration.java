package com.coder4.lmsia.rabbitmq.configuration;

import com.coder4.lmsia.rabbitmq.RabbitClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author coder4
 */
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitClientAutoConfiguration {

    private String host;

    private int port;

    private String username;

    private String password;

    private String vhost;

    @Bean
    public RabbitClient createRabbitClient() {
        RabbitClient rabbitClient = new RabbitClient();
        rabbitClient.setHost(host);
        rabbitClient.setPort(port);
        rabbitClient.setUsername(username);
        rabbitClient.setPassword(password);
        rabbitClient.setVhost(vhost);
        rabbitClient.init();
        return rabbitClient;
    }

}