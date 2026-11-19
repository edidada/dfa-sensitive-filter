package com.yourname.sw;

import java.util.Objects;

/**
 * A single sensitive-word hit.
 *
 * <p>Indices follow the usual half-open convention: {@code [start, end)} refers to the
 * original (un-normalized) input text, so {@code text.substring(start, end)} returns
 * exactly the matched fragment. This is possible because normalization is strictly
 * length-preserving (one input char maps to one normalized char).</p>
 */
public final class MatchResult {

    private final String word;
    private final int start;
    private final int end;
    private final String engine;

    public MatchResult(String word, int start, int end, String engine) {
        this.word = word;
        this.start = start;
        this.end = end;
        this.engine = engine;
    }

    /** The matched fragment taken verbatim from the original input text. */
    public String getWord() { return word; }

    /** Inclusive start index into the original input text. */
    public int getStart() { return start; }

    /** Exclusive end index into the original input text. */
    public int getEnd() { return end; }

    /** Name of the engine that produced this hit. */
    public String getEngine() { return engine; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult)) return false;
        MatchResult that = (MatchResult) o;
        return start == that.start && end == that.end
                && Objects.equals(word, that.word)
                && Objects.equals(engine, that.engine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, start, end, engine);
    }

    @Override
    public String toString() {
        return "MatchResult{word='" + word + "', start=" + start
                + ", end=" + end + ", engine='" + engine + "'}";
    }
}
