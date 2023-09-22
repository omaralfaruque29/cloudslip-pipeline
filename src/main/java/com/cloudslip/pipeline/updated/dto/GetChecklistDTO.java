package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class GetChecklistDTO extends BaseInputDTO {
    private  ObjectId applicationId;
    private ObjectId appEnvId;

    public GetChecklistDTO(ObjectId applicationId, ObjectId appEnvId) {
        this.applicationId = applicationId;
        this.appEnvId = appEnvId;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public ObjectId getAppEnvId() {
        return appEnvId;
    }

    public void setAppEnvId(ObjectId appEnvId) {
        this.appEnvId = appEnvId;
    }
}
