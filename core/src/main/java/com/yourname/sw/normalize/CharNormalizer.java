package com.yourname.sw.normalize;

import com.yourname.sw.SensitiveWordConfig;

/**
 * Length-preserving character normalization applied once to both the dictionary and the
 * input text before matching. Because every input char maps to exactly one output char,
 * match indices remain valid against the original text.
 *
 * <p>Full-width ASCII (U+FF01..U+FF5E) is folded to half-width ASCII, the ideographic
 * space (U+3000) to a normal space, and (optionally) letters are lower-cased. Traditional
 * -&gt; Simplified conversion is intentionally left out: it is not a 1:1 mapping and would
 * break index stability; it can be plugged in here as a pre-step if needed.</p>
 */
public final class CharNormalizer {

    private final boolean ignoreCase;
    private final boolean fullWidthNormalize;

    public CharNormalizer(SensitiveWordConfig config) {
        this.ignoreCase = config.isIgnoreCase();
        this.fullWidthNormalize = config.isFullWidthNormalize();
    }

    public String normalize(String input) {
        if (input == null || input.isEmpty()) return input;
        char[] out = input.toCharArray();
        for (int i = 0; i < out.length; i++) {
            out[i] = normalizeChar(out[i]);
        }
        return new String(out);
    }

    private char normalizeChar(char c) {
        if (fullWidthNormalize) {
            if (c >= 0xFF01 && c <= 0xFF5E) {
                c = (char) (c - 0xFEE0);
            } else if (c == 0x3000) {
                c = ' ';
            }
        }
        if (ignoreCase) {
            c = Character.toLowerCase(c);
        }
        return c;
    }
}
