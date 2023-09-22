package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class DownloadPipelineStepLogInputDTO extends BaseInputDTO {

    private ObjectId appCommitPipelineStepId;

    public DownloadPipelineStepLogInputDTO() {
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }
}
