package unit;

import com.fbytes.llmka.tools.TextUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextUtilTrashTail {

    @Test
    void testTrimTail() {
        // Trash tail at the end
        assertEquals("Hello world", TextUtil.trimTail("Hello world read more"));

        // Trash tail "..." at the end
        assertEquals("Hello world", TextUtil.trimTail("Hello world..."));

        // Literal "[…]" at the end (not removed due to regex)
        assertEquals("Hello world", TextUtil.trimTail("Hello world […]"));

        // Trash tail with punctuation
        assertEquals("Hello world", TextUtil.trimTail("Hello world read more."));

        // Multiple trash tails
        assertEquals("Hello world", TextUtil.trimTail("Hello world read more..."));

        // Trash tail not at the end
        assertEquals("read more Hello world", TextUtil.trimTail("read more Hello world"));

        // Entire string is a trash tail
        assertEquals("", TextUtil.trimTail("read more"));

        // Trash tail "..." alone
        assertEquals("", TextUtil.trimTail("..."));

        // Single dot (unintended removal)
        assertEquals("Hello world.", TextUtil.trimTail("Hello world."));

        // Multiple dots
        assertEquals("Hello world..", TextUtil.trimTail("Hello world.."));

        // Leading spaces
        assertEquals("   Hello wo", TextUtil.trimTail("   Hello world read more"));

        // Empty string
        assertEquals("", TextUtil.trimTail(""));

        // Trash tail with punctuation alone
        assertEquals("", TextUtil.trimTail("read more."));

        // Different casing
        assertEquals("Hello world", TextUtil.trimTail("Hello world READ MORE"));

        // Literal "[…]" with punctuation
        assertEquals("Hello world.", TextUtil.trimTail("Hello world.[…]"));

        // Trash tail "…" at the end
        assertEquals("Hello world", TextUtil.trimTail("Hello world…"));
    }
}