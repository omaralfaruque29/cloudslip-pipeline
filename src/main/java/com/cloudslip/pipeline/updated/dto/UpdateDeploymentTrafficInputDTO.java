package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class UpdateDeploymentTrafficInputDTO extends BaseInputDTO {

    private ObjectId appCommitId;
    private ObjectId appPipelineStepId;
    private int trafficWeight;

    public UpdateDeploymentTrafficInputDTO() {
    }

    public ObjectId getAppCommitId() {
        return appCommitId;
    }

    public void setAppCommitId(ObjectId appCommitId) {
        this.appCommitId = appCommitId;
    }

    public ObjectId getAppPipelineStepId() {
        return appPipelineStepId;
    }

    public void setAppPipelineStepId(ObjectId appPipelineStepId) {
        this.appPipelineStepId = appPipelineStepId;
    }

    public int getTrafficWeight() {
        return trafficWeight;
    }

    public void setTrafficWeight(int trafficWeight) {
        this.trafficWeight = trafficWeight;
    }
}
