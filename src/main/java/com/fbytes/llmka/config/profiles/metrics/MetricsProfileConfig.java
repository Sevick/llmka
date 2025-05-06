package com.fbytes.llmka.config.profiles.metrics;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("metrics")
@EnableAspectJAutoProxy         // used for metrics-collection aspect
public class MetricsProfileConfig {

    @Bean
    public TimedAspect timedAspect(@Autowired MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
