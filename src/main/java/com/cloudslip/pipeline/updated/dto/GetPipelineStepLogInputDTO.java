package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class GetPipelineStepLogInputDTO extends BaseInputDTO {

    private ObjectId appCommitPipelineStepId;

    public GetPipelineStepLogInputDTO() {
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }
}
