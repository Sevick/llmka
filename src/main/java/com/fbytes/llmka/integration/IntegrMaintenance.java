package com.fbytes.llmka.integration;

import com.fbytes.llmka.model.appevent.AppEvent;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.ControlCenter.IControlCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class IntegrMaintenance {

    @Autowired
    IControlCenter controlCenter;

    @Bean(name = "appEventChannel")
    public MessageChannel appEventChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(AppEvent.class);
        return channel;
    }


    @Bean(name = "maintenanceChannel")
    public MessageChannel maintenanceChannel() {
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }

    @Bean
    public org.springframework.integration.dsl.IntegrationFlow maintenanceFlow() {
        return org.springframework.integration.dsl.IntegrationFlow.from("appEventChannel")
                .handle(msg -> {
                    controlCenter.processEvent((AppEvent) msg.getPayload());
                })
                .get();
    }
}
