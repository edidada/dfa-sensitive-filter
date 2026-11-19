package com.yourname.sw;

import com.yourname.sw.engine.AhoCorasickEngine;
import com.yourname.sw.engine.DfaTrieEngine;
import com.yourname.sw.engine.Engine;
import com.yourname.sw.normalize.CharNormalizer;
import com.yourname.sw.word.WordListLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe facade. init()/reload() build a fully immutable engine and atomically swap
 * it in via {@link AtomicReference}, so concurrent match() calls never see a partial state.
 * Deliberately NOT based on regex - see docs/architecture.md (ReDoS).
 */
public final class SensitiveWordFilter {

    private final SensitiveWordConfig config;
    private final CharNormalizer normalizer;
    private final AtomicReference<Engine> engineRef = new AtomicReference<>();

    public SensitiveWordFilter() { this(SensitiveWordConfig.defaults()); }

    public SensitiveWordFilter(SensitiveWordConfig config) {
        this.config = config;
        this.normalizer = new CharNormalizer(config);
    }

    public SensitiveWordConfig getConfig() { return config; }

    /** Initialize from a list/collection of words. */
    public void init(Collection<String> words) {
        List<String> cleaned = WordListLoader.dedup(words);
        Engine engine = chooseEngine(cleaned.size());
        for (String w : cleaned) engine.addWord(normalizer.normalize(w));
        engine.build();
        engineRef.set(engine);
    }

    /** Initialize from a UTF-8 text file (one word per line, '#' starts a comment). */
    public void init(Path file) throws IOException {
        init(WordListLoader.fromFile(file));
    }

    /** Initialize from a classpath resource. */
    public void initFromClasspath(String resource) throws IOException {
        init(WordListLoader.fromClasspath(resource));
    }

    /** Hot-swap the dictionary. Safe to call while other threads are matching. */
    public void reload(Collection<String> words) { init(words); }

    private Engine chooseEngine(int dictionarySize) {
        switch (config.getEngineType()) {
            case DFA: return new DfaTrieEngine();
            case AC:  return new AhoCorasickEngine();
            case AUTO:
            default:
                return dictionarySize < SensitiveWordConfig.AUTO_DFA_THRESHOLD
                        ? new DfaTrieEngine() : new AhoCorasickEngine();
        }
    }

    private Engine requireEngine() {
        Engine e = engineRef.get();
        if (e == null) throw new IllegalStateException("filter is not initialized; call init(...) first");
        return e;
    }

    /** Name of the engine currently in use (useful for verifying AUTO switching). */
    public String engineName() { return requireEngine().name(); }

    public boolean contains(String text) { return !findAll(text).isEmpty(); }

    public List<MatchResult> findAll(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        if (text.length() > config.getMaxTextLength()) {
            throw new IllegalArgumentException("text length " + text.length()
                    + " exceeds maxTextLength=" + config.getMaxTextLength());
        }
        Engine engine = requireEngine();
        String normalized = normalizer.normalize(text);
        List<MatchResult> raw = engine.match(normalized);
        List<MatchResult> result = new ArrayList<>(raw.size());
        for (MatchResult m : raw) {
            if (config.isWordBoundary() && !boundaryOk(normalized, m.getStart(), m.getEnd())) {
                continue;
            }
            String original = text.substring(m.getStart(), m.getEnd());
            result.add(new MatchResult(original, m.getStart(), m.getEnd(), m.getEngine()));
        }
        return result;
    }

    /** "ass" must not match "class": only enforce boundaries for ASCII word-like hits. */
    private boolean boundaryOk(String text, int start, int end) {
        String matched = text.substring(start, end);
        if (!isAsciiWord(matched)) return true; // Chinese / mixed -> no boundary enforcement
        if (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) return false;
        if (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) return false;
        return true;
    }

    private static boolean isAsciiWord(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean asciiLetterOrDigit = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
            if (!asciiLetterOrDigit) return false;
        }
        return true;
    }

    public String replace(String text, ReplaceMode mode) {
        if (text == null || text.isEmpty()) return text;
        List<MatchResult> matches = findAll(text);
        if (matches.isEmpty()) return text;
        // longest hit at each start wins; skip overlapping shorter hits
        matches.sort((a, b) -> {
            int c = Integer.compare(a.getStart(), b.getStart());
            if (c != 0) return c;
            return Integer.compare(b.getEnd(), a.getEnd());
        });
        StringBuilder sb = new StringBuilder(text.length());
        int cursor = 0;
        for (MatchResult m : matches) {
            if (m.getStart() < cursor) continue;
            sb.append(text, cursor, m.getStart());
            if (mode == ReplaceMode.WORD) {
                sb.append("***");
            } else {
                for (int i = m.getStart(); i < m.getEnd(); i++) sb.append('*');
            }
            cursor = m.getEnd();
        }
        sb.append(text, cursor, text.length());
        return sb.toString();
    }
}
