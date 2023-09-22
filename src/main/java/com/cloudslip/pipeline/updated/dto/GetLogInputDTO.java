package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class GetLogInputDTO extends BaseInputDTO{

    private ObjectId appCommitPipelineStepId;
    private String fetchType;

    public GetLogInputDTO() {
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }

    public String getFetchType() {
        return fetchType;
    }

    public void setFetchType(String fetchType) {
        this.fetchType = fetchType;
    }
}
