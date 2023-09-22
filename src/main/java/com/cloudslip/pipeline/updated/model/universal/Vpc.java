package com.cloudslip.pipeline.updated.model.universal;


import com.cloudslip.pipeline.updated.enums.VpcStatus;
import com.cloudslip.pipeline.updated.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

public class Vpc extends BaseEntity {

    private KubeCluster kubeCluster;

    @NotNull
    private String name;

    private String dashboardUrl;

    private int orderNo;

    private ObjectId companyId;

    private String namespace = "default";

    private int totalCPU;

    private int totalMemory;

    private int totalStorage;

    private int availableCPU;

    private int availableMemory;

    private int availableStorage;

    private VpcStatus vpcStatus;


    public Vpc() {
    }

    public Vpc(KubeCluster kubeCluster, @NotNull String name, String dashboardUrl, int orderNo, ObjectId companyId, String namespace, int totalCPU, int totalMemory, int totalStorage, int availableCPU, int availableMemory, int availableStorage, VpcStatus vpcStatus) {
        this.kubeCluster = kubeCluster;
        this.name = name;
        this.dashboardUrl = dashboardUrl;
        this.orderNo = orderNo;
        this.companyId = companyId;
        this.namespace = namespace;
        this.totalCPU = totalCPU;
        this.totalMemory = totalMemory;
        this.totalStorage = totalStorage;
        this.availableCPU = availableCPU;
        this.availableMemory = availableMemory;
        this.availableStorage = availableStorage;
        this.vpcStatus = vpcStatus;
    }

    public KubeCluster getKubeCluster() {
        return kubeCluster;
    }

    public void setKubeCluster(KubeCluster kubeCluster) {
        this.kubeCluster = kubeCluster;
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

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    @JsonIgnore
    public ObjectId getCompanyObjectId() {
        return companyId;
    }

    public String getCompanyId() {
        return companyId.toHexString();
    }

    public void setCompanyId(ObjectId companyId) {
        this.companyId = companyId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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

    public VpcStatus getVpcStatus() {
        return vpcStatus;
    }

    public void setVpcStatus(VpcStatus vpcStatus) {
        this.vpcStatus = vpcStatus;
    }
}
