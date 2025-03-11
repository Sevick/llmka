package com.fbytes.llmka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;


// getter & setter are mandatory to make ConfigurationProperties work
@Getter
@Setter
@ConfigurationProperties("llmka.herald.telegram.bot")
public class TelegramBotConfig {
    private Map<String, TelegramBotCreds> botparams;

    public Optional<TelegramBotCreds> retrieveBotCreds(String botName) {
        return Optional.ofNullable(botparams.get(botName));
    }

    @Getter
    @Setter
    public static class TelegramBotCreds {
        private String tgChannel;
        private String token;
    }
}