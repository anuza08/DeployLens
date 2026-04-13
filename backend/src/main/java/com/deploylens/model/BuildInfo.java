package com.deploylens.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildInfo {
    private int number;
    private String result;          // SUCCESS, FAILURE, UNSTABLE, ABORTED, IN_PROGRESS
    private long timestamp;
    private long duration;
    private String jobName;
    private String url;
    private String triggeredBy;
    private String branch;
}
