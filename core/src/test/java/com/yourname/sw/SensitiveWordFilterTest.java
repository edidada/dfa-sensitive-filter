package com.yourname.sw;

import com.yourname.sw.engine.AhoCorasickEngine;
import com.yourname.sw.engine.DfaTrieEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveWordFilterTest {

    @Test
    void containsAndFindAllAreCaseInsensitiveByDefault() {
        SensitiveWordFilter f = new SensitiveWordFilter();
        f.init(Collections.singletonList("BadWord"));
        assertTrue(f.contains("this is a badword here"));
        List<MatchResult> all = f.findAll("this is a badword here");
        assertEquals(1, all.size());
        assertEquals("badword", all.get(0).getWord());
        assertEquals(10, all.get(0).getStart());
        assertEquals(17, all.get(0).getEnd());
    }

    @Test
    void fullWidthInputMatchesHalfWidthDictionary() {
        SensitiveWordFilter f = new SensitiveWordFilter();
        f.init(Collections.singletonList("abc"));
        assertTrue(f.contains("ａｂｃ"));
        assertEquals("ａｂｃ", f.findAll("ａｂｃ").get(0).getWord());
    }

    @Test
    void wordBoundaryPreventsAssInsideClass() {
        SensitiveWordFilter f = new SensitiveWordFilter(
                SensitiveWordConfig.builder().wordBoundary(true).build());
        f.init(Collections.singletonList("ass"));
        assertFalse(f.contains("class"));
        List<MatchResult> all = f.findAll("class ass");
        assertEquals(1, all.size());
        assertEquals(6, all.get(0).getStart());
        assertEquals(9, all.get(0).getEnd());
        assertTrue(f.contains("ass"));
    }

    @Test
    void chineseWordsAreNotBrokenByBoundaryRule() {
        SensitiveWordFilter f = new SensitiveWordFilter(
                SensitiveWordConfig.builder().wordBoundary(true).build());
        f.init(Collections.singletonList("敏感词"));
        assertTrue(f.contains("很敏感词啊"));
    }

    @Test
    void replaceMasksHits() {
        SensitiveWordFilter f = new SensitiveWordFilter();
        f.init(Arrays.asList("bad"));
        assertEquals("*** and ***", f.replace("bad and bad", ReplaceMode.CHAR));

        SensitiveWordFilter g = new SensitiveWordFilter();
        g.init(Arrays.asList("badword"));
        assertEquals("*** here", g.replace("badword here", ReplaceMode.WORD));
        assertEquals("******* here", g.replace("badword here", ReplaceMode.CHAR));
    }

    @Test
    void replaceKeepsLongestOverlappingHit() {
        SensitiveWordFilter f = new SensitiveWordFilter();
        f.init(Arrays.asList("abc", "abcdef"));
        assertEquals("***", f.replace("abcdef", ReplaceMode.WORD)); // one hit only
    }

    @Test
    void autoEngineSwitchesAtThreshold() {
        SensitiveWordFilter small = new SensitiveWordFilter();
        small.init(Arrays.asList("a", "b", "c"));
        assertEquals(DfaTrieEngine.NAME, small.engineName());

        List<String> many = new ArrayList<>();
        for (int i = 0; i < 150; i++) many.add("w" + i);
        SensitiveWordFilter big = new SensitiveWordFilter();
        big.init(many);
        assertEquals(AhoCorasickEngine.NAME, big.engineName());
    }

    @Test
    void explicitEngineOverride() {
        SensitiveWordFilter f = new SensitiveWordFilter(
                SensitiveWordConfig.builder()
                        .engineType(SensitiveWordConfig.EngineType.AC).build());
        f.init(Collections.singletonList("a"));
        assertEquals(AhoCorasickEngine.NAME, f.engineName());
    }

    @Test
    void hotReloadAtomicallySwapsDictionary() {
        SensitiveWordFilter f = new SensitiveWordFilter();
        f.init(Collections.singletonList("alpha"));
        assertTrue(f.contains("alpha"));
        f.reload(Collections.singletonList("beta"));
        assertFalse(f.contains("alpha"));
        assertTrue(f.contains("beta"));
    }

    @Test
    void maxTextLengthIsEnforced() {
        SensitiveWordFilter f = new SensitiveWordFilter(
                SensitiveWordConfig.builder().maxTextLength(5).build());
        f.init(Collections.singletonList("x"));
        assertThrows(IllegalArgumentException.class, () -> f.contains("123456"));
    }

    @Test
    void uninitializedFilterFailsFast() {
        SensitiveWordFilter f = new SensitiveWordFilter();
        assertThrows(IllegalStateException.class, () -> f.contains("anything"));
    }

    @Test
    void loadsFromClasspathAndFile(@TempDir Path dir) throws IOException {
        SensitiveWordFilter f = new SensitiveWordFilter();
        f.initFromClasspath("wordlist-test.txt");
        assertTrue(f.contains("alpha"));
        assertTrue(f.contains("敏感词"));

        Path file = dir.resolve("words.txt");
        Files.write(file, Arrays.asList("foo", "bar"), StandardCharsets.UTF_8);
        SensitiveWordFilter g = new SensitiveWordFilter();
        g.init(file);
        assertTrue(g.contains("foo"));
    }
}
