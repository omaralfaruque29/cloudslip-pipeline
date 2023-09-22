package com.cloudslip.pipeline.updated.dto.kubeconfig;


import java.io.Serializable;

public class AppKubeConfig implements Serializable {

    private String name;

    private DeploymentConfig deploymentConfig;

    private ServiceConfig serviceConfig;

    private boolean ingressEnabled;

    private IngressConfig ingressConfig;

    private boolean autoScalingEnabled = false;

    private AutoScalingConfig autoScalingConfig;

    private boolean isCanaryDeployment = false;


    public AppKubeConfig() {
    }

    public AppKubeConfig(String name, DeploymentConfig deploymentConfig, ServiceConfig serviceConfig, boolean ingressEnabled, IngressConfig ingressConfig, boolean autoScalingEnabled, AutoScalingConfig autoScalingConfig, boolean isCanaryDeployment) {
        this.name = name;
        this.deploymentConfig = deploymentConfig;
        this.serviceConfig = serviceConfig;
        this.ingressEnabled = ingressEnabled;
        this.ingressConfig = ingressConfig;
        this.autoScalingEnabled = autoScalingEnabled;
        this.autoScalingConfig = autoScalingConfig;
        this.isCanaryDeployment = isCanaryDeployment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeploymentConfig getDeploymentConfig() {
        return deploymentConfig;
    }

    public void setDeploymentConfig(DeploymentConfig deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public boolean isIngressEnabled() {
        return ingressEnabled;
    }

    public void setIngressEnabled(boolean ingressEnabled) {
        this.ingressEnabled = ingressEnabled;
    }

    public IngressConfig getIngressConfig() {
        return ingressConfig;
    }

    public void setIngressConfig(IngressConfig ingressConfig) {
        this.ingressConfig = ingressConfig;
    }

    public boolean isAutoScalingEnabled() {
        return autoScalingEnabled;
    }

    public void setAutoScalingEnabled(boolean autoScalingEnabled) {
        this.autoScalingEnabled = autoScalingEnabled;
    }

    public AutoScalingConfig getAutoScalingConfig() {
        return autoScalingConfig;
    }

    public void setAutoScalingConfig(AutoScalingConfig autoScalingConfig) {
        this.autoScalingConfig = autoScalingConfig;
    }

    public boolean isCanaryDeployment() {
        return isCanaryDeployment;
    }

    public void setCanaryDeployment(boolean canaryDeployment) {
        isCanaryDeployment = canaryDeployment;
    }
}
