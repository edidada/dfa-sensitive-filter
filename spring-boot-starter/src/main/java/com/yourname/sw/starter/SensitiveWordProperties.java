package com.yourname.sw.starter;

import com.yourname.sw.SensitiveWordConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** {@code sensitive-word.*} properties bound into a {@link SensitiveWordConfig}. */
@ConfigurationProperties(prefix = "sensitive-word")
public class SensitiveWordProperties {

    /** Inline words. Used when no {@code location} is configured. */
    private List<String> words = new ArrayList<>();

    /** Optional {@code classpath:} or {@code file:} location of a word list. */
    private String location;

    private boolean ignoreCase = true;
    private boolean fullWidthNormalize = true;
    private boolean wordBoundary = false;
    private SensitiveWordConfig.EngineType engineType = SensitiveWordConfig.EngineType.AUTO;
    private int maxTextLength = 1_000_000;

    public SensitiveWordConfig toConfig() {
        return SensitiveWordConfig.builder()
                .ignoreCase(ignoreCase)
                .fullWidthNormalize(fullWidthNormalize)
                .wordBoundary(wordBoundary)
                .engineType(engineType)
                .maxTextLength(maxTextLength)
                .build();
    }

    public List<String> getWords() { return words; }
    public void setWords(List<String> words) { this.words = words; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isIgnoreCase() { return ignoreCase; }
    public void setIgnoreCase(boolean ignoreCase) { this.ignoreCase = ignoreCase; }
    public boolean isFullWidthNormalize() { return fullWidthNormalize; }
    public void setFullWidthNormalize(boolean fullWidthNormalize) { this.fullWidthNormalize = fullWidthNormalize; }
    public boolean isWordBoundary() { return wordBoundary; }
    public void setWordBoundary(boolean wordBoundary) { this.wordBoundary = wordBoundary; }
    public SensitiveWordConfig.EngineType getEngineType() { return engineType; }
    public void setEngineType(SensitiveWordConfig.EngineType engineType) { this.engineType = engineType; }
    public int getMaxTextLength() { return maxTextLength; }
    public void setMaxTextLength(int maxTextLength) { this.maxTextLength = maxTextLength; }
}
