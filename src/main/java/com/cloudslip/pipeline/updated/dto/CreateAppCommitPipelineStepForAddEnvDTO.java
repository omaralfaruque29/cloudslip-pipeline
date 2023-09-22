package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import org.bson.types.ObjectId;

public class CreateAppCommitPipelineStepForAddEnvDTO extends BaseInputDTO {
    private ObjectId commitId;
    private AppEnvironment appEnvironment;
    private AppPipelineStep appPipelineStep;

    public CreateAppCommitPipelineStepForAddEnvDTO() {
    }

    public CreateAppCommitPipelineStepForAddEnvDTO(ObjectId commitId, AppEnvironment appEnvironment, AppPipelineStep appPipelineStep) {
        this.commitId = commitId;
        this.appEnvironment = appEnvironment;
        this.appPipelineStep = appPipelineStep;
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

    public AppPipelineStep getAppPipelineStep() {
        return appPipelineStep;
    }

    public void setAppPipelineStep(AppPipelineStep appPipelineStep) {
        this.appPipelineStep = appPipelineStep;
    }
}
