package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class ResetAppCommitStateInputDTO extends BaseInputDTO {

    private ObjectId appCommitStateId;

    public ResetAppCommitStateInputDTO() {
    }

    public ObjectId getAppCommitStateId() {
        return appCommitStateId;
    }

    public void setAppCommitStateId(ObjectId appCommitStateId) {
        this.appCommitStateId = appCommitStateId;
    }
}
