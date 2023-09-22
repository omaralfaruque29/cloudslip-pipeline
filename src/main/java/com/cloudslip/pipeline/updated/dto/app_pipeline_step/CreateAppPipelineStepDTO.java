package com.cloudslip.pipeline.updated.dto.app_pipeline_step;

import com.cloudslip.pipeline.updated.dto.BaseInputDTO;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.AppEnvironment;

import java.util.List;

public class CreateAppPipelineStepDTO extends BaseInputDTO {

    private AppEnvironment appEnvironment;
    private List<AppVpc> appVpcList;

    public CreateAppPipelineStepDTO(AppEnvironment appEnvironment, List<AppVpc> appVpcList) {
        this.appEnvironment = appEnvironment;
        this.appVpcList = appVpcList;
    }

    public CreateAppPipelineStepDTO() {
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public List<AppVpc> getAppVpcList() {
        return appVpcList;
    }

    public void setAppVpcList(List<AppVpc> appVpcList) {
        this.appVpcList = appVpcList;
    }
}
