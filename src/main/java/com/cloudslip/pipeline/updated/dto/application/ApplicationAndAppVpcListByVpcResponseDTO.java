package com.cloudslip.pipeline.updated.dto.application;

import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.Application;


public class ApplicationAndAppVpcListByVpcResponseDTO {
    private Application application;
    private AppVpc appVpc;

    public ApplicationAndAppVpcListByVpcResponseDTO() {
    }

    public ApplicationAndAppVpcListByVpcResponseDTO(Application application, AppVpc appVpc) {
        this.application = application;
        this.appVpc = appVpc;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public AppVpc getAppVpc() {
        return appVpc;
    }

    public void setAppVpc(AppVpc appVpc) {
        this.appVpc = appVpc;
    }
}
