package com.cloudslip.pipeline.updated.dto;

import java.io.Serializable;

public class PipelineStepStatusWebSocketData implements Serializable {

    private String gitCommitId;
    private String appCommitPipelineStepId;
    private String log;
    private Long estimatedTime;

    public PipelineStepStatusWebSocketData() {
    }

    public PipelineStepStatusWebSocketData(String gitCommitId, String appCommitPipelineStepId, String log, Long estimatedTime) {
        this.gitCommitId = gitCommitId;
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.log = log;
        this.estimatedTime = estimatedTime;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public void setGitCommitId(String gitCommitId) {
        this.gitCommitId = gitCommitId;
    }

    public String getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(String appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setValues(String gitCommitId, String appCommitPipelineStepId, String log) {
        this.gitCommitId = gitCommitId;
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.log = log;
    }

    public void setValues(String gitCommitId, String appCommitPipelineStepId, String log, Long estimatedTime) {
        this.gitCommitId = gitCommitId;
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.log = log;
        this.estimatedTime = estimatedTime;
    }
}
