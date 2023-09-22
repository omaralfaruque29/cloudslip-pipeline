package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppEnvironment;
import org.bson.types.ObjectId;

public class CreateAppCommitPipelineStepDTO extends BaseInputDTO {
    private ObjectId commitId;
    private AppEnvironment appEnvironment;

    public CreateAppCommitPipelineStepDTO(ObjectId commitId, AppEnvironment appEnvironment) {
        this.commitId = commitId;
        this.appEnvironment = appEnvironment;
    }

    public ObjectId getCommitId() {
        return commitId;
    }

    public void setCommitId(ObjectId commitId) {
        this.commitId = commitId;
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }
}
