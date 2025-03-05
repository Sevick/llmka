package com.fbytes.llmka.integration;

import com.fbytes.llmka.model.EmbeddedData;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.datasource.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class ChannelsConfig {
    @Bean
    @Qualifier("datasourceChannel")
    public MessageChannel datasourceChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(DataSource.class);
        return channel;
    }

    @Bean
    public MessageChannel newDataChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }


    @Bean
    public MessageChannel embeddingChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean
    public MessageChannel embeddingChannelOut() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(EmbeddedData.class);
        return channel;
    }

    @Bean
    public MessageChannel newsDataCheckChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(EmbeddedData.class);
        return channel;
    }

    @Bean
    public MessageChannel newsDataCheckChannelOut() {
        return new DirectChannel();
    }

    @Bean
    @Qualifier("newsDataCheckChannelReject")
    public MessageChannel newsDataCheckChannelReject() {
        return new DirectChannel();
    }

    @Bean
    @Qualifier("heraldChannel")
    public MessageChannel heraldChannel() {
        return new QueueChannel();
    }

//    @Bean
//    public MessageChannel errorChannel() {
//        return new DirectChannel();
//    }
}
