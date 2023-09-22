package com.cloudslip.pipeline.updated.dto.kubeconfig;

import java.io.Serializable;

public class RollingUpdateConfig implements Serializable {

    private int maxSurge;
    private int maxUnavailable = 0;

    public RollingUpdateConfig() {
    }

    public RollingUpdateConfig(int maxSurge) {
        this.maxSurge = maxSurge;
    }

    public RollingUpdateConfig(int maxSurge, int maxUnavailable) {
        this.maxSurge = maxSurge;
        this.maxUnavailable = maxUnavailable;
    }

    public int getMaxSurge() {
        return maxSurge;
    }

    public void setMaxSurge(int maxSurge) {
        this.maxSurge = maxSurge;
    }

    public int getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(int maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }
}
