package com.cloudslip.pipeline.updated.dto;


import com.cloudslip.pipeline.updated.model.universal.Vpc;

public class VpcResourceDetails {

    private Vpc vpc;
    private boolean autoScalingEnabled;
    private boolean canaryDeploymentEnabled;
    private int desiredNumberOfInstance;
    private int minCpu;
    private int maxCpu;
    private int minMemory;
    private int maxMemory;
    private int maxStorage;
    private int minNumOfInstance;
    private int maxNumOfInstance;
    private int cpuThreshold;
    private int transactionPerSecondThreshold;

    public VpcResourceDetails() {
    }

    public boolean isCanaryDeploymentEnabled() {
        return canaryDeploymentEnabled;
    }

    public void setCanaryDeploymentEnabled(boolean canaryDeploymentEnabled) {
        this.canaryDeploymentEnabled = canaryDeploymentEnabled;
    }

    public boolean isAutoScalingEnabled() {
        return autoScalingEnabled;
    }

    public void setAutoScalingEnabled(boolean autoScalingEnabled) {
        this.autoScalingEnabled = autoScalingEnabled;
    }

    public int getDesiredNumberOfInstance() {
        return desiredNumberOfInstance;
    }

    public void setDesiredNumberOfInstance(int desiredNumberOfInstance) {
        this.desiredNumberOfInstance = desiredNumberOfInstance;
    }

    public Vpc getVpc() {
        return vpc;
    }

    public void setVpc(Vpc vpc) {
        this.vpc = vpc;
    }

    public int getMinCpu() {
        return minCpu;
    }

    public void setMinCpu(int minCpu) {
        this.minCpu = minCpu;
    }

    public int getMaxCpu() {
        return maxCpu;
    }

    public void setMaxCpu(int maxCpu) {
        this.maxCpu = maxCpu;
    }

    public int getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getMaxStorage() {
        return maxStorage;
    }

    public void setMaxStorage(int maxStorage) {
        this.maxStorage = maxStorage;
    }

    public int getMinNumOfInstance() {
        return minNumOfInstance;
    }

    public void setMinNumOfInstance(int minNumOfInstance) {
        this.minNumOfInstance = minNumOfInstance;
    }

    public int getMaxNumOfInstance() {
        return maxNumOfInstance;
    }

    public void setMaxNumOfInstance(int maxNumOfInstance) {
        this.maxNumOfInstance = maxNumOfInstance;
    }

    public int getCpuThreshold() {
        return cpuThreshold;
    }

    public void setCpuThreshold(int cpuThreshold) {
        this.cpuThreshold = cpuThreshold;
    }

    public int getTransactionPerSecondThreshold() {
        return transactionPerSecondThreshold;
    }

    public void setTransactionPerSecondThreshold(int transactionPerSecondThreshold) {
        this.transactionPerSecondThreshold = transactionPerSecondThreshold;
    }
}
