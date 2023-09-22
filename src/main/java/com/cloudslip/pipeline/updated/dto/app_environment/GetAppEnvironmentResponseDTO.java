package com.cloudslip.pipeline.updated.dto.app_environment;


import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;

import java.util.List;

public class GetAppEnvironmentResponseDTO {
    private Application application;
    private List<AppEnvironment> appEnvironmentList;

    public GetAppEnvironmentResponseDTO() {
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<AppEnvironment> getAppEnvironmentList() {
        return appEnvironmentList;
    }

    public void setAppEnvironmentList(List<AppEnvironment> appEnvironmentList) {
        this.appEnvironmentList = appEnvironmentList;
    }
}
