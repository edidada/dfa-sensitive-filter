package com.yourname.sw.starter;

import com.yourname.sw.SensitiveWordFilter;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveWordAutoConfigurationTest {

    @Test
    void createsConfiguredFilterBean() {
        SensitiveWordProperties props = new SensitiveWordProperties();
        props.setWords(Collections.singletonList("bad"));
        props.setIgnoreCase(true);

        SensitiveWordAutoConfiguration cfg = new SensitiveWordAutoConfiguration();
        SensitiveWordFilter filter = cfg.sensitiveWordFilter(props);

        assertTrue(filter.contains("BAD"));
        assertTrue(filter.contains("bad"));
    }
}
