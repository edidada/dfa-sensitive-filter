package com.yourname.sw.trie;

import java.util.HashMap;
import java.util.Map;

/** Node of the DFA Trie used by {@code DfaTrieEngine}. */
public final class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean terminal;
    private String word;

    public Map<Character, TrieNode> getChildren() { return children; }
    public boolean isTerminal() { return terminal; }
    public void setTerminal(boolean terminal) { this.terminal = terminal; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
}
