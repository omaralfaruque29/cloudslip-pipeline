package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.enums.PipelineStepStatusType;
import org.bson.types.ObjectId;

public class AppPipelineStepStatusUpdateDTO extends BaseInputDTO{

    private ObjectId applicationId;

    private String commitId;

    private PipelineStepStatusType statusType;

    public AppPipelineStepStatusUpdateDTO() {
    }

    public AppPipelineStepStatusUpdateDTO(ObjectId applicationId, String commitId, PipelineStepStatusType statusType) {
        this.applicationId = applicationId;
        this.commitId = commitId;
        this.statusType = statusType;
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

    public PipelineStepStatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(PipelineStepStatusType statusType) {
        this.statusType = statusType;
    }

    public void setValues(ObjectId applicationId, String commitId, PipelineStepStatusType statusType) {
        this.applicationId = applicationId;
        this.commitId = commitId;
        this.statusType = statusType;
    }
}
