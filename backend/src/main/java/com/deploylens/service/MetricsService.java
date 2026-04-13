package com.deploylens.service;

import com.deploylens.model.BuildInfo;
import com.deploylens.model.PipelineJob;
import com.deploylens.model.PipelineMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetricsService {

    private final JenkinsService jenkinsService;
    private final MeterRegistry meterRegistry;

    // Atomic holders so Gauge can read latest values
    private final AtomicInteger totalJobs = new AtomicInteger(0);
    private final AtomicInteger healthyJobs = new AtomicInteger(0);
    private final AtomicInteger failingJobs = new AtomicInteger(0);
    private final AtomicReference<Double> successRate = new AtomicReference<>(0.0);
    private final AtomicReference<PipelineMetrics> cachedMetrics = new AtomicReference<>();

    public MetricsService(JenkinsService jenkinsService, MeterRegistry meterRegistry) {
        this.jenkinsService = jenkinsService;
        this.meterRegistry = meterRegistry;
        registerGauges();
    }

    private void registerGauges() {
        Gauge.builder("deploylens.jobs.total", totalJobs, AtomicInteger::get)
                .description("Total number of Jenkins pipeline jobs")
                .register(meterRegistry);

        Gauge.builder("deploylens.jobs.healthy", healthyJobs, AtomicInteger::get)
                .description("Number of healthy (last build SUCCESS) jobs")
                .register(meterRegistry);

        Gauge.builder("deploylens.jobs.failing", failingJobs, AtomicInteger::get)
                .description("Number of failing jobs")
                .register(meterRegistry);

        Gauge.builder("deploylens.success.rate", successRate, ref -> ref.get())
                .description("Overall pipeline success rate (%)")
                .register(meterRegistry);
    }

    public PipelineMetrics collectAndCache() {
        log.info("Collecting Jenkins pipeline metrics...");
        List<PipelineJob> jobs = jenkinsService.fetchAllJobs();
        PipelineMetrics metrics = buildMetrics(jobs);
        updateGauges(metrics);
        cachedMetrics.set(metrics);
        log.info("Metrics collected: {} jobs, {}% success rate",
                metrics.getTotalJobs(), metrics.getOverallSuccessRate());
        return metrics;
    }

    public PipelineMetrics getCachedMetrics() {
        PipelineMetrics m = cachedMetrics.get();
        if (m == null) {
            return collectAndCache();
        }
        return m;
    }

    private PipelineMetrics buildMetrics(List<PipelineJob> jobs) {
        int healthy = (int) jobs.stream().filter(j -> "SUCCESS".equals(j.getLastBuildStatus())).count();
        int failing = (int) jobs.stream().filter(j -> "FAILURE".equals(j.getLastBuildStatus())).count();
        int unstable = (int) jobs.stream().filter(j -> "UNSTABLE".equals(j.getLastBuildStatus())).count();

        double overall = jobs.stream()
                .mapToDouble(PipelineJob::getSuccessRate)
                .average().orElse(0.0);

        long avgDur = (long) jobs.stream()
                .mapToLong(PipelineJob::getLastBuildDuration)
                .filter(d -> d > 0)
                .average().orElse(0.0);

        List<BuildInfo> recentFailures = jobs.stream()
                .flatMap(j -> j.getRecentBuilds().stream())
                .filter(b -> "FAILURE".equals(b.getResult()))
                .sorted(Comparator.comparingLong(BuildInfo::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return PipelineMetrics.builder()
                .collectedAt(Instant.now())
                .totalJobs(jobs.size())
                .healthyJobs(healthy)
                .failingJobs(failing)
                .unstableJobs(unstable)
                .overallSuccessRate(Math.round(overall * 10.0) / 10.0)
                .averageBuildDuration(avgDur)
                .jobs(jobs)
                .recentFailures(recentFailures)
                .buildCountByDay(buildCountByDay(jobs))
                .build();
    }

    private Map<String, Integer> buildCountByDay(List<PipelineJob> jobs) {
        // Aggregate builds per day (simple last-7-days buckets)
        Map<String, Integer> counts = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        for (int i = 6; i >= 0; i--) {
            long dayStart = now - (long) i * 86400000;
            long dayEnd = dayStart + 86400000;
            String label = "Day-" + (i == 0 ? "Today" : i);
            int count = (int) jobs.stream()
                    .flatMap(j -> j.getRecentBuilds().stream())
                    .filter(b -> b.getTimestamp() >= dayStart && b.getTimestamp() < dayEnd)
                    .count();
            counts.put(label, count);
        }
        return counts;
    }

    private void updateGauges(PipelineMetrics m) {
        totalJobs.set(m.getTotalJobs());
        healthyJobs.set(m.getHealthyJobs());
        failingJobs.set(m.getFailingJobs());
        successRate.set(m.getOverallSuccessRate());
    }
}
