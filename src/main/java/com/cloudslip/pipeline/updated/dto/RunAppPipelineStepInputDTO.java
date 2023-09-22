package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class RunAppPipelineStepInputDTO extends BaseInputDTO {

    private ObjectId appCommitPipelineStepId;
    private boolean canaryDeployment = false;
    private boolean forceRun = false;

    public RunAppPipelineStepInputDTO() {
    }

    public RunAppPipelineStepInputDTO(ObjectId appCommitPipelineStepId, boolean canaryDeployment, boolean forceRun) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
        this.canaryDeployment = canaryDeployment;
        this.forceRun = forceRun;
    }

    public ObjectId getAppCommitPipelineStepId() {
        return appCommitPipelineStepId;
    }

    public void setAppCommitPipelineStepId(ObjectId appCommitPipelineStepId) {
        this.appCommitPipelineStepId = appCommitPipelineStepId;
    }

    public boolean isCanaryDeployment() {
        return canaryDeployment;
    }

    public void setCanaryDeployment(boolean canaryDeployment) {
        this.canaryDeployment = canaryDeployment;
    }

    public boolean isForceRun() {
        return forceRun;
    }

    public void setForceRun(boolean forceRun) {
        this.forceRun = forceRun;
    }
}
