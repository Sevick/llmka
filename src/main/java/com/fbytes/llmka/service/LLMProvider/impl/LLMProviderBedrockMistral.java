package com.fbytes.llmka.service.LLMProvider.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.LLMProvider.LLMProvider;
import com.fbytes.llmka.service.StsService.StsService;
import dev.langchain4j.model.chat.request.ResponseFormat;
import io.micrometer.core.instrument.MeterRegistry;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.Optional;

@Service
@Qualifier("bedrockMistral")
public class LLMProviderBedrockMistral extends LLMProvider {
    private static final Logger logger = Logger.getLogger(LLMProviderBedrockMistral.class);

    @Value("${llmka.llm_provider.bedrock.model_name}")
    private String modelName;

    @Autowired
    StsService stsService;

    public LLMProviderBedrockMistral(@Autowired MeterRegistry meterRegistry) {
        super("BedrockMistral", meterRegistry);
    }

    @Override
    public String askLLMImpl(Optional<String> systemPrompt, String userPrompt, Optional<ResponseFormat> responseFormat) {
        logger.info("Asking LLM: {}", modelName);

        if (systemPrompt.isEmpty()) {
            systemPrompt = Optional.of("You are a helpful assistant.");
        }

        BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
                .credentialsProvider(stsService.getCredentialsProvider())
                .region(Region.US_EAST_1)
                .build();

        var nativeRequestTemplate = "{ \"prompt\": \"{{instruction}}\" }";
        var instruction = "<s>[INST] {{prompt}} [/INST]\\n".replace("{{prompt}}", userPrompt);
        var nativeRequest = nativeRequestTemplate.replace("{{instruction}}", instruction);
        try {
            var response = bedrockClient.invokeModel(request -> request
                    .body(SdkBytes.fromUtf8String(nativeRequest))
                    .modelId(modelName)
            );
            var responseBody = new JSONObject(response.body().asUtf8String());
            var text = new JSONPointer("/outputs/0/text").queryFrom(responseBody).toString();
            logger.debug("Response from LLM: {}", text);
            return text;
        } catch (SdkClientException e) {
            logger.logException(String.format("ERROR: Can't invoke '%s'. Reason: %s", modelName, e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }
}
