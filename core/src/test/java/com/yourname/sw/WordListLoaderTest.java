package com.yourname.sw;

import com.yourname.sw.word.WordListLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordListLoaderTest {

    @Test
    void dedupTrimsDropsBlanksAndComments() {
        List<String> out = WordListLoader.dedup(
                Arrays.asList(" a ", "", "# comment", "a", "b", null));
        assertEquals(Arrays.asList("a", "b"), out);
    }

    @Test
    void loadsFromClasspath() throws IOException {
        List<String> out = WordListLoader.fromClasspath("wordlist-test.txt");
        assertEquals(Arrays.asList("alpha", "beta", "敏感词"), out);
    }

    @Test
    void loadsFromFile(@TempDir Path dir) throws IOException {
        Path f = dir.resolve("words.txt");
        Files.write(f, Arrays.asList("x", "# skip", "y", "x"), StandardCharsets.UTF_8);
        assertEquals(Arrays.asList("x", "y"), WordListLoader.fromFile(f));
    }
}
