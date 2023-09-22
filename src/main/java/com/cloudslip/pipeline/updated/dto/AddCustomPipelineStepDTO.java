package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.dto_helper.AppPipelineStepHelper;
import org.bson.types.ObjectId;

import java.util.List;

public class AddCustomPipelineStepDTO extends BaseInputDTO{
    private String name;
    private ObjectId appEnvironmentId;
    private String jenkinsUrl;
    private String jenkinsApiToken;
    private List<AppPipelineStepHelper> successors;

    public AddCustomPipelineStepDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectId getAppEnvironmentId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
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

    public List<AppPipelineStepHelper> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<AppPipelineStepHelper> successors) {
        this.successors = successors;
    }
}
