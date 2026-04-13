package com.deploylens.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineJob {
    private String name;
    private String url;
    private String color;           // Jenkins color: blue=OK, red=FAIL, yellow=UNSTABLE
    private String lastBuildStatus;
    private int lastBuildNumber;
    private long lastBuildTimestamp;
    private long lastBuildDuration;
    private int totalBuilds;
    private int successCount;
    private int failureCount;
    private double successRate;
    private List<BuildInfo> recentBuilds;
}
