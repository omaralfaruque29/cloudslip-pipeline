package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

@Document(collection = "app_commit")
public class AppCommit extends BaseEntity  {

    @NotNull
    private ObjectId applicationId;

    @NotNull
    private String gitCommitId;

    @NotNull
    private String commitMessage;

    @NotNull
    private Date commitDate;

    public AppCommit() {
    }

    public String getApplicationId() {
        return applicationId.toHexString();
    }

    @JsonIgnore
    public ObjectId getApplicationObjectId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public void setGitCommitId(String gitCommitId) {
        this.gitCommitId = gitCommitId;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    @JsonIgnore
    public boolean isValid() {
        if(this.getStatus() == Status.V) {
            return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isDeleted() {
        if(this.getStatus() == Status.D) {
            return true;
        }
        return false;
    }
}
