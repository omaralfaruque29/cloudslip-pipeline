package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class TriggerPipelineStepInputDTO extends BaseInputDTO {

    private ObjectId appCommitPipelineStepId;

    public TriggerPipelineStepInputDTO() {
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }
}
