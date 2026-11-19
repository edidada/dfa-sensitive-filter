package com.yourname.sw.demo;

import com.yourname.sw.MatchResult;
import com.yourname.sw.ReplaceMode;
import com.yourname.sw.SensitiveWordFilter;

import java.util.Arrays;

/** Minimal runnable usage example. */
public final class Demo {

    public static void main(String[] args) {
        SensitiveWordFilter filter = new SensitiveWordFilter();
        filter.init(Arrays.asList("badword", "敏感词", "赌博"));
        String text = "这是一个 BadWord，还包含敏感词，禁止赌博。";

        System.out.println("engine   : " + filter.engineName());
        System.out.println("contains : " + filter.contains(text));
        for (MatchResult m : filter.findAll(text)) {
            System.out.println("hit      : " + m);
        }
        System.out.println("masked   : " + filter.replace(text, ReplaceMode.CHAR));
    }
}
