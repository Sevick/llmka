package real;

import com.fbytes.llmka.LLMka;
import com.fbytes.llmka.config.TelegramBotConfig;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import com.fbytes.llmka.service.Herald.impl.HeraldServiceTelegram;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest(classes = LLMka.class)
@EnableConfigurationProperties(TelegramBotConfig.class)
public class TelegramBotTest {

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private TelegramBotConfig telegramBotConfig;

    private final String testChannel = "xxxxx";

    @Disabled
    @Test
    public void sendMessageTest() {

        HeraldServiceTelegram newHeraldService = new HeraldServiceTelegram(testChannel);
        context.getAutowireCapableBeanFactory().autowireBean(newHeraldService);
        context.getBeanFactory().registerSingleton("TestHerald", newHeraldService);

        newHeraldService.sendMessage(new TelegramMessage("TestMessage#4"));
    }
}
