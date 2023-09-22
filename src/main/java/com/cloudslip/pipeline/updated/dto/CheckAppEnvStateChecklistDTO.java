package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

import java.util.List;

public class CheckAppEnvStateChecklistDTO extends BaseInputDTO{

    private ObjectId appCommitStateId;
    private ObjectId appEnvironmentId;
    private List<ObjectId> checkedItems;

    public CheckAppEnvStateChecklistDTO() {
    }

    public ObjectId getAppCommitStateId() {
        return appCommitStateId;
    }

    public void setAppCommitStateId(ObjectId appCommitStateId) {
        this.appCommitStateId = appCommitStateId;
    }

    public ObjectId getAppEnvironmentId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }

    public List<ObjectId> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(List<ObjectId> checkedItems) {
        this.checkedItems = checkedItems;
    }
}
