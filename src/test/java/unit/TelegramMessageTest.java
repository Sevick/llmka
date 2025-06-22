package unit;


import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

//class TelegramMessageTest {
//
//    @Autowired
//    private TelegramMessageFormatter telegramMessageFormatter;
//
//    @Test
//    void testFromString() {
//        String title = "Test - Title";
//        Optional<String> description = Optional.of("Test - description.");
//        NewsData newsData = NewsData.builder()
//                .id("TelegramMessageTest#")
//                .link("http://TelegramMessageTest.somelink")
//                .title(title)
//                .description(description)
//                .build();
//        TelegramMessage message = TelegramMessage.fromNewsData(newsData);
//        assertTrue(message.getMessageText().contains(title));
//        assertTrue(message.getMessageText().contains(description.get()));
//    }
//}
