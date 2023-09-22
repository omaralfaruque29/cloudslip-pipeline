package com.cloudslip.pipeline.updated.dto.kubeconfig;


import java.io.Serializable;

public class NamespaceConfig implements Serializable {

    private String namespace;

    private int cpuLimit;

    private int memoryLimit;

    private int storageLimit;


    public NamespaceConfig() {
    }

    public NamespaceConfig(String namespace) {
        this.namespace = namespace;
    }

    public NamespaceConfig(String namespace, int cpuLimit, int memoryLimit, int storageLimit) {
        this.namespace = namespace;
        this.cpuLimit = cpuLimit;
        this.memoryLimit = memoryLimit;
        this.storageLimit = storageLimit;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(int cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public int getStorageLimit() {
        return storageLimit;
    }

    public void setStorageLimit(int storageLimit) {
        this.storageLimit = storageLimit;
    }
}
