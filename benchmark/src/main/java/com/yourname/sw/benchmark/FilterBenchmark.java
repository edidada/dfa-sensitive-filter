package com.yourname.sw.benchmark;

import com.yourname.sw.SensitiveWordFilter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Compares the Aho-Corasick filter against two naive per-word approaches.
 * See docs/benchmark.md for methodology and reference numbers.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class FilterBenchmark {

    @Param({"100", "1000"})
    public int wordCount;

    private SensitiveWordFilter filter;
    private List<String> words;
    private List<Pattern> patterns;
    private String text;

    @Setup(Level.Trial)
    public void setup() {
        words = new ArrayList<>();
        for (int i = 0; i < wordCount; i++) words.add("sensitive" + i);

        filter = new SensitiveWordFilter();
        filter.init(words);

        // Compiled once (best case for regex); still O(k * n) and backtracking-prone.
        patterns = new ArrayList<>();
        for (String w : words) patterns.add(Pattern.compile(Pattern.quote(w)));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("lorem ipsum dolor sit amet ");
            if (i % 10 == 0) sb.append(words.get(i % wordCount)).append(' ');
        }
        text = sb.toString();
    }

    @Benchmark
    public int acFindAll() {
        return filter.findAll(text).size();
    }

    @Benchmark
    public void stringContainsPerWord(Blackhole bh) {
        for (String w : words) {
            if (text.contains(w)) bh.consume(w);
        }
    }

    @Benchmark
    public void regexPerWord(Blackhole bh) {
        for (Pattern p : patterns) {
            if (p.matcher(text).find()) bh.consume(p);
        }
    }
}
