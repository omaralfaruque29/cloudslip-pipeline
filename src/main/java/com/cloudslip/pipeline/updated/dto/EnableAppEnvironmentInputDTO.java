package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class EnableAppEnvironmentInputDTO extends BaseInputDTO {

    private ObjectId appCommitStateId;
    private ObjectId appEnvironmentStateForAppCommitId;
    private boolean force;

    public EnableAppEnvironmentInputDTO() {
    }

    public ObjectId getAppCommitStateId() {
        return appCommitStateId;
    }

    public void setAppCommitStateId(ObjectId appCommitStateId) {
        this.appCommitStateId = appCommitStateId;
    }

    public ObjectId getAppEnvironmentStateForAppCommitId() {
        return appEnvironmentStateForAppCommitId;
    }

    public void setAppEnvironmentStateForAppCommitId(ObjectId appEnvironmentStateForAppCommitId) {
        this.appEnvironmentStateForAppCommitId = appEnvironmentStateForAppCommitId;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
