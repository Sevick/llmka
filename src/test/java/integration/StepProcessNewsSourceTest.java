package integration;

import com.fbytes.llmka.integration.config.ChannelsConfig;
import com.fbytes.llmka.integration.steps.StepProcessNewsSource;
import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.model.newssource.NewsSource;
import com.fbytes.llmka.service.DataRetriver.DataRetrieveService;
import com.fbytes.llmka.service.DataRetriver.DataRetriever;
import com.fbytes.llmka.service.DataRetriver.IDataRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

@SpringBootTest //(classes = {embeddingService.class})
@ContextConfiguration(classes = {StepProcessNewsSource.class, ChannelsConfig.class, DataRetrieveService.class, StepProcessNewsSourceTest.TestConfig.class})
public class StepProcessNewsSourceTest {
    private static final Logger logger = Logger.getLogger(StepProcessNewsSourceTest.class);

    @Autowired
    @Qualifier("newsSourceChannel")
    private MessageChannel newsSourceChannel;

    @Autowired
    @Qualifier("newsDataChannelOut")
    private DirectChannel newsDataChannelOut;

    final static NewsData[] resultData = new NewsData[] {
            NewsData.builder()
                    .id("ID1")
                    .dataSourceID("DataSourceID")
                    .link("http://somelink1")
                    .title("Title1")
                    .description(Optional.of("Description 1"))
                    .text(Optional.empty())
                    .build(),
            NewsData.builder()
                    .id("ID2")
                    .dataSourceID("DataSourceID")
                    .link("http://somelink2")
                    .title("Title2")
                    .description(Optional.of("Description 2"))
                    .text(Optional.empty())
                    .build()
    };


    @TestConfiguration
    @EnableIntegration
    static class TestConfig {
        @Bean
        public IDataRetriever dataRetrieverTst() {
            return new DataRetriever() {
                @Override
                public Optional<Stream<NewsData>> retrieveData(NewsSource dataSource) {
                    return Optional.of(Arrays.stream(resultData));
                }
            };
        }
    }


    @Test
    public void testFlow() {
        NewsSource tstNewsSource = new TstNewsSource("tst-id1", "tst-newsSourceName", "tst-newsSourceGroup");
        TestMessageHandler testMessageHandler = new TestMessageHandler();
        newsDataChannelOut.subscribe(testMessageHandler);
        Message<NewsSource> message = MessageBuilder.withPayload(tstNewsSource).build();

        newsSourceChannel.send(message);
        int outMessagesCount = testMessageHandler.getConcurrentLinkedQueue().size();
        Assert.isTrue(outMessagesCount == resultData.length, String.format("Number of messages reached output channel: {0}, Expected: {1}", outMessagesCount, resultData.length));
        //Message<?> result = newsDataChannelOut..receive(1000);
    }


    private class TstNewsSource extends NewsSource {
        public TstNewsSource(String id, String name, String group) {
            super(id, "tst", name, group);
        }
    }

    private class TestMessageHandler implements MessageHandler {
        private static final Logger logger = Logger.getLogger(TestMessageHandler.class);
        private final LinkedBlockingQueue<Message> concurrentLinkedQueue = new LinkedBlockingQueue<>(10);

        public LinkedBlockingQueue<Message> getConcurrentLinkedQueue() {
            return concurrentLinkedQueue;
        }

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            logger.debug("Message: {}", message);
            concurrentLinkedQueue.add(message);
        }
    }
}
