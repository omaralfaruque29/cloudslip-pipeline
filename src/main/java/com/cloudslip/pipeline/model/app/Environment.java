package com.cloudslip.pipeline.model.app;

import com.cloudslip.pipeline.model.Status;

public class Environment {

    private String envName;
    private Status status;
    private String currentDeployedVersion;              //commit id
    private boolean allTrafficToCurrentVersion;
    private String appBaseUrl;
    private String grafanaDashboardUrl;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(final String envName) {
        this.envName = envName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getCurrentDeployedVersion() {
        return currentDeployedVersion;
    }

    public void setCurrentDeployedVersion(final String currentDeployedVersion) {
        this.currentDeployedVersion = currentDeployedVersion;
    }

    public boolean isAllTrafficToCurrentVersion() {
        return allTrafficToCurrentVersion;
    }

    public void setAllTrafficToCurrentVersion(final boolean allTrafficToCurrentVersion) {
        this.allTrafficToCurrentVersion = allTrafficToCurrentVersion;
    }

    public String getAppBaseUrl() {
        return appBaseUrl;
    }

    public void setAppBaseUrl(final String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
    }

    public String getGrafanaDashboardUrl() {
        return grafanaDashboardUrl;
    }

    public void setGrafanaDashboardUrl(final String grafanaDashboardUrl) {
        this.grafanaDashboardUrl = grafanaDashboardUrl;
    }
}
