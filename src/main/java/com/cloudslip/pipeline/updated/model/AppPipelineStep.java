package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.enums.PipelineStepType;
import com.cloudslip.pipeline.updated.model.dummy.SuccessorPipelineStep;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "app_pipeline_step")
public class AppPipelineStep extends BaseEntity {

    @NotNull
    private String name;

    @NotNull
    private ObjectId appEnvironmentId;

    @NotNull
    private PipelineStepType stepType = PipelineStepType.CUSTOM;

    private ObjectId appVpcId;

    private String jenkinsUrl;

    private String jenkinsApiToken;

    private List<SuccessorPipelineStep> successors;


    public AppPipelineStep() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppEnvironmentId() {
        return appEnvironmentId.toHexString();
    }

    @JsonIgnore
    public ObjectId getAppEnvironmentObjectId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }

    public PipelineStepType getStepType() {
        return stepType;
    }

    public void setStepType(PipelineStepType stepType) {
        this.stepType = stepType;
    }

    public String getAppVpcId() {
        if(appVpcId == null) return null;
        return appVpcId.toHexString();
    }

    @JsonIgnore
    public ObjectId getAppVpcObjectId() {
        return appVpcId;
    }

    public void setAppVpcId(ObjectId appVpcId) {
        this.appVpcId = appVpcId;
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    public void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public String getJenkinsApiToken() {
        return jenkinsApiToken;
    }

    public void setJenkinsApiToken(String jenkinsApiToken) {
        this.jenkinsApiToken = jenkinsApiToken;
    }

    public List<SuccessorPipelineStep> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<SuccessorPipelineStep> successors) {
        this.successors = successors;
    }
}
