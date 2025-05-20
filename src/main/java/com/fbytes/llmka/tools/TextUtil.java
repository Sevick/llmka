package com.fbytes.llmka.tools;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import dev.langchain4j.model.chat.request.json.JsonSchema;

import java.text.BreakIterator;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    private static final String regexEndsWithDelimiter = ".*[\\.\\?!\\\"]$";
    private static final Pattern patternEndsWithDelimiter = Pattern.compile(regexEndsWithDelimiter);

    private static String[] trashTails = {
            "read more",
            "read more at",
            "read more here",
            "read more on",
            "read more about",
            "read more from",
            "read the full article",
            "read the full story",
            "read the full post",
            "read the full text",
            "read the full report",
            "read the full review",
            "read the full transcript",
            "the post appeared first on",
            "\\[…\\]",
            "…",
            "…\\.",
            "\\.\\.\\."
    };
    private static String regexTrashTails = "(?:" + String.join("|", trashTails) + ")\\s*[\\.,;]*$";
    private static Pattern patternTrashTails = Pattern.compile(regexTrashTails);


    private TextUtil() {
    }

    public static int extractLastSentenceIdx(String src) {
        if (src == null || src.trim().isEmpty()) {
            return 0;
        }
        BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(Locale.ENGLISH); // TODO: Set locale per-group
        sentenceIterator.setText(src);
        sentenceIterator.last();
        return sentenceIterator.previous();
    }


    public static int countWords(String src) {
        if (src == null || src.trim().isEmpty()) {
            return 0;
        }
        BreakIterator breakIterator = BreakIterator.getWordInstance();
        breakIterator.setText(src);

        int wordCount = 0;
        int start = breakIterator.first();
        int end = breakIterator.next();

        // Iterate through all boundaries
        while (end != BreakIterator.DONE) {
            // Check if the segment starts with a letter or digit (indicating a word)
            if (Character.isLetterOrDigit(src.charAt(start))) {
                wordCount++;
            }
            start = end;
            end = breakIterator.next();
        }
        return wordCount;
    }


    public static String checkAddLastDot(String src) {
        if (src == null || src.trim().isEmpty()) {
            return src;
        }
        return src.transform(str -> {
            String trimmed = str.trim();
            Matcher matcher = patternEndsWithDelimiter.matcher(trimmed);
            if (!matcher.matches())
                return trimmed + ".";
            else
                return trimmed;
        });
    }

    public static String normalize(String src) {
        String normalized = Normalizer.normalize(src, Normalizer.Form.NFD);
        normalized = normalized.trim().replaceAll("\\s+", " ");
        return normalized;
    }


    public static Optional<String> stringToOptional(String src) {
        if (src == null || src.isEmpty())
            return Optional.empty();
        else
            return Optional.of(src);
    }


    public static boolean extractYesNo(String src) throws TextParsingException {
        String yesNoStr = src.trim().split("[\n \\.,;]")[0].toLowerCase();
        switch (yesNoStr) {
            case "yes", "**yes**", "**yes":
                return true;
            case "no", "**no**", "**no":
                return false;
            default:
                throw new TextParsingException("Unable to interpret the string as 'yes' or 'no'");
        }
    }


    public static JsonSchema genJsonSchemaFromClass(Class clazz) throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(clazz, visitor);
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = visitor.finalSchema();
        return JsonSchema.builder().build();
    }

    public static class TextParsingException extends Exception {
        public TextParsingException(String message) {
            super(message);
        }
    }

    public static String trimToLength(String src, int lengthLimit) {
        return src.length() > lengthLimit ? src.substring(0, lengthLimit) : src;
    }

    public static String cleanMarkdown(String markdownText) {
        if (markdownText == null || markdownText.isEmpty()) {
            return markdownText;
        }
        String cleanText = markdownText
                .replaceAll("#+\\s*", "") // Remove headers
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1") // Remove bold
                .replaceAll("\\*(.*?)\\*", "$1") // Remove italic
                .replaceAll("\\[.*?\\]\\(.*?\\)", ""); // Remove links
        cleanText.replaceAll(".", "\\.");

        return cleanText.trim();
    }


    public static String trimTail(String src){
        String strSrc;
        String strRes = src.toLowerCase().trim();
        do {
            strSrc = strRes;
            Matcher matcher = patternTrashTails.matcher(strRes);
            strRes = matcher.replaceAll("").trim();
        } while (!strSrc.equals(strRes));
        return src.substring(0, strRes.length());
    }
}
