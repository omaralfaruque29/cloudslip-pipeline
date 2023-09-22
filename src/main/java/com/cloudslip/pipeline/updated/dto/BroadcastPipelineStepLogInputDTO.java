package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class BroadcastPipelineStepLogInputDTO extends BaseInputDTO {

    private ObjectId appCommitStateId;

    public BroadcastPipelineStepLogInputDTO() {
    }

    public ObjectId getAppCommitStateId() {
        return appCommitStateId;
    }

    public void setAppCommitStateId(ObjectId appCommitStateId) {
        this.appCommitStateId = appCommitStateId;
    }
}
