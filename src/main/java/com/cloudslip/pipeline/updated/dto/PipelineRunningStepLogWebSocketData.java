package com.cloudslip.pipeline.updated.dto;

import java.io.Serializable;

public class PipelineRunningStepLogWebSocketData implements Serializable {

    private String gitCommitId;
    private String appCommitPipelineStepId;
    private String log;

    public PipelineRunningStepLogWebSocketData() {
    }

    public PipelineRunningStepLogWebSocketData(String gitCommitId, String appCommitPipelineStepId, String log) {
        this.gitCommitId = gitCommitId;
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.log = log;
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

    public void setValues(String gitCommitId, String appCommitPipelineStepId, String log) {
        this.gitCommitId = gitCommitId;
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.log = log;
    }
}
