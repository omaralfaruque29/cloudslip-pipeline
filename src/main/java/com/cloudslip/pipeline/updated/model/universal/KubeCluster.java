package com.cloudslip.pipeline.updated.model.universal;



import com.cloudslip.pipeline.updated.enums.ClusterType;
import com.cloudslip.pipeline.updated.model.BaseEntity;

import javax.validation.constraints.NotNull;

public class KubeCluster extends BaseEntity {

    @NotNull
    private String name;

    private String dashboardUrl;

    @NotNull
    private String defaultNamespace = "default";

    @NotNull
    private Region region;

    @NotNull
    private String kafkaTopic;

    private ClusterType clusterType = ClusterType.PUBLIC;

    private boolean isEnabled = true;

    private int totalCPU;

    private int totalMemory;

    private int totalStorage;

    private int availableCPU;

    private int availableMemory;

    private int availableStorage;


    public KubeCluster() {
    }

    public KubeCluster(@NotNull String name, String dashboardUrl, @NotNull String defaultNamespace, @NotNull Region region, @NotNull String kafkaTopic, ClusterType clusterType, boolean isEnabled, int totalCPU, int totalMemory, int totalStorage, int availableCPU, int availableMemory, int availableStorage) {
        this.name = name;
        this.dashboardUrl = dashboardUrl;
        this.defaultNamespace = defaultNamespace;
        this.region = region;
        this.kafkaTopic = kafkaTopic;
        this.isEnabled = isEnabled;
        this.totalCPU = totalCPU;
        this.totalMemory = totalMemory;
        this.totalStorage = totalStorage;
        this.availableCPU = availableCPU;
        this.availableMemory = availableMemory;
        this.availableStorage = availableStorage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getTotalCPU() {
        return totalCPU;
    }

    public void setTotalCPU(int totalCPU) {
        this.totalCPU = totalCPU;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(int totalMemory) {
        this.totalMemory = totalMemory;
    }

    public int getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(int totalStorage) {
        this.totalStorage = totalStorage;
    }

    public int getAvailableCPU() {
        return availableCPU;
    }

    public void setAvailableCPU(int availableCPU) {
        this.availableCPU = availableCPU;
    }

    public int getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(int availableMemory) {
        this.availableMemory = availableMemory;
    }

    public int getAvailableStorage() {
        return availableStorage;
    }

    public void setAvailableStorage(int availableStorage) {
        this.availableStorage = availableStorage;
    }

    public boolean hasEnoughResource(int cpu, int memory, int storage) {
        if(this.availableCPU > cpu && this.availableMemory > memory && this.availableStorage > storage) {
            return true;
        }
        return false;
    }

    public boolean hasMoreResourceAvailableThan(KubeCluster kubeCluster) {
        int points = 0;
        points = (this.availableCPU - kubeCluster.availableCPU) * 2000;
        points += (this.availableMemory - kubeCluster.availableMemory) * 0.5;
        points += (this.availableStorage - kubeCluster.availableStorage) * 0.01;
        return points > 0 ? true : false;
    }

    public void deductFromAvailableResource(int cpu, int memory, int storage) {
        this.availableCPU -= cpu;
        this.availableMemory -= memory;
        this.availableStorage -= storage;
    }
}
