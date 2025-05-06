package com.fbytes.llmka.integration.config;

import com.fbytes.llmka.model.NewsData;
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

    @Bean(name = "newsCheckChannelReject")
    public MessageChannel newsDataCheckChannelReject() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }



//    @Bean
//    public MessageChannel errorChannel() {
//        return new DirectChannel();
//    }
}
