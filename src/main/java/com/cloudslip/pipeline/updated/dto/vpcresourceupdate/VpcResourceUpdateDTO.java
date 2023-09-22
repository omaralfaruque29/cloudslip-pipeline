package com.cloudslip.pipeline.updated.dto.vpcresourceupdate;

import java.util.List;

public class VpcResourceUpdateDTO {
    private List<EnvironmentInfoUpdateDTO> selectedEnvInfoList;
    private List<EnvironmentInfoUpdateDTO> unselectedEnvInfoList;

    public VpcResourceUpdateDTO() {
    }

    public VpcResourceUpdateDTO(List<EnvironmentInfoUpdateDTO> selectedEnvInfoList, List<EnvironmentInfoUpdateDTO> unselectedEnvInfoList) {
        this.selectedEnvInfoList = selectedEnvInfoList;
        this.unselectedEnvInfoList = unselectedEnvInfoList;
    }

    public List<EnvironmentInfoUpdateDTO> getSelectedEnvInfoList() {
        return selectedEnvInfoList;
    }

    public void setSelectedEnvInfoList(List<EnvironmentInfoUpdateDTO> selectedEnvInfoList) {
        this.selectedEnvInfoList = selectedEnvInfoList;
    }

    public List<EnvironmentInfoUpdateDTO> getUnselectedEnvInfoList() {
        return unselectedEnvInfoList;
    }

    public void setUnselectedEnvInfoList(List<EnvironmentInfoUpdateDTO> unselectedEnvInfoList) {
        this.unselectedEnvInfoList = unselectedEnvInfoList;
    }
}
