package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppVpc;

import java.util.List;
import java.util.Map;

public class CreateAppVpcResponseDTO extends BaseInputDTO {

    private List<AppVpc> selectedAppVpc;
    private List<AppVpc> unselectedAppVpc;
    private Map<String, Integer> previousAppVpcCPU;
    private Map<String, Integer> previousAppVpcMemory;
    private Map<String, Integer> previousAppVpcStorage;
    private Map<String, Integer> previousNumberOfInstance;

    public CreateAppVpcResponseDTO() {
    }

    public List<AppVpc> getUnselectedAppVpc() {
        return unselectedAppVpc;
    }

    public void setUnselectedAppVpc(List<AppVpc> unselectedAppVpc) {
        this.unselectedAppVpc = unselectedAppVpc;
    }

    public List<AppVpc> getSelectedAppVpc() {
        return selectedAppVpc;
    }

    public Map<String, Integer> getPreviousAppVpcCPU() {
        return previousAppVpcCPU;
    }

    public void setPreviousAppVpcCPU(Map<String, Integer> previousAppVpcCPU) {
        this.previousAppVpcCPU = previousAppVpcCPU;
    }

    public Map<String, Integer> getPreviousAppVpcMemory() {
        return previousAppVpcMemory;
    }

    public void setPreviousAppVpcMemory(Map<String, Integer> previousAppVpcMemory) {
        this.previousAppVpcMemory = previousAppVpcMemory;
    }

    public Map<String, Integer> getPreviousAppVpcStorage() {
        return previousAppVpcStorage;
    }

    public void setPreviousAppVpcStorage(Map<String, Integer> previousAppVpcStorage) {
        this.previousAppVpcStorage = previousAppVpcStorage;
    }

    public Map<String, Integer> getPreviousNumberOfInstance() {
        return previousNumberOfInstance;
    }

    public void setPreviousNumberOfInstance(Map<String, Integer> previousNumberOfInstance) {
        this.previousNumberOfInstance = previousNumberOfInstance;
    }

    public void setSelectedAppVpc(List<AppVpc> selectedAppVpc) {
        this.selectedAppVpc = selectedAppVpc;
    }
}
