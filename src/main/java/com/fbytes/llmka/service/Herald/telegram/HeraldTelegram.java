package com.fbytes.llmka.service.Herald.telegram;

import com.fbytes.llmka.config.TelegramBotConfig;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.config.heraldchannel.HeraldConfigTelegram;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import com.fbytes.llmka.service.Herald.Herald;
import com.google.common.util.concurrent.RateLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.text.MessageFormat;
import java.util.Optional;


public class HeraldTelegram extends Herald<TelegramMessage> {
    private static final Logger logger = Logger.getLogger(HeraldTelegram.class);

    @Autowired
    TelegramBotConfig telegramBotConfigService;
    @Value("#{T(Float).parseFloat('${llmka.herald.telegram.rate_per_sec}')}")
    private Float ratePerSecond;

    private final String botUsername;
    private RateLimiter rateLimiter;
    private String botToken;
    private String tgChannel;
    private TelegramLongPollingBot telegramLongPollingBot;


    public HeraldTelegram(HeraldConfigTelegram heraldConfigTelegram) {
        super(heraldConfigTelegram.getName(), "TELEGRAM", TelegramMessage.class);
        this.botUsername = heraldConfigTelegram.getBot();
    }


    @PostConstruct
    private void init() {
        logger.info("[{}] Initializing HeraldTelegram", getName());
        this.rateLimiter = RateLimiter.create(ratePerSecond);
        Optional<TelegramBotConfig.TelegramBotCreds> telegramBotConfig = telegramBotConfigService.retrieveBotCreds(botUsername.toLowerCase());
        if (telegramBotConfig.isEmpty())
            throw new RuntimeException();
        tgChannel = telegramBotConfig.get().getTgChannel();
        botToken = telegramBotConfig.get().getToken();

        // TODO: check bot options
        telegramLongPollingBot = new TelegramLongPollingBot(botToken) {
            @Override
            public void onUpdateReceived(Update update) {
                // do nothing
            }

            @Override
            public String getBotUsername() {
                return "@" + botUsername;
            }
        };

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            var session = botsApi.registerBot(telegramLongPollingBot);
            session.stop();
        } catch (TelegramApiException e) {
            logger.logException("Failed to register bot", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    @Counted(value = "llmka.herald.telegram.sendmessage.counter", description = "The number of messages sent")
    @Timed(value = "llmka.herald.telegram.sendmessage.time", description = "time to send Telegram message")
    public void sendMessage(TelegramMessage msg) {
        logger.trace("[{}] Acquiring rateLimiter", getName());
        rateLimiter.acquire();
        logger.trace("[{}] Acquired rateLimiter", getName());
        SendMessage message = SendMessage.builder()
                .chatId(tgChannel)
                .text(msg.getMessageText())
                .build();
        message.disableWebPagePreview();
        message.setParseMode("Markdown");
        try {
            telegramLongPollingBot.execute(message);
            logger.info("[{}] Message sent successfully!", getName());
        } catch (TelegramApiException e) {
            logger.logException(MessageFormat.format("[{0}] Failed to send message", getName()), e);
        }
    }
}
