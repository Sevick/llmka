package unit;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.tools.TextUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextUtilWordsCountTest {
    private static final Logger logger = Logger.getLogger(TextUtilWordsCountTest.class);

    @Test
    void testNormalText() {
        String text = "Hello, world! This is a test.";
        assertEquals(6, TextUtil.countWords(text));
    }

    @Test
    void testSingleWord() {
        String text = "Hello";
        assertEquals(1, TextUtil.countWords(text));
    }

    @Test
    void testEmptyString() {
        String text = "";
        assertEquals(0, TextUtil.countWords(text));
    }

    @Test
    void testNullString() {
        String text = null;
        assertEquals(0, TextUtil.countWords(text));
    }

    @Test
    void testWhitespaceOnly() {
        String text = "   \t\n  ";
        assertEquals(0, TextUtil.countWords(text));
    }

    @Test
    void testNumbers() {
        String text = "123 456 789";
        assertEquals(3, TextUtil.countWords(text));
    }

    @Test
    void testPunctuationOnly() {
        String text = "!!! ,,, ...";
        assertEquals(0, TextUtil.countWords(text));
    }

    @Test
    void testMixedTextAndNumbers() {
        String text = "Hello 123 world 456!";
        assertEquals(4, TextUtil.countWords(text));
    }

    @Test
    void testMultipleSpacesAndPunctuation() {
        String text = "This   is,,,a   test!";
        assertEquals(4, TextUtil.countWords(text));
    }

    @Test
    void testNonLatinText() {
        String text = "こんにちは 世界";
        assertEquals(2, TextUtil.countWords(text));
    }
}
