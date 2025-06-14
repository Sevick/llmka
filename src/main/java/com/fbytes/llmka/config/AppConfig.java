package com.fbytes.llmka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000); // 1 seconds
        factory.setReadTimeout(10000);  // 10 seconds
        return new RestTemplate(factory);
    }

    @Bean("LLMRestTemplate")
    public RestTemplate longTimeoutRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000); // 1 seconds
        factory.setReadTimeout(300000);  // 5 minutes
        return new RestTemplate(factory);
    }
}
