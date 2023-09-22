package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.Application;
import org.bson.types.ObjectId;

public class AppEnvStateForAppCommitDTO extends BaseInputDTO  {

    Application application;
    ObjectId commitId;

    public AppEnvStateForAppCommitDTO(Application application, ObjectId commitId) {
        this.application = application;
        this.commitId = commitId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ObjectId getCommitId() {
        return commitId;
    }

    public void setCommitId(ObjectId commitId) {
        this.commitId = commitId;
    }
}
