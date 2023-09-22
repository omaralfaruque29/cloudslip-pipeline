package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.enums.PipelineStepStatusType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "app_commit_pipeline_step")
public class AppCommitPipelineStep extends BaseEntity {

    @NotNull
    private ObjectId appCommitId;

    @NotNull
    private AppPipelineStep appPipelineStep;

    private boolean enabled = false;

    @NotNull
    private PipelineStepStatusType type = PipelineStepStatusType.NONE;

    private String log;

    private String pipelineStartTime;

    private Long estimatedTime;

    private String jenkinsBuildId;

    private boolean runningAsCanary;


    public AppCommitPipelineStep() {
    }

    public AppCommitPipelineStep(@NotNull ObjectId appCommitId, @NotNull AppPipelineStep appPipelineStep, boolean enabled, @NotNull PipelineStepStatusType type, String log, String pipelineStartTime, Long estimatedTime, String jenkinsBuildId, boolean runningAsCanary) {
        this.appCommitId = appCommitId;
        this.appPipelineStep = appPipelineStep;
        this.enabled = enabled;
        this.type = type;
        this.log = log;
        this.pipelineStartTime = pipelineStartTime;
        this.estimatedTime = estimatedTime;
        this.jenkinsBuildId = jenkinsBuildId;
        this.runningAsCanary = runningAsCanary;
    }

    public AppCommitPipelineStep(AppCommitPipelineStep input) {
        this.setId(input.getObjectId());
        this.setStatus(input.getStatus());
        this.setCreateDate(input.getCreateDate());
        this.setUpdateDate(input.getUpdateDate());
        this.setCreatedBy(input.getCreatedBy());
        this.setUpdatedBy(input.getUpdatedBy());
        this.appCommitId = input.getAppCommitObjectId();
        this.appPipelineStep = input.getAppPipelineStep();
        this.enabled = input.isEnabled();
        this.type = input.getType();
        this.log = input.getLog();
        this.pipelineStartTime = input.getPipelineStartTime();
        this.estimatedTime = input.getEstimatedTime();
        this.jenkinsBuildId = input.getJenkinsBuildId();
        this.runningAsCanary = input.isRunningAsCanary();
    }

    public String getAppCommitId() {
        return appCommitId.toHexString();
    }

    @JsonIgnore
    public ObjectId getAppCommitObjectId() {
        return appCommitId;
    }

    public void setAppCommitId(ObjectId appCommitId) {
        this.appCommitId = appCommitId;
    }

    public AppPipelineStep getAppPipelineStep() {
        return appPipelineStep;
    }

    public void setAppPipelineStep(AppPipelineStep appPipelineStep) {
        this.appPipelineStep = appPipelineStep;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PipelineStepStatusType getType() {
        return type;
    }

    public void setType(PipelineStepStatusType type) {
        this.type = type;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getPipelineStartTime() {
        return pipelineStartTime;
    }

    public void setPipelineStartTime(String pipelineStartTime) {
        this.pipelineStartTime = pipelineStartTime;
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getJenkinsBuildId() {
        return jenkinsBuildId;
    }

    public void setJenkinsBuildId(String jenkinsBuildId) {
        this.jenkinsBuildId = jenkinsBuildId;
    }

    public boolean isRunningAsCanary() {
        return runningAsCanary;
    }

    public void setRunningAsCanary(boolean runningAsCanary) {
        this.runningAsCanary = runningAsCanary;
    }

    @JsonIgnore
    public boolean pipelineStepExitsIn(List<AppCommitPipelineStep> appCommitPipelineStepList) {
        for (AppCommitPipelineStep appCommitPipelineStep : appCommitPipelineStepList) {
            if (this.appPipelineStep.getId().equals(appCommitPipelineStep.getAppPipelineStep().getId())){
                return true;
            }
        }
        return false;
    }

    public boolean isFailed() {
        if(this.type.equals(PipelineStepStatusType.NONE) || this.type.equals(PipelineStepStatusType.RUNNING)
                || this.type.equals(PipelineStepStatusType.PIPELINE_STARTED)
                || this.type.equals(PipelineStepStatusType.CLONING_GIT)
                || this.type.equals(PipelineStepStatusType.GRADLE_BUILDING)
                || this.type.equals(PipelineStepStatusType.MAVEN_BUILDING)
                || this.type.equals(PipelineStepStatusType.BUILDING_IMAGE)
                || this.type.equals(PipelineStepStatusType.REMOVING_UNUSED_DOCKER_IMAGE)
                || this.type.equals(PipelineStepStatusType.DEPLOYING_IMAGE)
                || this.type.equals(PipelineStepStatusType.PIPELINE_SUCCESS)) {
            return false;
        }
        return true;
    }

    public boolean isRunning() {
        if(this.type.equals(PipelineStepStatusType.RUNNING)
                || this.type.equals(PipelineStepStatusType.PIPELINE_STARTED)
                || this.type.equals(PipelineStepStatusType.CLONING_GIT)
                || this.type.equals(PipelineStepStatusType.GRADLE_BUILDING)
                || this.type.equals(PipelineStepStatusType.MAVEN_BUILDING)
                || this.type.equals(PipelineStepStatusType.BUILDING_IMAGE)
                || this.type.equals(PipelineStepStatusType.REMOVING_UNUSED_DOCKER_IMAGE)
                || this.type.equals(PipelineStepStatusType.DEPLOYING_IMAGE)) {
            return true;
        }
        return false;
    }

    public boolean isSuccess() {
        if(this.type.equals(PipelineStepStatusType.PIPELINE_SUCCESS)
                || this.type.equals(PipelineStepStatusType.SUCCESS)) {
            return true;
        }
        return false;
    }

    public AppCommitPipelineStep clone(Gson gson) {
        return gson.fromJson(gson.toJson(this), AppCommitPipelineStep.class);
    }
}
