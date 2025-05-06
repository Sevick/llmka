package playground;

import config.TestConfig;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
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
import java.util.List;

@Disabled
@SpringBootTest(classes = {})
@ContextConfiguration(classes = {TestConfig.class})
class OllamaPlaygroundBrief {
    //static String MODEL_NAME = "llama3.2";
    static final String MODEL_NAME = "phi4-mini";
    static final String BASE_URL = "http://192.168.1.72:11434"; // local ollama base url


    static final String systemPrompt = "";

    private String promptPhi4 = "{0}";
    private String systemPromptPhi4 = "Rewrite the provided text in one or two sentences. Preserve the original language and tone. Do not add any comments or links.";

    @Test
    void testRus(){
        String testText = "Премьер-министр молчал несколько часов и наконец дал ответ: \"Очередное повторение прозрачного фейка Йорама Коэна, который превратился в политика и покрывает коррупцию в ШАБАК Ронена Бара посредством смехотворной лжи\". Коэн сообщил в интервью \"Галей ЦАХАЛ\", что Нетанияху хотел от него материалов, позволяющих изгнать Беннета из военно-политического кабинета.";

        List<ChatMessage> messages = List.of(
                //SystemMessage.from("You should analyze the text and give one-word binary answer (Yes/No) if it's a commercial or not."),
                SystemMessage.from(systemPromptPhi4),
                UserMessage.from(MessageFormat.format(promptPhi4,testText))
        );

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .responseFormat(ResponseFormat.TEXT)
                .temperature(0.1d)
                .build();

        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        System.out.println(chatResponse.aiMessage().text());
    }

    @Test
    void testEng(){
        String testText = "NFL Adopts Sony's 'Virtual Measurements' for Football's First Downs. theodp writes: America's National Football League announced that beginning with the 2025 season, Sony's Hawk-Eye virtual measurement technology will assess and identify first downs after a ball spot. Sony's Hawk-Eye virtual measurement technology, which consists of six 8K cameras for optical tracking of the position of the ball, is operated from the NFL's \"Art McNally GameDay Central Officiating Center\" in New York and is integrated with the League's existing replay system. It will serve as an efficient alternative to the process of having a three-person chain crew walk chains onto the field and manually measure whether 10 yards have been met after the official has spotted the ball. However, the chain crew will remain on the field in a secondary capacity. The NFL's executive VP of football operations says their move brings \"world-class on field officiating with state-of-the-art technology to advance football excellence.\" (The NFL's announcement notes the whole process takes about 30 seconds, \"saving up to 40 seconds from a measurement with the chains.\") The move comes a full seven years after Apple introduced its iPhone Measure app...";

        List<ChatMessage> messages = List.of(
                //SystemMessage.from("You should analyze the text and give one-word binary answer (Yes/No) if it's a commercial or not."),
                SystemMessage.from(systemPromptPhi4),
                UserMessage.from(MessageFormat.format(promptPhi4,testText))
        );

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .responseFormat(ResponseFormat.TEXT)
                .build();

        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        System.out.println(chatResponse.aiMessage().text());
    }


    @Test
    void testLongEng(){
        String testText = "\"I started working on Voyager in 1977,\" the Voyager mission's project scientist told Gizmodo Saturday in a new interview. \"It was my first job out of college.\" 35 years later, a Voyager probe became the first spacecraft to cross into interstellar space in 2012, with Voyager 2 following in 2018. But while each Voyager spacecraft carries 10 scientific instruments, all but three have now been turned off to conserve power, Gizmodo writes. \"The two spacecraft now have enough power to operate for another year or so before engineers are forced to turn off two more instruments...\" Voyager Mission Project Scientist Linda Spilker: The number of people that are working on and flying Voyager is a whole lot smaller than it was in the planetary days... The challenge was, can we reach the heliopause? We didn't know where it was, we had no idea how far away it was. We got to Neptune, and then we thought, \"well, maybe it's just another 10 astronomical units or so, a little bit further, a little bit further.\" And so every time we got a little bit further, the modelers would go back, scratch their heads and say, \"ah, it could be a little bit more, a little bit farther away,\" and so on and on that continued, until finally, Voyager 1 crossed the heliopause in 2012... Gizmodo: Is it an emotional decision to turn off Voyager's instruments? Spilker: I was talking to the cosmic ray instrument lead, and I said, \"Wow, this must really be tough for you to see your instrument turned off.\" He helped build the instrument in the early 1970s. This instrument that's been sending you data, and that's been part of your life for over 50 years now. And he said, it was hard to think about turning it off for the whole team. It's kind of like losing a best friend, or someone that's been a part of your life for so many years, and then suddenly, it's silent. At the same time, there's this pride that you were part of that, and your instrument got so much great data — so it's a mix of emotions... The spacecraft had a lot of redundancy on it, so that means two of every computer and two of all the key components. We've been able to turn off those backup units, but we're now at the point where, to really get a significant amount of power, all that's left are some of the science instruments to turn off. So, that's where we're at... How cold can the lines get before they freeze? How cold can some of these other components get before they stop working? So that's another challenge. Then there are individual tiny thrusters that align the spacecraft and keep that antenna pointed at the Earth so we can send the data back, and they're very slowly clogging up with little bits of silica, and so their puffs are getting weaker and weaker. That's another challenge that we're going through to balance. But we're hopeful that we can get one, possibly two, spacecraft to the 50th anniversary in 2027. Voyager's golden anniversary, and perhaps even into the early 2030s with one, maybe two, science instruments. \"We're well past the warranty of four years...\" Spilker says at one point. And \"We're still working and thinking about an interstellar probe that would go much, much farther than Voyager. \"You're talking about a multi-generation mission.\"";

        List<ChatMessage> messages = List.of(
                //SystemMessage.from("You should analyze the text and give one-word binary answer (Yes/No) if it's a commercial or not."),
                SystemMessage.from(systemPromptPhi4),
                UserMessage.from(MessageFormat.format(promptPhi4,testText))
        );

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .responseFormat(ResponseFormat.TEXT)
                .build();

        ChatResponse chatResponse = model.chat(ChatRequest.builder().messages(messages).build());
        System.out.println(chatResponse.aiMessage().text());
    }
}
