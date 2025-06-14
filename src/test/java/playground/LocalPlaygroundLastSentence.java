package playground;

import config.TestConfig;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;

@Disabled
@SpringBootTest(classes = {})
@ContextConfiguration(classes = {TestConfig.class})
class LocalPlaygroundLastSentence {
    //static String MODEL_NAME = "llama3.2";
    static final String MODEL_NAME = "mistral-small3.1:latest";
    static final String BASE_URL = "http://localhost:11434"; // local ollama base url


    @Test
    void testRus1() {
        String userPrompt = "Does content carry additional information or details? Respond with 'YES' or 'NO'.\nTitle: {0}\nContent: {1}";
        String testTitle = "ЦАХАЛ подвел итог операции в Газе: «Наступаем осторожно, поэтому мало пострадавших с нашей стороны»";
        String testText = "ЦАХАЛ подводит итог почти месячной операции в секторе Газа. С 18 марта, когда армия возобновила боевые действия в секторе, 350 самолетов и дронов ВВС нанесли удары по 1200 объектам террористических организаций и совершили более 100 точечных ликвидаций.";

        List<ChatMessage> messages = List.of(
                //SystemMessage.from(systemPrompt),
                UserMessage.from(MessageFormat.format(userPrompt, testTitle, testText))
        );


        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(Duration.ofSeconds(300))
                .build();


        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        System.out.println("Resonse: " + chatResponse.aiMessage().text());
    }



    @Test
    void testRus2() {
        String userPrompt = "Rewrite the content - use language of original text, preserve all details, do not repeat information given in title.\nTitle: {0}\nContent: {1}";
        String testTitle = "ЦАХАЛ подвел итог операции в Газе: «Наступаем осторожно, поэтому мало пострадавших с нашей стороны»";
        String testText = "ЦАХАЛ подводит итог почти месячной операции в секторе Газа. С 18 марта, когда армия возобновила боевые действия в секторе, 350 самолетов и дронов ВВС нанесли удары по 1200 объектам террористических организаций и совершили более ";

        List<ChatMessage> messages = List.of(
                //SystemMessage.from(systemPrompt),
                UserMessage.from(MessageFormat.format(userPrompt, testTitle, testText))
        );


        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(Duration.ofSeconds(300))
                .build();


        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        System.out.println("Resonse: " + chatResponse.aiMessage().text());
    }

}
