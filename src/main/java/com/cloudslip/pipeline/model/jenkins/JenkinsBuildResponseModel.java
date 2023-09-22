package com.cloudslip.pipeline.model.jenkins;

import java.util.Date;

public class JenkinsBuildResponseModel {

    private String appName;
    private String buildId;
    private Long actualDuration;
    private Long estimatedDuration;
    private Date timeStarted;
    private String gitCommitId;
    private String currentJenkinsBuildUrl;
    private String status;
    private String currentJobName;
    private String currentBuildStatusUrl;
    private String nextBuildStatusUrl;
    private String consoleLogUrl;
    private PipelineStep pipelineStep;

    public String getAppName() {
        return appName;
    }

    public void setAppName(final String appName) {
        this.appName = appName;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(final String buildId) {
        this.buildId = buildId;
    }

    public Long getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(final Long actualDuration) {
        this.actualDuration = actualDuration;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(final Long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Date getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(final Date timeStarted) {
        this.timeStarted = timeStarted;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public void setGitCommitId(final String gitCommitId) {
        this.gitCommitId = gitCommitId;
    }

    public String getCurrentJenkinsBuildUrl() {
        return currentJenkinsBuildUrl;
    }

    public void setCurrentJenkinsBuildUrl(final String currentJenkinsBuildUrl) {
        this.currentJenkinsBuildUrl = currentJenkinsBuildUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getCurrentJobName() {
        return currentJobName;
    }

    public void setCurrentJobName(final String currentJobName) {
        this.currentJobName = currentJobName;
    }

    public String getCurrentBuildStatusUrl() {
        return currentBuildStatusUrl;
    }

    public void setCurrentBuildStatusUrl(final String currentBuildStatusUrl) {
        this.currentBuildStatusUrl = currentBuildStatusUrl;
    }

    public String getNextBuildStatusUrl() {
        return nextBuildStatusUrl;
    }

    public void setNextBuildStatusUrl(final String nextBuildStatusUrl) {
        this.nextBuildStatusUrl = nextBuildStatusUrl;
    }

    public String getConsoleLogUrl() {
        return consoleLogUrl;
    }

    public void setConsoleLogUrl(final String consoleLogUrl) {
        this.consoleLogUrl = consoleLogUrl;
    }

    public PipelineStep getPipelineStep() {
        return pipelineStep;
    }

    public void setPipelineStep(final PipelineStep pipelineStep) {
        this.pipelineStep = pipelineStep;
    }
}
