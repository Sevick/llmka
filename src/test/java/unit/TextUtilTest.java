package unit;

import com.fbytes.llmka.logger.Logger;
import com.fbytes.llmka.tools.TextUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilTest {
    private static final Logger logger = Logger.getLogger(TextUtilTest.class);

    @Test
    void extractYesNo_No1() throws TextUtil.TextParsingException {
        String src = "No.\n\nSome other text";
        boolean result = TextUtil.extractYesNo(src);
        assertEquals(false, result);
    }

    @Test
    void extractYesNo_NO() throws TextUtil.TextParsingException {
        String src = "NO\n\nSome other text";
        boolean result = TextUtil.extractYesNo(src);
        assertFalse(result);
    }

    @Test
    void extractYesNo_Yes() throws TextUtil.TextParsingException {
        String src = "Yes.\n\nSome other text";
        boolean result = TextUtil.extractYesNo(src);
        assertTrue(result);
    }


    @Test
    void extractYesNo_No2() throws TextUtil.TextParsingException {
        String src = "**No.**\n";
        boolean result = TextUtil.extractYesNo(src);
        assertFalse(result);
    }

    @Test
    void extractYesNo_YES() throws TextUtil.TextParsingException {
        String src = "YES\n\nSome other text";
        boolean result = TextUtil.extractYesNo(src);
        assertTrue(result);
    }

    @Test
    void extractYesNo_Y() throws TextUtil.TextParsingException {
        String src = "Y";
        Exception exception = assertThrows(TextUtil.TextParsingException.class, () -> {
            TextUtil.extractYesNo(src);
        });
    }

    @Test
    void extractYesNo_Empty() throws TextUtil.TextParsingException {
        String src = "";
        Exception exception = assertThrows(TextUtil.TextParsingException.class, () -> {
            TextUtil.extractYesNo(src);
        });
    }


    @Test
    void cleanMarkdownTest1() {
        String src = "# Hello, World!\nThis is **bold** and *italic* text. Here's [a link](http://example.com).\n- Item 1\n- Item 2";
        String result = TextUtil.cleanMarkdown(src);
        assertEquals("Hello, World!\n" +
                "This is bold and italic text. Here's .\n" +
                "- Item 1\n" +
                "- Item 2", result);
    }

    @Test
    void trimTailTest() {
        String src = "This is a test string. ... The post appeared first on ";
        String result = TextUtil.trimTail(src);
        assertEquals("This is a test string.", result);
    }

}
