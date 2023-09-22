package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Company;

import java.util.List;

public class CreateAppVpcDTO extends BaseInputDTO {
    private List<VpcResourceDetails> appVpcResourceList;
    private AppEnvironment appEnvironment;
    private Company company;
    private Application application;

    public CreateAppVpcDTO() {
    }

    public CreateAppVpcDTO(List<VpcResourceDetails> appVpcResourceList, AppEnvironment appEnvironment, Company company, Application application) {
        this.appVpcResourceList = appVpcResourceList;
        this.appEnvironment = appEnvironment;
        this.company = company;
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<VpcResourceDetails> getAppVpcResourceList() {
        return appVpcResourceList;
    }

    public void setAppVpcResourceList(List<VpcResourceDetails> appVpcResourceList) {
        this.appVpcResourceList = appVpcResourceList;
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
