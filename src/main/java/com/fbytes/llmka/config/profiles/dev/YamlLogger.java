package com.fbytes.llmka.config.profiles.dev;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class YamlLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) event.getApplicationContext().getEnvironment();
        List<String> propertySources = environment.getPropertySources().stream()
                .filter(ps -> ps.getName().startsWith("Config resource"))
                .map(ps -> ps.getName())
                .toList();

        System.out.println("Loaded configuration files:");
        propertySources.forEach(System.out::println);
    }
}
