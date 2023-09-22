package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class SaveBuildIdForAppCommitPipelineStepInputDTO extends BaseInputDTO {

    private ObjectId applicationId;

    private String commitId;

    private ObjectId appCommitPipelineStepId;

    private String jenkinsBuildId;

    private Long estimatedTime;

    public SaveBuildIdForAppCommitPipelineStepInputDTO() {
    }

    public SaveBuildIdForAppCommitPipelineStepInputDTO(ObjectId applicationId, String commitId, ObjectId appCommitPipelineStepId, String jenkinsBuildId, Long estimatedTime) {
        this.applicationId = applicationId;
        this.commitId = commitId;
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.jenkinsBuildId = jenkinsBuildId;
        this.estimatedTime = estimatedTime;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }

    public String getJenkinsBuildId() {
        return jenkinsBuildId;
    }

    public void setJenkinsBuildId(String jenkinsBuildId) {
        this.jenkinsBuildId = jenkinsBuildId;
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}
