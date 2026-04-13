package com.deploylens.scheduler;

import com.deploylens.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCollector {

    private final MetricsService metricsService;

    @Value("${jenkins.poll-interval-ms:30000}")
    private long pollIntervalMs;

    // Runs every 30 seconds by default (configurable via application.properties)
    @Scheduled(fixedRateString = "${jenkins.poll-interval-ms:30000}")
    public void collect() {
        log.debug("Scheduled metrics collection triggered");
        metricsService.collectAndCache();
    }
}
