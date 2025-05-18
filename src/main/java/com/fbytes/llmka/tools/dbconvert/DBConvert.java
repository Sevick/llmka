package com.fbytes.llmka.tools.dbconvert;

import com.fbytes.llmka.model.NewsData;
import com.fbytes.llmka.service.InMemoryFastStore.InMemoryFastStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;


public class DBConvert {

    public static void convert(String filePath) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        InMemoryEmbeddingStore<TextSegment> a = InMemoryEmbeddingStore.fromFile(filePath);

        // access entries via reflection
        Field field = InMemoryEmbeddingStore.class.getDeclaredField("entries");
        field.setAccessible(true);
        CopyOnWriteArrayList<?> entries = (CopyOnWriteArrayList<?>) field.get(a);

        entries.forEach(entry -> {
            ;
            // convert each entry to the new format
            // for example, if the entry is of type TextSegment, you can convert it to a different type
            // or just print it out
            System.out.println(entry);
        });

        InMemoryFastStore<NewsData> b = new InMemoryFastStore<>();


    }


    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        String filePath = args[0];
        if (filePath == null || filePath.isEmpty()) {
            System.out.println("Please provide a file path.");
            return;
        }
        // read dicrectory interate over all files
        File directory = new File(filePath);
        if (!directory.isDirectory()) {
            System.out.println("The provided path is not a directory.");
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in the directory.");
            return;
        }

        // iterate over all files
        for (File file : files) {
            if (file.isFile()) {
                System.out.println("Converting file: " + file.getName());
                // convert each file
                convert(file.getAbsolutePath());
            } else {
                System.out.println("Skipping non-file: " + file.getName());
            }
        }

    }

}
