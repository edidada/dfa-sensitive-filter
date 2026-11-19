package com.yourname.sw.ac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Node of the Aho-Corasick automaton. */
public final class AcNode {
    private final Map<Character, AcNode> next = new HashMap<>();
    private AcNode fail;
    private final List<String> output = new ArrayList<>();

    public Map<Character, AcNode> getNext() { return next; }
    public AcNode getFail() { return fail; }
    public void setFail(AcNode fail) { this.fail = fail; }
    /** Words ending exactly here, plus words inherited from the failure chain. */
    public List<String> getOutput() { return output; }
}
