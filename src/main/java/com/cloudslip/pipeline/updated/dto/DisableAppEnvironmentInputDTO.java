package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class DisableAppEnvironmentInputDTO extends BaseInputDTO {

    private ObjectId appCommitStateId;
    private ObjectId appEnvironmentStateForAppCommitId;

    public DisableAppEnvironmentInputDTO() {
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
}
