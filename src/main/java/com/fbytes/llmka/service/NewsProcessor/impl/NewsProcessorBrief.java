package com.fbytes.llmka.service.NewsProcessor.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.BriefResponse;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.LLMProvider.ILLMProvider;
import com.fbytes.llmka.service.LLMService.ILLMService;
import com.fbytes.llmka.service.NewsProcessor.INewsProcessor;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Optional;

@Service
@Qualifier("NewsProcessorBrief")
public class NewsProcessorBrief extends NewsProcessor implements INewsProcessor {
    private static final Logger logger = Logger.getLogger(NewsProcessorBrief.class);

    //@Value("${llmka.brief.prompt.system:null}")
    private final Optional<String> systemPrompt;
    @Value("${llmka.brief.prompt.user}")
    private String userPrompt;
    @Value("${llmka.brief.description_size_limit}")
    private Integer descriptionSizeLimit;
    @Value("${llmka.brief.timeout}")
    private Duration timeOut;

    private final ILLMProvider llmProvider;

    private final ResponseFormat responseFormat;

    public NewsProcessorBrief(@Autowired ILLMService illmService,
                              @Value("${llmka.brief.llm_provider}") String llmProviderName,
                              @Value("${llmka.brief.prompt.system:null}") String systemPrompt) {

        this.llmProvider = illmService.findProvider(llmProviderName);

        JsonSchemaElement rootElement = JsonObjectSchema.builder()
                .addStringProperty("title", "The title of the news article")
                .addStringProperty("content", "The content of the news article")
                .required("title", "content")
                .build();

        JsonSchema jsonSchema = JsonSchema.builder()
                .name("Shapes")
                .rootElement(rootElement)
                .build();

        responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(BriefResponse.toLLMResonseSchema())
                .build();

        if (systemPrompt == null || systemPrompt.isEmpty())
            this.systemPrompt = Optional.empty();
        else
            this.systemPrompt = Optional.of(systemPrompt);
    }


    @Override
    @Timed(value = "llmka.briefmaker.time", description = "time to write short description")
    public NewsData process(NewsData newsData) {
        if (newsData.getDescription().orElse("").length() <= descriptionSizeLimit)
            return newsData;

        logger.debug("processing");

        String shortDescription =
                llmProvider.askLLM(systemPrompt,
                        MessageFormat.format(userPrompt, newsData.getTitle(), newsData.getDescription().orElse("")),
                        Optional.empty());

        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            BriefResponse briefResponse = objectMapper.readValue(shortDescription, BriefResponse.class);
//            NewsData result = newsData.toBuilder().build();
//            result.setDescription(Optional.of(briefResponse.getContent()));

            NewsData result = newsData.toBuilder().build();
            result.setDescription(Optional.of(shortDescription));
            result.setRewritten(true);
            logger.debug("finished processing New description: {}   Old description: {} ",
                    result.getDescription().orElse(""), newsData.getDescription().orElse(""));
            return result;
        } catch (Exception e) {
            logger.logException(e);
            throw new RuntimeException(e);
        }
    }
}
