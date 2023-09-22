package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class TriggerPipelineFromStartInputDTO extends BaseInputDTO {

    private ObjectId applicationId;

    private String commitId;

    public TriggerPipelineFromStartInputDTO(ObjectId applicationId, String commitId) {
        this.applicationId = applicationId;
        this.commitId = commitId;
    }

    public TriggerPipelineFromStartInputDTO() {
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
}
