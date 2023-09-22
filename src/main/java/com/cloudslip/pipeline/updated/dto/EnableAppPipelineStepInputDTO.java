package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class EnableAppPipelineStepInputDTO extends BaseInputDTO {

    private ObjectId appCommitStateId;
    private ObjectId appCommitPipelineStepId;
    private boolean force;

    public EnableAppPipelineStepInputDTO() {
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

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
