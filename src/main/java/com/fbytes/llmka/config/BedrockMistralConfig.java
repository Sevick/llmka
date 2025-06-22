//package com.fbytes.llmka.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.ai.chat.client.ChatClient;
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//
//@Configuration
//public class BedrockMistralConfig {
//
//    @Bean
//    public ChatClient mistralChatClient() {
//        // This uses the default AWS credentials provider chain
//        // Make sure your AWS credentials are configured properly (env, profile, etc.)
//        return new BedrockChatClient(
//                Region.of(awsRegion),
//                DefaultCredentialsProvider.create(),
//                // Model id for Mistral on Bedrock; latest as of June 2024: "mistral.mistral-7b-instruct-v0:2"
//                "mistral.mistral-7b-instruct-v0:2"
//        );
//    }
//
//    @Bean
//    public BedrockChatClient bedrockChatClient(
//            @Value("${spring.ai.bedrock.region}") String region,
//            @Value("${spring.ai.bedrock.model-id}") String modelId,
//            @Value("${spring.ai.bedrock.credentials.access-key}") String accessKey,
//            @Value("${spring.ai.bedrock.credentials.secret-key}") String secretKey) {
//        return new BedrockChatClient(region, modelId, accessKey, secretKey);
//    }
//}
