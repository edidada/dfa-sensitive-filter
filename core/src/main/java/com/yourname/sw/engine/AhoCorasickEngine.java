package com.yourname.sw.engine;

import com.yourname.sw.MatchResult;
import com.yourname.sw.ac.AcNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Aho-Corasick with BFS-built failure links. O(n + matches). Default for large dictionaries.
 * Failure outputs are merged at build time so suffix matches are reported inline without
 * walking the failure chain during matching.
 */
public final class AhoCorasickEngine implements Engine {

    public static final String NAME = "Aho-Corasick";
    private final AcNode root = new AcNode();
    private boolean built = false;

    @Override
    public void addWord(String normalizedWord) {
        if (normalizedWord == null || normalizedWord.isEmpty()) return;
        AcNode cur = root;
        for (int i = 0; i < normalizedWord.length(); i++) {
            char c = normalizedWord.charAt(i);
            cur = cur.getNext().computeIfAbsent(c, k -> new AcNode());
        }
        cur.getOutput().add(normalizedWord);
    }

    @Override
    public void build() {
        Deque<AcNode> queue = new ArrayDeque<>();
        root.setFail(root);
        for (AcNode child : root.getNext().values()) {
            child.setFail(root);
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            AcNode node = queue.poll();
            for (Map.Entry<Character, AcNode> e : node.getNext().entrySet()) {
                char c = e.getKey();
                AcNode child = e.getValue();
                AcNode f = node.getFail();
                while (f != root && !f.getNext().containsKey(c)) {
                    f = f.getFail();
                }
                AcNode candidate = f.getNext().get(c);
                child.setFail(candidate != null && candidate != child ? candidate : root);
                // inherit outputs of the failure node (suffix words)
                child.getOutput().addAll(child.getFail().getOutput());
                queue.add(child);
            }
        }
        built = true;
    }

    @Override
    public List<MatchResult> match(String text) {
        List<MatchResult> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;
        if (!built) throw new IllegalStateException("build() must be called before match()");
        AcNode state = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (state != root && !state.getNext().containsKey(c)) {
                state = state.getFail();
            }
            AcNode next = state.getNext().get(c);
            state = (next != null) ? next : root;
            for (String w : state.getOutput()) {
                int start = i - w.length() + 1;
                out.add(new MatchResult(w, start, i + 1, NAME));
            }
        }
        return out;
    }

    @Override
    public String name() { return NAME; }
}
