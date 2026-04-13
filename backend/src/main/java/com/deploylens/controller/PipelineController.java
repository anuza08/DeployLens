package com.deploylens.controller;

import com.deploylens.model.BuildInfo;
import com.deploylens.model.PipelineJob;
import com.deploylens.model.PipelineMetrics;
import com.deploylens.service.JenkinsService;
import com.deploylens.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final MetricsService metricsService;
    private final JenkinsService jenkinsService;

    // GET /api/v1/pipelines/metrics  — summary metrics for dashboard header
    @GetMapping("/metrics")
    public ResponseEntity<PipelineMetrics> getMetrics() {
        return ResponseEntity.ok(metricsService.getCachedMetrics());
    }

    // GET /api/v1/pipelines/jobs  — all pipeline jobs list
    @GetMapping("/jobs")
    public ResponseEntity<List<PipelineJob>> getJobs() {
        return ResponseEntity.ok(metricsService.getCachedMetrics().getJobs());
    }

    // GET /api/v1/pipelines/jobs/{name}/builds  — builds for a specific job
    @GetMapping("/jobs/{name}/builds")
    public ResponseEntity<List<BuildInfo>> getBuilds(@PathVariable String name) {
        List<BuildInfo> builds = jenkinsService.fetchRecentBuilds(name);
        if (builds.isEmpty()) {
            // fallback to cached
            return metricsService.getCachedMetrics().getJobs().stream()
                    .filter(j -> j.getName().equals(name))
                    .findFirst()
                    .map(j -> ResponseEntity.ok(j.getRecentBuilds()))
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.ok(builds);
    }

    // GET /api/v1/pipelines/failures  — recent failures across all jobs
    @GetMapping("/failures")
    public ResponseEntity<List<BuildInfo>> getRecentFailures() {
        return ResponseEntity.ok(metricsService.getCachedMetrics().getRecentFailures());
    }

    // POST /api/v1/pipelines/refresh  — trigger immediate refresh
    @PostMapping("/refresh")
    public ResponseEntity<PipelineMetrics> refresh() {
        return ResponseEntity.ok(metricsService.collectAndCache());
    }

    // GET /api/v1/pipelines/health  — simple health summary map
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        PipelineMetrics m = metricsService.getCachedMetrics();
        return ResponseEntity.ok(Map.of(
                "totalJobs", m.getTotalJobs(),
                "healthyJobs", m.getHealthyJobs(),
                "failingJobs", m.getFailingJobs(),
                "unstableJobs", m.getUnstableJobs(),
                "overallSuccessRate", m.getOverallSuccessRate(),
                "collectedAt", m.getCollectedAt().toString()
        ));
    }
}
