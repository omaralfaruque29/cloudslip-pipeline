package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class DisableAppPipelineStepInputDTO extends BaseInputDTO {

    private ObjectId appCommitStateId;
    private ObjectId appCommitPipelineStepId;

    public DisableAppPipelineStepInputDTO() {
    }

    public ObjectId getAppCommitStateId() {
        return appCommitStateId;
    }

    public void setAppCommitStateId(ObjectId appCommitStateId) {
        this.appCommitStateId = appCommitStateId;
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }
}
