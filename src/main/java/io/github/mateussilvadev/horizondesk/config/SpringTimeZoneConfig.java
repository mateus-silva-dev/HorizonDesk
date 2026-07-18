package io.github.mateussilvadev.horizondesk.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.TimeZone;

@Configuration
public class SpringTimeZoneConfig {

    @PostConstruct
    public void timeZoneConfig() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
