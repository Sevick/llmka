package com.fbytes.llmka.integration;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.appevent.AppEvent;
import com.fbytes.llmka.service.Maintenance.ControlCenter.IControlCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

import java.util.Optional;

@Configuration
public class IntegrMaintenance {
    private static final Logger logger = Logger.getLogger(IntegrMaintenance.class);

    @Value("${llmka.herald.news_group_header}")
    private String newsGroupHeader;
    @Value("${llmka.maintenance.news_group}")
    private String maintenanceGroup;
    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    IControlCenter controlCenter;

    @Bean(name = "appEventChannel")
    public MessageChannel appEventChannel() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();
        channel.setDatatypes(AppEvent.class);
        return channel;
    }


    @Bean(name = "maintenanceChannel")
    public MessageChannel maintenanceChannel() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();
        channel.setDatatypes(NewsData.class);
        return channel;
    }


    @Bean
    public org.springframework.integration.dsl.IntegrationFlow maintenanceHeraldFlow() {
        return org.springframework.integration.dsl.IntegrationFlow.from("appEventChannel")
                .transform(AppEvent.class, appEvent ->
                    NewsData.builder()
                            .id(appEvent.getId())
                            .extID("AppEvent")
                            .title(appEvent.getEventType().toString())
                            .description(Optional.of(String.valueOf(appEvent.getInstance())))
                            .dataSourceName(appName)
                            .link("http://fbytes.com")
                            .build()
                )
                .enrichHeaders(h -> h
                        .headerFunction(newsGroupHeader, m -> maintenanceGroup)
                )
                .channel("heraldChannel_Q")
                .get();
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
