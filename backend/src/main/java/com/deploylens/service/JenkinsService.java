package com.deploylens.service;

import com.deploylens.config.JenkinsConfig;
import com.deploylens.model.BuildInfo;
import com.deploylens.model.PipelineJob;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsService {

    private final RestTemplate jenkinsRestTemplate;
    private final JenkinsConfig jenkinsConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int RECENT_BUILDS_LIMIT = 10;

    public List<PipelineJob> fetchAllJobs() {
        try {
            String url = jenkinsConfig.getBaseUrl()
                    + "/api/json?tree=jobs[name,url,color,lastBuild[number,result,timestamp,duration]]";
            String response = jenkinsRestTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode jobsNode = root.path("jobs");

            List<PipelineJob> jobs = new ArrayList<>();
            for (JsonNode jobNode : jobsNode) {
                PipelineJob job = parseJob(jobNode);
                List<BuildInfo> builds = fetchRecentBuilds(job.getName());
                job.setRecentBuilds(builds);
                enrichJobStats(job, builds);
                jobs.add(job);
            }
            return jobs;
        } catch (Exception e) {
            log.error("Failed to fetch Jenkins jobs: {}", e.getMessage());
            return getMockJobs(); // fallback to mock data when Jenkins is unreachable
        }
    }

    private PipelineJob parseJob(JsonNode node) {
        String name = node.path("name").asText();
        String url = node.path("url").asText();
        String color = node.path("color").asText("grey");

        JsonNode lastBuild = node.path("lastBuild");
        String result = lastBuild.isMissingNode() ? "UNKNOWN" : lastBuild.path("result").asText("IN_PROGRESS");
        int buildNum = lastBuild.isMissingNode() ? 0 : lastBuild.path("number").asInt();
        long ts = lastBuild.isMissingNode() ? 0 : lastBuild.path("timestamp").asLong();
        long dur = lastBuild.isMissingNode() ? 0 : lastBuild.path("duration").asLong();

        return PipelineJob.builder()
                .name(name)
                .url(url)
                .color(color)
                .lastBuildStatus(result)
                .lastBuildNumber(buildNum)
                .lastBuildTimestamp(ts)
                .lastBuildDuration(dur)
                .build();
    }

    public List<BuildInfo> fetchRecentBuilds(String jobName) {
        try {
            String url = jenkinsConfig.getBaseUrl() + "/job/" + jobName
                    + "/api/json?tree=builds[number,result,timestamp,duration,url]{0," + RECENT_BUILDS_LIMIT + "}";
            String response = jenkinsRestTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            return StreamSupport.stream(root.path("builds").spliterator(), false)
                    .map(b -> BuildInfo.builder()
                            .number(b.path("number").asInt())
                            .result(b.path("result").asText("IN_PROGRESS"))
                            .timestamp(b.path("timestamp").asLong())
                            .duration(b.path("duration").asLong())
                            .jobName(jobName)
                            .url(b.path("url").asText())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch builds for job {}: {}", jobName, e.getMessage());
            return Collections.emptyList();
        }
    }

    private void enrichJobStats(PipelineJob job, List<BuildInfo> builds) {
        int success = (int) builds.stream().filter(b -> "SUCCESS".equals(b.getResult())).count();
        int failure = (int) builds.stream().filter(b -> "FAILURE".equals(b.getResult())).count();
        double rate = builds.isEmpty() ? 0 : (double) success / builds.size() * 100;
        long avgDur = (long) builds.stream().mapToLong(BuildInfo::getDuration).filter(d -> d > 0).average().orElse(0);

        job.setTotalBuilds(builds.size());
        job.setSuccessCount(success);
        job.setFailureCount(failure);
        job.setSuccessRate(Math.round(rate * 10.0) / 10.0);
        job.setLastBuildDuration(avgDur);
    }

    // --- Mock data for demo without a live Jenkins instance ---
    public List<PipelineJob> getMockJobs() {
        long now = System.currentTimeMillis();

        List<BuildInfo> buildsA = List.of(
                BuildInfo.builder().number(42).result("SUCCESS").timestamp(now - 3600000).duration(120000).jobName("frontend-deploy").build(),
                BuildInfo.builder().number(41).result("FAILURE").timestamp(now - 7200000).duration(95000).jobName("frontend-deploy").build(),
                BuildInfo.builder().number(40).result("SUCCESS").timestamp(now - 10800000).duration(110000).jobName("frontend-deploy").build(),
                BuildInfo.builder().number(39).result("SUCCESS").timestamp(now - 14400000).duration(115000).jobName("frontend-deploy").build(),
                BuildInfo.builder().number(38).result("FAILURE").timestamp(now - 18000000).duration(88000).jobName("frontend-deploy").build()
        );

        List<BuildInfo> buildsB = List.of(
                BuildInfo.builder().number(88).result("SUCCESS").timestamp(now - 1800000).duration(240000).jobName("backend-api-build").build(),
                BuildInfo.builder().number(87).result("SUCCESS").timestamp(now - 5400000).duration(230000).jobName("backend-api-build").build(),
                BuildInfo.builder().number(86).result("SUCCESS").timestamp(now - 9000000).duration(245000).jobName("backend-api-build").build(),
                BuildInfo.builder().number(85).result("UNSTABLE").timestamp(now - 12600000).duration(210000).jobName("backend-api-build").build(),
                BuildInfo.builder().number(84).result("SUCCESS").timestamp(now - 16200000).duration(235000).jobName("backend-api-build").build()
        );

        List<BuildInfo> buildsC = List.of(
                BuildInfo.builder().number(15).result("FAILURE").timestamp(now - 900000).duration(60000).jobName("db-migration").build(),
                BuildInfo.builder().number(14).result("FAILURE").timestamp(now - 4500000).duration(55000).jobName("db-migration").build(),
                BuildInfo.builder().number(13).result("SUCCESS").timestamp(now - 8100000).duration(75000).jobName("db-migration").build()
        );

        List<BuildInfo> buildsD = List.of(
                BuildInfo.builder().number(200).result("SUCCESS").timestamp(now - 600000).duration(180000).jobName("integration-tests").build(),
                BuildInfo.builder().number(199).result("SUCCESS").timestamp(now - 4200000).duration(175000).jobName("integration-tests").build(),
                BuildInfo.builder().number(198).result("SUCCESS").timestamp(now - 7800000).duration(190000).jobName("integration-tests").build()
        );

        PipelineJob jobA = PipelineJob.builder()
                .name("frontend-deploy").url("http://jenkins/job/frontend-deploy").color("blue")
                .lastBuildStatus("SUCCESS").lastBuildNumber(42).lastBuildTimestamp(now - 3600000)
                .lastBuildDuration(120000).totalBuilds(5).successCount(3).failureCount(2).successRate(60.0)
                .recentBuilds(buildsA).build();

        PipelineJob jobB = PipelineJob.builder()
                .name("backend-api-build").url("http://jenkins/job/backend-api-build").color("blue")
                .lastBuildStatus("SUCCESS").lastBuildNumber(88).lastBuildTimestamp(now - 1800000)
                .lastBuildDuration(240000).totalBuilds(5).successCount(4).failureCount(0).successRate(80.0)
                .recentBuilds(buildsB).build();

        PipelineJob jobC = PipelineJob.builder()
                .name("db-migration").url("http://jenkins/job/db-migration").color("red")
                .lastBuildStatus("FAILURE").lastBuildNumber(15).lastBuildTimestamp(now - 900000)
                .lastBuildDuration(60000).totalBuilds(3).successCount(1).failureCount(2).successRate(33.3)
                .recentBuilds(buildsC).build();

        PipelineJob jobD = PipelineJob.builder()
                .name("integration-tests").url("http://jenkins/job/integration-tests").color("blue")
                .lastBuildStatus("SUCCESS").lastBuildNumber(200).lastBuildTimestamp(now - 600000)
                .lastBuildDuration(180000).totalBuilds(3).successCount(3).failureCount(0).successRate(100.0)
                .recentBuilds(buildsD).build();

        return List.of(jobA, jobB, jobC, jobD);
    }
}
