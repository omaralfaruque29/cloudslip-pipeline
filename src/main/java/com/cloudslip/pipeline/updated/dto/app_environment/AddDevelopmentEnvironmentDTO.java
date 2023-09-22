package com.cloudslip.pipeline.updated.dto.app_environment;

import com.cloudslip.pipeline.updated.dto.BaseInputDTO;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.model.universal.EnvironmentOption;

import java.util.List;

public class AddDevelopmentEnvironmentDTO extends BaseInputDTO {
    private Company company;
    private Application application;
    private EnvironmentOption environmentOption;

    public AddDevelopmentEnvironmentDTO() {
    }

    public AddDevelopmentEnvironmentDTO(Company company, Application application, EnvironmentOption environmentOption) {
        this.company = company;
        this.application = application;
        this.environmentOption = environmentOption;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public EnvironmentOption getEnvironmentOption() {
        return environmentOption;
    }

    public void setEnvironmentOption(EnvironmentOption environmentOption) {
        this.environmentOption = environmentOption;
    }
}
