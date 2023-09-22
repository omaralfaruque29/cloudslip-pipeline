package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import org.bson.types.ObjectId;

public class AddPipelineStepToListDTO extends BaseInputDTO{
    private AppPipelineStep appPipelineStep;
    private ObjectId appEnvironmentId;

    public AddPipelineStepToListDTO(AppPipelineStep appPipelineStep, ObjectId appEnvironmentId) {
        this.appPipelineStep = appPipelineStep;
        this.appEnvironmentId = appEnvironmentId;
    }

    public AppPipelineStep getAppPipelineStep() {
        return appPipelineStep;
    }

    public void setAppPipelineStep(AppPipelineStep appPipelineStep) {
        this.appPipelineStep = appPipelineStep;
    }

    public ObjectId getAppEnvironmentId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }
}
