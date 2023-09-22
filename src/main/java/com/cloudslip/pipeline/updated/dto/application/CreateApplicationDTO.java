package com.cloudslip.pipeline.updated.dto.application;

import com.cloudslip.pipeline.updated.dto.BaseInputDTO;
import org.bson.types.ObjectId;

public class CreateApplicationDTO extends BaseInputDTO {

    private String name;
    private String packageName;
    private ObjectId teamId;
    private String appCreationType;
    private String applicationType;
    private String applicationBuildType;
    private String gitRepositoryName;
    private String gitBranchName;

    public CreateApplicationDTO() {
    }

    public String getGitRepositoryName() {
        return gitRepositoryName;
    }

    public void setGitRepositoryName(String gitRepositoryName) {
        this.gitRepositoryName = gitRepositoryName;
    }

    public String getGitBranchName() {
        return gitBranchName;
    }

    public void setGitBranchName(String gitBranchName) {
        this.gitBranchName = gitBranchName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public ObjectId getTeamId() {
        return teamId;
    }

    public void setTeamId(ObjectId teamId) {
        this.teamId = teamId;
    }

    public String getAppCreationType() {
        return appCreationType;
    }

    public void setAppCreationType(String appCreationType) {
        this.appCreationType = appCreationType;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getApplicationBuildType() {
        return applicationBuildType;
    }

    public void setApplicationBuildType(String applicationBuildType) {
        this.applicationBuildType = applicationBuildType;
    }
}
