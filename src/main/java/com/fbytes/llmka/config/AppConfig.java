package com.fbytes.llmka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class AppConfig {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

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



//    @Bean
//    public AwsCredentialsProvider awsCredentialsProvider() {
//        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
//    }
}
