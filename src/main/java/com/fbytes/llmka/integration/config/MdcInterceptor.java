package com.fbytes.llmka.integration.config;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.service.Maintenance.MDC.IMDCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;


@Service
@GlobalChannelInterceptor(patterns = {"*${llmka.integration.queue_suffix}"})
public class MdcInterceptor implements ChannelInterceptor {
    private static final Logger logger = Logger.getLogger(MdcInterceptor.class);

    @Value("${llmka.newssource_header}")
    private String newsSourceHeader;
    @Value("${llmka.newsdata_header}")
    private String newsDataHeader;

    @Autowired
    private IMDCService mdcService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (channel instanceof PollableChannel) {
            logger.trace("Cleaning the MDC context for PollableChannel");
            mdcService.clearMDC();  // clear MDC in producer's thread
        }
        return message;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel){
        if (channel instanceof PollableChannel) {
            logger.trace("Setting MDC context for PollableChannel");
            Map<String, String> mdcMap = message.getHeaders().entrySet().stream()
                    .filter(entry -> entry.getKey().equals(newsSourceHeader) || entry.getKey().equals(newsDataHeader))
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
            mdcService.setMDC(mdcMap);
        }
        return message;
    }
}
