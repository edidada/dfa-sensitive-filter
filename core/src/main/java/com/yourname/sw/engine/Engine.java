package com.yourname.sw.engine;

import com.yourname.sw.MatchResult;

import java.util.List;

/**
 * Matching engine contract. An engine is built once from a normalized dictionary and is
 * immutable afterwards, so {@code match} is lock-free and safe for concurrent use.
 * Input to {@code match} is already normalized.
 */
public interface Engine {
    void addWord(String normalizedWord);
    void build();
    List<MatchResult> match(String normalizedText);
    String name();
}
