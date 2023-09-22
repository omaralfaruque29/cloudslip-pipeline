package com.cloudslip.pipeline.updated.model.dummy;

import com.cloudslip.pipeline.model.jenkins.PipelineStep;
import com.cloudslip.pipeline.updated.enums.PipelineStepStatusType;
import com.cloudslip.pipeline.updated.model.AppCommit;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class DeployedCommit implements Serializable {

    @NotNull
    private AppCommit appCommit;

    private PipelineStepStatusType statusType;

    private String ingresUrl;

    private int trafficWeight;

    public DeployedCommit() {
    }

    public DeployedCommit(@NotNull AppCommit appCommit, PipelineStepStatusType statusType, String ingresUrl, @NotNull int trafficWeight) {
        this.appCommit = appCommit;
        this.statusType = statusType;
        this.ingresUrl = ingresUrl;
        this.trafficWeight = trafficWeight;
    }

    public AppCommit getAppCommit() {
        return appCommit;
    }

    public void setAppCommit(AppCommit appCommit) {
        this.appCommit = appCommit;
    }

    public PipelineStepStatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(PipelineStepStatusType statusType) {
        this.statusType = statusType;
    }

    public String getIngresUrl() {
        return ingresUrl;
    }

    public void setIngresUrl(String ingresUrl) {
        this.ingresUrl = ingresUrl;
    }

    public int getTrafficWeight() {
        return trafficWeight;
    }

    public void setTrafficWeight(int trafficWeight) {
        this.trafficWeight = trafficWeight;
    }
}
