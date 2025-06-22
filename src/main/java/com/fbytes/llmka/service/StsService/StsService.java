package com.fbytes.llmka.service.StsService;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.LLMProvider.impl.LLMProviderBedrockMistral;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

import java.time.Instant;

@Service
public class StsService {
    private static final Logger logger = Logger.getLogger(LLMProviderBedrockMistral.class);

    @Value("${llmka.llm_provider.bedrock.role_arn}")
    private String roleARN;

    private AwsSessionCredentials sessionCredentials;
    private Instant expiration;
    private volatile StaticCredentialsProvider staticCredentialsProvider;

    private void refreshCredentials() {
        StsClient stsClient = StsClient.create();
        AssumeRoleRequest request = AssumeRoleRequest.builder()
                .roleArn(roleARN)
                .roleSessionName("LLMkaSession")
                .durationSeconds(900)
                .build();
        AssumeRoleResponse response = stsClient.assumeRole(request);
        sessionCredentials = AwsSessionCredentials.create(
                response.credentials().accessKeyId(),
                response.credentials().secretAccessKey(),
                response.credentials().sessionToken()
        );
        expiration = response.credentials().expiration();
        logger.info("Temporary credentials refreshed! Expiration: {}", expiration);
    }

    synchronized public StaticCredentialsProvider getCredentialsProvider() {
        if (sessionCredentials == null || Instant.now().isAfter(expiration)) {
            refreshCredentials();
            staticCredentialsProvider = StaticCredentialsProvider.create(sessionCredentials);
        }
        return staticCredentialsProvider;
    }
}
