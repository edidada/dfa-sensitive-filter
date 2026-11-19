package com.yourname.sw.engine;

import com.yourname.sw.MatchResult;
import com.yourname.sw.trie.TrieNode;

import java.util.ArrayList;
import java.util.List;

/** Trie walk from every start position. O(n * L). Best for small dictionaries. */
public final class DfaTrieEngine implements Engine {

    public static final String NAME = "DFA-Trie";
    private final TrieNode root = new TrieNode();

    @Override
    public void addWord(String normalizedWord) {
        if (normalizedWord == null || normalizedWord.isEmpty()) return;
        TrieNode cur = root;
        for (int i = 0; i < normalizedWord.length(); i++) {
            char c = normalizedWord.charAt(i);
            cur = cur.getChildren().computeIfAbsent(c, k -> new TrieNode());
        }
        cur.setTerminal(true);
        cur.setWord(normalizedWord);
    }

    @Override
    public void build() { /* nothing to precompute for a plain trie */ }

    @Override
    public List<MatchResult> match(String text) {
        List<MatchResult> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;
        for (int i = 0; i < text.length(); i++) {
            TrieNode cur = root;
            for (int j = i; j < text.length(); j++) {
                TrieNode next = cur.getChildren().get(text.charAt(j));
                if (next == null) break;
                cur = next;
                if (cur.isTerminal()) {
                    out.add(new MatchResult(cur.getWord(), i, j + 1, NAME));
                }
            }
        }
        return out;
    }

    @Override
    public String name() { return NAME; }
}
