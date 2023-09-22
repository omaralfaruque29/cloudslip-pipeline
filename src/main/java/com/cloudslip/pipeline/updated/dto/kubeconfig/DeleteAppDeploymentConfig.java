package com.cloudslip.pipeline.updated.dto.kubeconfig;

public class DeleteAppDeploymentConfig {
    private String namespaceName;
    private String deployedName;
    private boolean defaultIngressEnabled = false;
    private boolean customIngressEnabled = false;
    private boolean canaryEnabled = false;
    private boolean autoConfigEnabled = false;
    private boolean deleteDeployment = false;
    private boolean deleteCanary = false;

    public DeleteAppDeploymentConfig() {
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getDeployedName() {
        return deployedName;
    }

    public void setDeployedName(String deployedName) {
        this.deployedName = deployedName;
    }

    public boolean isDefaultIngressEnabled() {
        return defaultIngressEnabled;
    }

    public void setDefaultIngressEnabled(boolean defaultIngressEnabled) {
        this.defaultIngressEnabled = defaultIngressEnabled;
    }

    public boolean isCustomIngressEnabled() {
        return customIngressEnabled;
    }

    public void setCustomIngressEnabled(boolean customIngressEnabled) {
        this.customIngressEnabled = customIngressEnabled;
    }

    public boolean isCanaryEnabled() {
        return canaryEnabled;
    }

    public void setCanaryEnabled(boolean canaryEnabled) {
        this.canaryEnabled = canaryEnabled;
    }

    public boolean isAutoConfigEnabled() {
        return autoConfigEnabled;
    }

    public boolean isDeleteDeployment() {
        return deleteDeployment;
    }

    public void setDeleteDeployment(boolean deleteDeployment) {
        this.deleteDeployment = deleteDeployment;
    }

    public boolean isDeleteCanary() {
        return deleteCanary;
    }

    public void setDeleteCanary(boolean deleteCanary) {
        this.deleteCanary = deleteCanary;
    }

    public void setAutoConfigEnabled(boolean autoConfigEnabled) {

        this.autoConfigEnabled = autoConfigEnabled;
    }
}
