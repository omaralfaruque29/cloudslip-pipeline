package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

import java.util.Date;

public class CreateAppCommitDTO extends BaseInputDTO{

    private ObjectId applicationId;
    private String commitId;
    private String commitMessage;
    private Date commitDate;

    public CreateAppCommitDTO() {
    }

    public CreateAppCommitDTO(ObjectId applicationId, String commitId, String commitMessage, Date commitDate) {
        this.applicationId = applicationId;
        this.commitId = commitId;
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
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

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
}
