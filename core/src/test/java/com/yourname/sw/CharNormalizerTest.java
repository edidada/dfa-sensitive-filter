package com.yourname.sw;

import com.yourname.sw.normalize.CharNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharNormalizerTest {

    @Test
    void fullWidthIsFoldedAndLengthPreserved() {
        CharNormalizer n = new CharNormalizer(SensitiveWordConfig.defaults());
        assertEquals("abc", n.normalize("ａｂｃ"));
        assertEquals(3, n.normalize("ａｂｃ").length());
        assertEquals(" ", n.normalize("　")); // ideographic space
    }

    @Test
    void caseIsFoldedOnlyWhenEnabled() {
        CharNormalizer folding = new CharNormalizer(SensitiveWordConfig.defaults());
        assertEquals("abc", folding.normalize("AbC"));

        CharNormalizer strict = new CharNormalizer(
                SensitiveWordConfig.builder().ignoreCase(false).build());
        assertEquals("AbC", strict.normalize("AbC"));
    }
}
