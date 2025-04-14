package com.fbytes.llmka.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelsConfig {

    @Bean(name = "heraldChannel")
    public MessageChannel heraldChannel() {
        return new QueueChannel();
    }



//    @Bean
//    public MessageChannel errorChannel() {
//        return new DirectChannel();
//    }
}
