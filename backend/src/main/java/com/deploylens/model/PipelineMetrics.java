package com.deploylens.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineMetrics {
    private Instant collectedAt;
    private int totalJobs;
    private int healthyJobs;
    private int failingJobs;
    private int unstableJobs;
    private double overallSuccessRate;
    private long averageBuildDuration;
    private List<PipelineJob> jobs;
    private List<BuildInfo> recentFailures;
    private Map<String, Integer> buildCountByDay;  // last 7 days
}
