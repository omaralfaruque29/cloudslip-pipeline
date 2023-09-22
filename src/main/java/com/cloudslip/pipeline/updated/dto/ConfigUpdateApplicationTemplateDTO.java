package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Company;

import java.util.List;
import java.util.Map;


public class ConfigUpdateApplicationTemplateDTO extends BaseInputDTO {
    private Application application;
    private Company company;
    private List<AppEnvironment> selectedAppEnvironments;
    private List<AppEnvironment> unselectedAppEnvironments;
    private Map<String, List<AppVpc>> unselectedAppVpcMap;

    public ConfigUpdateApplicationTemplateDTO() {
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<AppEnvironment> getSelectedAppEnvironments() {
        return selectedAppEnvironments;
    }

    public void setSelectedAppEnvironments(List<AppEnvironment> selectedAppEnvironments) {
        this.selectedAppEnvironments = selectedAppEnvironments;
    }

    public List<AppEnvironment> getUnselectedAppEnvironments() {
        return unselectedAppEnvironments;
    }

    public void setUnselectedAppEnvironments(List<AppEnvironment> unselectedAppEnvironments) {
        this.unselectedAppEnvironments = unselectedAppEnvironments;
    }

    public Map<String, List<AppVpc>> getUnselectedAppVpcMap() {
        return unselectedAppVpcMap;
    }

    public void setUnselectedAppVpcMap(Map<String, List<AppVpc>> unselectedAppVpcMap) {
        this.unselectedAppVpcMap = unselectedAppVpcMap;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
