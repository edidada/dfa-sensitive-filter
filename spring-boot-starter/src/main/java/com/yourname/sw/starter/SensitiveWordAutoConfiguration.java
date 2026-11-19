package com.yourname.sw.starter;

import com.yourname.sw.SensitiveWordConfig;
import com.yourname.sw.SensitiveWordFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Auto-configures a ready-to-use {@link SensitiveWordFilter} bean. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SensitiveWordFilter.class)
@EnableConfigurationProperties(SensitiveWordProperties.class)
public class SensitiveWordAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SensitiveWordFilter sensitiveWordFilter(SensitiveWordProperties props) {
        SensitiveWordConfig config = props.toConfig();
        SensitiveWordFilter filter = new SensitiveWordFilter(config);
        try {
            if (props.getLocation() != null && !props.getLocation().isBlank()) {
                String loc = props.getLocation();
                if (loc.startsWith("classpath:")) {
                    filter.initFromClasspath(loc.substring("classpath:".length()));
                } else {
                    Path p = loc.startsWith("file:")
                            ? Paths.get(loc.substring("file:".length()))
                            : Paths.get(loc);
                    filter.init(p);
                }
            } else {
                filter.init(props.getWords());
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to load sensitive word list", e);
        }
        return filter;
    }
}
