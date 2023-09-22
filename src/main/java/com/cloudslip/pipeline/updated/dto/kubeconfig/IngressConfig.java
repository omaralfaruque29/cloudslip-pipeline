package com.cloudslip.pipeline.updated.dto.kubeconfig;

import java.io.Serializable;

public class IngressConfig implements Serializable {

    private String defaultIngressUrl;

    private String customIngressUrl;

    private String canaryIngressUrl;

    public IngressConfig() {
    }

    public IngressConfig(String defaultIngressUrl, String customIngressUrl, String canaryIngressUrl) {
        this.defaultIngressUrl = defaultIngressUrl;
        this.customIngressUrl = customIngressUrl;
        this.canaryIngressUrl = canaryIngressUrl;
    }

    public String getDefaultIngressUrl() {
        return defaultIngressUrl;
    }

    public void setDefaultIngressUrl(String defaultIngressUrl) {
        this.defaultIngressUrl = defaultIngressUrl;
    }

    public String getCustomIngressUrl() {
        return customIngressUrl;
    }

    public void setCustomIngressUrl(String customIngressUrl) {
        this.customIngressUrl = customIngressUrl;
    }

    public String getCanaryIngressUrl() {
        return canaryIngressUrl;
    }

    public void setCanaryIngressUrl(String canaryIngressUrl) {
        this.canaryIngressUrl = canaryIngressUrl;
    }
}
