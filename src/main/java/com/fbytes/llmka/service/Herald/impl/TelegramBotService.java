package com.fbytes.llmka.service.Herald.impl;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.Herald.IHeraldService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBotService extends TelegramLongPollingBot implements IHeraldService {

    @Value("${LLMka.herald.telegram.bot.username}")
    private String botUsername;
    @Value("${LLMka.herald.telegram.bot.token}")
    private String botToken;

    private static final Logger logger = Logger.getLogger(TelegramBotService.class);

    @Override
    public void onUpdateReceived(Update update) {
        // Handle incoming updates here (optional)
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void sendMessage(String channel, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(channel)
                .text(text)
                .build();
        message.disableWebPagePreview();
        message.setParseMode("Markdown");
        try {
            execute(message);
            logger.info("Message sent successfully!");
        } catch (TelegramApiException e) {
            logger.logException(e);
        }
    }
}

