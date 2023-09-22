package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;
import org.bson.types.ObjectId;

public class AppEnvStateForAddEnvironmentDTO extends BaseInputDTO {

    Application application;
    AppEnvironment appEnvironment;
    ObjectId appCommitId;

    public AppEnvStateForAddEnvironmentDTO() {
    }

    public AppEnvStateForAddEnvironmentDTO(Application application, AppEnvironment appEnvironment, ObjectId appCommitId) {
        this.application = application;
        this.appEnvironment = appEnvironment;
        this.appCommitId = appCommitId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public ObjectId getAppCommitId() {
        return appCommitId;
    }

    public void setAppCommitId(ObjectId appCommitId) {
        this.appCommitId = appCommitId;
    }
}
