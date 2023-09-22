package com.cloudslip.pipeline.updated.model.dummy;

import java.io.Serializable;

public class AppSecretEnvironment implements Serializable {

    private String appEnvironmentId;

    private String environmentName;

    public AppSecretEnvironment() {
    }

    public AppSecretEnvironment(String appEnvironmentId, String environmentName) {
        this.appEnvironmentId = appEnvironmentId;
        this.environmentName = environmentName;
    }

    public String getAppEnvironmentId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(String appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }
}
