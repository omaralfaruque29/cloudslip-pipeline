package com.cloudslip.pipeline.updated.dto.kubeconfig;

import java.io.Serializable;

public class ServiceConfig implements Serializable {

    private String serviceName;
    private int applicationPort;
    private int metricsPort;

    public ServiceConfig() {
    }

    public ServiceConfig(String serviceName, int applicationPort, int metricsPort) {
        this.serviceName = serviceName;
        this.applicationPort = applicationPort;
        this.metricsPort = metricsPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getApplicationPort() {
        return applicationPort;
    }

    public void setApplicationPort(int applicationPort) {
        this.applicationPort = applicationPort;
    }

    public int getMetricsPort() {
        return metricsPort;
    }

    public void setMetricsPort(int metricsPort) {
        this.metricsPort = metricsPort;
    }
}
