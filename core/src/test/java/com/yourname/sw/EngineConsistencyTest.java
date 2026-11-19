package com.yourname.sw;

import com.yourname.sw.engine.AhoCorasickEngine;
import com.yourname.sw.engine.DfaTrieEngine;
import com.yourname.sw.engine.Engine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineConsistencyTest {

    private static List<String> signatures(List<MatchResult> ms) {
        List<String> out = new ArrayList<>();
        for (MatchResult m : ms) out.add(m.getWord() + ":" + m.getStart() + ":" + m.getEnd());
        return out;
    }

    private static Engine build(Engine e, String... words) {
        for (String w : words) e.addWord(w);
        e.build();
        return e;
    }

    @Test
    void bothEnginesReportTheSameMatchesWithFailureLinks() {
        String[] words = {"he", "she", "hers", "his"};
        String text = "ushers"; // she[1,4) he[2,4) hers[2,6)

        List<String> dfa = signatures(build(new DfaTrieEngine(), words).match(text));
        List<String> ac = signatures(build(new AhoCorasickEngine(), words).match(text));

        assertTrue(dfa.contains("she:1:4"));
        assertTrue(dfa.contains("he:2:4"));
        assertTrue(dfa.contains("hers:2:6"));
        assertEquals(3, dfa.size());

        Collections.sort(dfa);
        Collections.sort(ac);
        assertEquals(dfa, ac, "DFA Trie and Aho-Corasick must agree");
    }

    @Test
    void overlappedAndSuffixWordsAreAllFound() {
        Engine ac = build(new AhoCorasickEngine(), "a", "aa", "aaa");
        assertEquals(6, ac.match("aaa").size()); // 3 at pos0 + 2 at pos1 + 1 at pos2
    }
}
