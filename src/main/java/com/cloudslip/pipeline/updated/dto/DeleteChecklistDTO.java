package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

import java.util.List;

public class DeleteChecklistDTO extends BaseInputDTO{

    private ObjectId appEnvChecklistId;
    private List<ObjectId> checkItemIds;

    public DeleteChecklistDTO() {
    }

    public ObjectId getAppEnvChecklistId() {
        return appEnvChecklistId;
    }

    public void setAppEnvChecklistId(ObjectId appEnvChecklistId) {
        this.appEnvChecklistId = appEnvChecklistId;
    }

    public List<ObjectId> getCheckItemIds() {
        return checkItemIds;
    }

    public void setCheckItemIds(List<ObjectId> checkItemIds) {
        this.checkItemIds = checkItemIds;
    }
}
