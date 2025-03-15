package com.fbytes.llmka.service.Herald.impl;

import com.fbytes.llmka.config.TelegramBotConfig;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.heraldchannel.HeraldTelegram;
import com.fbytes.llmka.model.heraldmessage.TelegramMessage;
import com.fbytes.llmka.service.Herald.IHeraldService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Optional;


public class HeraldServiceTelegram implements IHeraldService<TelegramMessage> {
    private static final Logger logger = Logger.getLogger(HeraldServiceTelegram.class);

    @Autowired
    TelegramBotConfig telegramBotConfigService;

    private String botUsername;
    private String botToken;
    private String tgChannel;
    private TelegramLongPollingBot telegramLongPollingBot;

    public HeraldServiceTelegram(String botUsername) {
        this.botUsername = botUsername;
    }

    public HeraldServiceTelegram(HeraldTelegram heraldChannelTelegram) {
        this.botUsername = heraldChannelTelegram.getBot();
    }


    @PostConstruct
    private void init() {
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
            botsApi.registerBot(telegramLongPollingBot);
        } catch (TelegramApiException e) {
            logger.logException(e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void sendMessage(TelegramMessage msg) {
        SendMessage message = SendMessage.builder()
                .chatId(tgChannel)
                .text(msg.getMessageText())
                .build();
        message.disableWebPagePreview();
        message.setParseMode("Markdown");
        try {
            telegramLongPollingBot.execute(message);
            logger.info("Message sent successfully!");
        } catch (TelegramApiException e) {
            logger.logException(e);
        }
    }
}
