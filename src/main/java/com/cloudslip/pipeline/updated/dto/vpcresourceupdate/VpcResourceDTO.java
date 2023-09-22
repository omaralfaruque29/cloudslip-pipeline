package com.cloudslip.pipeline.updated.dto.vpcresourceupdate;

import org.bson.types.ObjectId;

public class VpcResourceDTO {
    private ObjectId vpcId;

    private int selectedCpuSize;

    private int selectedMemorySize;

    private int selectedStorageSize;

    private int numberOfInstance;

    private int previousSelectedCpuSize;
    private int previousSelectedMemorySize;
    private int previousSelectedStorageSize;
    private int previousNumberOfInstance;

    public ObjectId getVpcId() {
        return vpcId;
    }

    public void setVpcId(ObjectId vpcId) {
        this.vpcId = vpcId;
    }

    public int getPreviousSelectedCpuSize() {
        return previousSelectedCpuSize;
    }

    public void setPreviousSelectedCpuSize(int previousSelectedCpuSize) {
        this.previousSelectedCpuSize = previousSelectedCpuSize;
    }

    public int getPreviousSelectedMemorySize() {
        return previousSelectedMemorySize;
    }

    public void setPreviousSelectedMemorySize(int previousSelectedMemorySize) {
        this.previousSelectedMemorySize = previousSelectedMemorySize;
    }

    public int getPreviousSelectedStorageSize() {
        return previousSelectedStorageSize;
    }

    public void setPreviousSelectedStorageSize(int previousSelectedStorageSize) {
        this.previousSelectedStorageSize = previousSelectedStorageSize;
    }

    public int getPreviousNumberOfInstance() {
        return previousNumberOfInstance;
    }

    public void setPreviousNumberOfInstance(int previousNumberOfInstance) {
        this.previousNumberOfInstance = previousNumberOfInstance;
    }

    public int getSelectedCpuSize() {
        return selectedCpuSize;
    }

    public void setSelectedCpuSize(int selectedCpuSize) {
        this.selectedCpuSize = selectedCpuSize;
    }

    public int getSelectedMemorySize() {
        return selectedMemorySize;
    }

    public void setSelectedMemorySize(int selectedMemorySize) {
        this.selectedMemorySize = selectedMemorySize;
    }

    public int getSelectedStorageSize() {
        return selectedStorageSize;
    }

    public void setSelectedStorageSize(int selectedStorageSize) {
        this.selectedStorageSize = selectedStorageSize;
    }

    public int getNumberOfInstance() {
        return numberOfInstance;
    }

    public void setNumberOfInstance(int numberOfInstance) {
        this.numberOfInstance = numberOfInstance;
    }
}
