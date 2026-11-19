package com.yourname.sw;

/** Immutable configuration for {@link SensitiveWordFilter}. */
public final class SensitiveWordConfig {

    public enum EngineType { AUTO, DFA, AC }

    /** Below this dictionary size AUTO mode selects the DFA Trie engine. */
    public static final int AUTO_DFA_THRESHOLD = 100;

    private final boolean ignoreCase;
    private final boolean fullWidthNormalize;
    private final boolean wordBoundary;
    private final EngineType engineType;
    private final int maxTextLength;

    private SensitiveWordConfig(Builder b) {
        this.ignoreCase = b.ignoreCase;
        this.fullWidthNormalize = b.fullWidthNormalize;
        this.wordBoundary = b.wordBoundary;
        this.engineType = b.engineType;
        this.maxTextLength = b.maxTextLength;
    }

    public static SensitiveWordConfig defaults() { return builder().build(); }

    public static Builder builder() { return new Builder(); }

    public boolean isIgnoreCase() { return ignoreCase; }
    public boolean isFullWidthNormalize() { return fullWidthNormalize; }
    public boolean isWordBoundary() { return wordBoundary; }
    public EngineType getEngineType() { return engineType; }
    public int getMaxTextLength() { return maxTextLength; }

    public static final class Builder {
        private boolean ignoreCase = true;
        private boolean fullWidthNormalize = true;
        private boolean wordBoundary = false;
        private EngineType engineType = EngineType.AUTO;
        private int maxTextLength = 1_000_000;

        public Builder ignoreCase(boolean v) { this.ignoreCase = v; return this; }
        public Builder fullWidthNormalize(boolean v) { this.fullWidthNormalize = v; return this; }
        public Builder wordBoundary(boolean v) { this.wordBoundary = v; return this; }
        public Builder engineType(EngineType v) { this.engineType = v; return this; }
        public Builder maxTextLength(int v) { this.maxTextLength = v; return this; }
        public SensitiveWordConfig build() { return new SensitiveWordConfig(this); }
    }
}
