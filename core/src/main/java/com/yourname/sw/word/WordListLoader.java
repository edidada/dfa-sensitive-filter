package com.yourname.sw.word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** Loads word lists from a {@code Collection}, a UTF-8 file, or a classpath resource. */
public final class WordListLoader {

    private WordListLoader() {}

    /** Trim, drop blanks / comment lines, deduplicate while preserving order. */
    public static List<String> dedup(Collection<String> raw) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : raw) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;
            set.add(t);
        }
        return new ArrayList<>(set);
    }

    public static List<String> fromList(Collection<String> words) {
        return dedup(words);
    }

    public static List<String> fromFile(Path file) throws IOException {
        return dedup(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    public static List<String> fromClasspath(String resource) throws IOException {
        String name = resource.startsWith("/") ? resource.substring(1) : resource;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = WordListLoader.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(name)) {
            if (in == null) throw new IOException("classpath resource not found: " + resource);
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = r.readLine()) != null) lines.add(line);
                return dedup(lines);
            }
        }
    }
}
