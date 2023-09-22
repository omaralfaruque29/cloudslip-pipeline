package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.dto.kubeconfig.IngressConfig;
import com.cloudslip.pipeline.updated.model.dummy.ResourceDetails;
import com.cloudslip.pipeline.updated.model.dummy.DeployedCommit;
import com.cloudslip.pipeline.updated.model.universal.Vpc;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;


@Document(collection = "app_vpc")
public class AppVpc extends BaseEntity {

    @NotNull
    private Vpc vpc;

    private ObjectId applicationId;

    private ObjectId appEnvironmentId;

    private DeployedCommit mainCommit;

    private DeployedCommit canaryCommit;

    private DeployedCommit previousCommit;

    private ResourceDetails resourceDetails;

    private String deploymentName;

    private IngressConfig ingressConfig;

    private boolean autoScalingEnabled = false;

    private boolean canaryDeploymentEnabled = false;

    public AppVpc() {
    }

    public Vpc getVpc() {
        return vpc;
    }

    public void setVpc(Vpc vpc) {
        this.vpc = vpc;
    }

    public DeployedCommit getMainCommit() {
        return mainCommit;
    }

    public void setMainCommit(DeployedCommit mainCommit) {
        this.mainCommit = mainCommit;
    }

    public DeployedCommit getCanaryCommit() {
        return canaryCommit;
    }

    public void setCanaryCommit(DeployedCommit canaryCommit) {
        this.canaryCommit = canaryCommit;
    }

    public String getAppEnvironmentId() {
        return appEnvironmentId.toHexString();
    }

    public ObjectId getAppEnvironmentObjectId() {
        return appEnvironmentId;
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public ResourceDetails getResourceDetails() {
        return resourceDetails;
    }

    public void setResourceDetails(ResourceDetails resourceDetails) {
        this.resourceDetails = resourceDetails;
    }

    public String getInternalUrl() {
        return "http://" + deploymentName;
    }

    public String getInternalCanaryUrl() {
        return this.canaryCommit != null ? "http://" + deploymentName + "-canary" : null;
    }

    @JsonIgnore
    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public IngressConfig getIngressConfig() {
        return ingressConfig;
    }

    public void setIngressConfig(IngressConfig ingressConfig) {
        this.ingressConfig = ingressConfig;
    }

    public DeployedCommit getPreviousCommit() {
        if (previousCommit == null) {
            return null;
        }
        return previousCommit;
    }

    public void setPreviousCommit(DeployedCommit previousCommit) {
        this.previousCommit = previousCommit;
    }

    public boolean isAutoScalingEnabled() {
        return autoScalingEnabled;
    }

    public void setAutoScalingEnabled(boolean autoScalingEnabled) {
        this.autoScalingEnabled = autoScalingEnabled;
    }

    public boolean isCanaryDeploymentEnabled() {
        return canaryDeploymentEnabled;
    }

    public void setCanaryDeploymentEnabled(boolean canaryDeploymentEnabled) {
        this.canaryDeploymentEnabled = canaryDeploymentEnabled;
    }

    public AppVpc clone(Gson gson) {
        return gson.fromJson(gson.toJson(this), AppVpc.class);
    }
}
