package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class RemoveDeploymentInAppVpcInputDTO extends BaseInputDTO {

    private ObjectId appCommitId;
    private ObjectId appVpcId;

    public RemoveDeploymentInAppVpcInputDTO() {
    }

    public ObjectId getAppCommitId() {
        return appCommitId;
    }

    public void setAppCommitId(ObjectId appCommitId) {
        this.appCommitId = appCommitId;
    }

    public ObjectId getAppVpcId() {
        return appVpcId;
    }

    public void setAppVpcId(ObjectId appVpcId) {
        this.appVpcId = appVpcId;
    }
}
