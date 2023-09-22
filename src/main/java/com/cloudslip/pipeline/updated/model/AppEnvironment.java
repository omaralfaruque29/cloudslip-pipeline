package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.model.universal.EnvironmentOption;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;


@Document(collection = "app_environment")
public class AppEnvironment extends BaseEntity {

    @NotNull
    private ObjectId applicationId;

    @NotNull
    private ObjectId companyId;

    @NotNull
    private EnvironmentOption environment;

    private String jenkinsUrl;

    private String jenkinsApiToken;

    private String grafanaApiUrl;

    private String grafanaAuthorizationToken;

    @NotNull
    private List<AppVpc> appVpcList;

    @NotNull
    private List<AppPipelineStep> appPipelineStepList;

    public AppEnvironment() {
    }

    public AppEnvironment(@NotNull ObjectId applicationId, @NotNull ObjectId companyId, @NotNull EnvironmentOption environment, String jenkinsUrl, String jenkinsApiToken, String grafanaApiUrl, String grafanaAuthorizationToken, @NotNull List<AppVpc> appVpcList, @NotNull List<AppPipelineStep> appPipelineStepList) {
        this.applicationId = applicationId;
        this.companyId = companyId;
        this.environment = environment;
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsApiToken = jenkinsApiToken;
        this.grafanaApiUrl = grafanaApiUrl;
        this.grafanaAuthorizationToken = grafanaAuthorizationToken;
        this.appVpcList = appVpcList;
        this.appPipelineStepList = appPipelineStepList;
    }

    @JsonIgnore
    public ObjectId getApplicationObjectId() {
        return applicationId;
    }

    public String getApplicationId() {
        return applicationId.toHexString();
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    @JsonIgnore
    public ObjectId getCompanyObjectId() { return companyId; }

    public String getCompanyId() {
        return companyId.toHexString();
    }

    public void setCompanyId(ObjectId companyId) {
        this.companyId = companyId;
    }

    public EnvironmentOption getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentOption environment) {
        this.environment = environment;
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    public void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public String getJenkinsApiToken() {
        return jenkinsApiToken;
    }

    public void setJenkinsApiToken(String jenkinsApiToken) {
        this.jenkinsApiToken = jenkinsApiToken;
    }

    public String getGrafanaApiUrl() {
        return grafanaApiUrl;
    }

    public void setGrafanaApiUrl(String grafanaApiUrl) {
        this.grafanaApiUrl = grafanaApiUrl;
    }

    public String getGrafanaAuthorizationToken() {
        return grafanaAuthorizationToken;
    }

    public void setGrafanaAuthorizationToken(String grafanaAuthorizationToken) {
        this.grafanaAuthorizationToken = grafanaAuthorizationToken;
    }

    public List<AppVpc> getAppVpcList() {
        return appVpcList;
    }

    public void setAppVpcList(List<AppVpc> appVpcList) {
        this.appVpcList = appVpcList;
    }

    public List<AppPipelineStep> getAppPipelineStepList() {
        return appPipelineStepList;
    }

    public void setAppPipelineStepList(List<AppPipelineStep> appPipelineStepList) {
        this.appPipelineStepList = appPipelineStepList;
    }

    @JsonIgnore
    public int getAppVpcIndex(ObjectId appVpcId) {
        if (this.getAppVpcList() == null) {
            return -1;
        }
        for (int index = 0; index < this.getAppVpcList().size(); index++) {
            if (this.getAppVpcList().get(index).getObjectId().toString().equals(appVpcId.toString())) {
                return index;
            }
        }
        return -1;
    }

    @JsonIgnore
    public int getAppPipelineStepIndex(AppPipelineStep appPipelineStep) {
        int index = 0;
        if(this.appPipelineStepList == null){
            return -1;
        }
        for (AppPipelineStep aps : this.appPipelineStepList) {
            if(aps.getId().equals(appPipelineStep.getId())){
                return index;
            }
            index++;
        }
        return -1;
    }

    @JsonIgnore
    public boolean exitsIn(List<AppEnvironment> appEnvironmentList) {
        for (AppEnvironment appEnvironment : appEnvironmentList) {
            if (this.getId().equals(appEnvironment.getId())){
                return true;
            }
        }
        return false;
    }

    public AppEnvironment clone(Gson gson) {
        return gson.fromJson(gson.toJson(this), AppEnvironment.class);
    }

}
