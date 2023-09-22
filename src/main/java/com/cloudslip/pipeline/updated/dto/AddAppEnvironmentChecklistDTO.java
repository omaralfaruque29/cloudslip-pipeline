package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

import java.util.List;

public class AddAppEnvironmentChecklistDTO extends BaseInputDTO{

    private ObjectId applicationId;
    private ObjectId appEnvironmentId;
    private List<CreateCheckItemDTO> checklists;

    public AddAppEnvironmentChecklistDTO() {
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public ObjectId getAppEnvironmentId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }

    public List<CreateCheckItemDTO> getChecklists() {
        return checklists;
    }

    public void setChecklists(List<CreateCheckItemDTO> checklists) {
        this.checklists = checklists;
    }
}
