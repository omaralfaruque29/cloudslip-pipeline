package com.cloudslip.pipeline.updated.dto.application;

import com.cloudslip.pipeline.updated.dto.BaseInputDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class AddApplicationAdvanceConfigDTO extends BaseInputDTO {

    private ObjectId applicationId;
    private String tlsSecretName;
    private String healthCheckUrl;
    private int appPort;
    private int appMetricsPort;
    private boolean ingressEnabled;
    private boolean blueGreenDeploymentEnabled;
    private boolean istioEnabled;
    private boolean istioIngressGatewayEnabled;
    private List<CustomIngressConfigDTO> customIngressConfigList;

    public AddApplicationAdvanceConfigDTO() {
    }

    public List<CustomIngressConfigDTO> getCustomIngressConfigList() {
        return customIngressConfigList;
    }

    public void setCustomIngressConfigList(List<CustomIngressConfigDTO> customIngressConfigList) {
        this.customIngressConfigList = customIngressConfigList;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getTlsSecretName() {
        return tlsSecretName;
    }

    public void setTlsSecretName(String tlsSecretName) {
        this.tlsSecretName = tlsSecretName;
    }

    public boolean isIngressEnabled() {
        return ingressEnabled;
    }

    public void setIngressEnabled(boolean ingressEnabled) {
        this.ingressEnabled = ingressEnabled;
    }

    public boolean isBlueGreenDeploymentEnabled() {
        return blueGreenDeploymentEnabled;
    }

    public void setBlueGreenDeploymentEnabled(boolean blueGreenDeploymentEnabled) {
        this.blueGreenDeploymentEnabled = blueGreenDeploymentEnabled;
    }

    public boolean isIstioEnabled() {
        return istioEnabled;
    }

    public void setIstioEnabled(boolean istioEnabled) {
        this.istioEnabled = istioEnabled;
    }

    public boolean isIstioIngressGatewayEnabled() {
        return istioIngressGatewayEnabled;
    }

    public void setIstioIngressGatewayEnabled(boolean istioIngressGatewayEnabled) {
        this.istioIngressGatewayEnabled = istioIngressGatewayEnabled;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public int getAppPort() {
        return appPort;
    }

    public void setAppPort(int appPort) {
        this.appPort = appPort;
    }

    public int getAppMetricsPort() {
        return appMetricsPort;
    }

    public void setAppMetricsPort(int appMetricsPort) {
        this.appMetricsPort = appMetricsPort;
    }
}
