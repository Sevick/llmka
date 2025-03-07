package real;

import com.fbytes.llmka.service.Herald.impl.TelegramBotService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TelegramBotService.class)
public class TelegramBotTest {

    @Autowired
    TelegramBotService telegramBotService;

    @Value("${LLMka.herald.telegram.bot.testchannel}")
    private String channel;

    @Disabled
    @Test
    public void sendMessageTest() {
        telegramBotService.sendMessage(channel, "TestMessage#3");
    }
}
