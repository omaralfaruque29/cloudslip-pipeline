package com.cloudslip.pipeline.updated.dto.kubeconfig;

import java.io.Serializable;

public class AutoScalingConfig implements Serializable {

    private int minReplicas;
    private int maxReplicas;
    private int cpuThreshold;
    private int transactionThreshold;

    public AutoScalingConfig() {
    }

    public AutoScalingConfig(int minReplicas, int maxReplicas, int cpuThreshold, int transactionThreshold) {
        this.minReplicas = minReplicas;
        this.maxReplicas = maxReplicas;
        this.cpuThreshold = cpuThreshold;
        this.transactionThreshold = transactionThreshold;
    }

    public int getMinReplicas() {
        return minReplicas;
    }

    public void setMinReplicas(int minReplicas) {
        this.minReplicas = minReplicas;
    }

    public int getMaxReplicas() {
        return maxReplicas;
    }

    public void setMaxReplicas(int maxReplicas) {
        this.maxReplicas = maxReplicas;
    }

    public int getCpuThreshold() {
        return cpuThreshold;
    }

    public void setCpuThreshold(int cpuThreshold) {
        this.cpuThreshold = cpuThreshold;
    }

    public int getTransactionThreshold() {
        return transactionThreshold;
    }

    public void setTransactionThreshold(int transactionThreshold) {
        this.transactionThreshold = transactionThreshold;
    }
}
