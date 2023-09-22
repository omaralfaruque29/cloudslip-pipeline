package com.cloudslip.pipeline.updated.dto.app_environment;

import org.bson.types.ObjectId;

import java.util.List;

public class EnvironmentDTO {
    private ObjectId environmentId;
    private List<VpcResourceDetailsDTO> selectedVpcList;

    public EnvironmentDTO() {
    }

    public EnvironmentDTO(ObjectId environmentId, List<VpcResourceDetailsDTO> selectedVpcList) {
        this.environmentId = environmentId;
        this.selectedVpcList = selectedVpcList;
    }

    public ObjectId getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(ObjectId environmentId) {
        this.environmentId = environmentId;
    }

    public List<VpcResourceDetailsDTO> getSelectedVpcList() {
        return selectedVpcList;
    }

    public void setSelectedVpcList(List<VpcResourceDetailsDTO> selectedVpcList) {
        this.selectedVpcList = selectedVpcList;
    }
}
