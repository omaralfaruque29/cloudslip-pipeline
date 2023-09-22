package com.cloudslip.pipeline.updated.model;


import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.model.dummy.AppGitInfo;
import com.cloudslip.pipeline.updated.model.dummy.ManualScalingDetails;
import com.cloudslip.pipeline.updated.model.universal.Team;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "application")
public class Application extends BaseEntity {

    @NotNull
    private String name;

    @NotNull
    private String uniqueName;

    @NotNull
    private String packageName;

    private Team team;

    private AppCommit lastCommit;

    @NotNull
    private ApplicationCreationType creationType;

    @NotNull
    private ApplicationType type;

    private ApplicationBuildType buildType;

    private int minCpu;

    private int maxMemory;

    @NotNull
    private AppGitInfo gitInfo;

    private ApplicationState applicationState = ApplicationState.STARTING_INITIALIZATION;

    private boolean autoScalingEnabled;

    private ManualScalingDetails manualScalingDetails;

    private String nameSpace;

    private boolean ingressEnabled;

    private String tlsSecretName;

    @NotNull
    private int port = 8080;

    private int metricsPort = 8081;

    private String healthCheckUrl = "/";

    private String dockerRepoName;

    private boolean blueGreenDeploymentEnabled;

    private boolean istioEnabled;

    private boolean istioIngressGatewayEnabled;

    private String appBuildJobNameInJenkins;

    private ApplicationStatus appCreateStatus = ApplicationStatus.PENDING;

    private String webSocketTopic;

    private int webSocketSubscriberCount = 0;

    private String lastJenkinsBuildIdForAppCreation;

    private String lastJenkinsBuildStartTime;

    private Long lastJenkinsBuildEstimatedTime;

    private boolean useSameConfigInAllAppVpc = false;

    private String gitRepositoryName;

    private String gitBranchName;

    private ArrayList<String> tags;

    public Application() {
    }


    public String getGitRepositoryName() {
        return gitRepositoryName;
    }

    public void setGitRepositoryName(String gitRepositoryName) {
        this.gitRepositoryName = gitRepositoryName;
    }

    public String getGitBranchName() {
        if(gitBranchName == null) {
            return "master";
        }
        return gitBranchName;
    }

    public void setGitBranchName(String gitBranchName) {
        this.gitBranchName = gitBranchName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public AppCommit getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(AppCommit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public ApplicationCreationType getCreationType() {
        return creationType;
    }

    public void setCreationType(ApplicationCreationType creationType) {
        this.creationType = creationType;
    }

    public ApplicationType getType() {
        return type;
    }

    public void setType(ApplicationType type) {
        this.type = type;
    }

    public int getMinCpu() {
        return minCpu;
    }

    public void setMinCpu(int minCpu) {
        this.minCpu = minCpu;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public AppGitInfo getGitInfo() {
        return gitInfo;
    }

    public void setGitInfo(AppGitInfo gitInfo) {
        this.gitInfo = gitInfo;
    }

    public ApplicationState getApplicationState() {
        return applicationState;
    }

    public void setApplicationState(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }


    public boolean isAutoScalingEnabled() {
        return autoScalingEnabled;
    }

    public void setAutoScalingEnabled(boolean autoScalingEnabled) {
        this.autoScalingEnabled = autoScalingEnabled;
    }

    public ManualScalingDetails getManualScalingDetails() {
        return manualScalingDetails;
    }

    public void setManualScalingDetails(ManualScalingDetails manualScalingDetails) {
        this.manualScalingDetails = manualScalingDetails;
    }

    public ApplicationBuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(ApplicationBuildType buildType) {
        this.buildType = buildType;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public boolean isIngressEnabled() {
        return ingressEnabled;
    }

    public void setIngressEnabled(boolean ingressEnabled) {
        this.ingressEnabled = ingressEnabled;
    }

    public String getTlsSecretName() {
        return tlsSecretName;
    }

    public void setTlsSecretName(String tlsSecretName) {
        this.tlsSecretName = tlsSecretName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMetricsPort() {
        return metricsPort;
    }

    public void setMetricsPort(int metricsPort) {
        this.metricsPort = metricsPort;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl != null ? healthCheckUrl : "/";
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getDockerRepoName() {
        return dockerRepoName;
    }

    public void setDockerRepoName(String dockerRepoName) {
        this.dockerRepoName = dockerRepoName;
    }

    public boolean isBlueGreenDeploymentEnabled() {
        return blueGreenDeploymentEnabled;
    }

    public void setBlueGreenDeploymentEnabled(boolean blueGreenDeploymentEnabled) {
        this.blueGreenDeploymentEnabled = blueGreenDeploymentEnabled;
    }

    public boolean isIstioEnabled() {
        return istioEnabled;
    }

    public void setIstioEnabled(boolean istioEnabled) {
        this.istioEnabled = istioEnabled;
    }

    public boolean isIstioIngressGatewayEnabled() {
        return istioIngressGatewayEnabled;
    }

    public void setIstioIngressGatewayEnabled(boolean istioIngressGatewayEnabled) {
        this.istioIngressGatewayEnabled = istioIngressGatewayEnabled;
    }

    public String getAppBuildJobNameInJenkins() {
        return appBuildJobNameInJenkins;
    }

    public void setAppBuildJobNameInJenkins(String appBuildJobNameInJenkins) {
        this.appBuildJobNameInJenkins = appBuildJobNameInJenkins;
    }

    public ApplicationStatus getAppCreateStatus() {
        return appCreateStatus;
    }

    public void setAppCreateStatus(ApplicationStatus appCreateStatus) {
        this.appCreateStatus = appCreateStatus;
    }

    public String getWebSocketTopic() {
        return webSocketTopic;
    }

    public void setWebSocketTopic(String webSocketTopic) {
        this.webSocketTopic = webSocketTopic;
    }

    public int getWebSocketSubscriberCount() {
        return webSocketSubscriberCount;
    }

    public void setWebSocketSubscriberCount(int webSocketSubscriberCount) {
        this.webSocketSubscriberCount = webSocketSubscriberCount;
    }

    @JsonIgnore
    public String getLastJenkinsBuildIdForAppCreation() {
        return lastJenkinsBuildIdForAppCreation;
    }

    public void setLastJenkinsBuildIdForAppCreation(String lastJenkinsBuildIdForAppCreation) {
        this.lastJenkinsBuildIdForAppCreation = lastJenkinsBuildIdForAppCreation;
    }

    public String getLastJenkinsBuildStartTime() {
        return lastJenkinsBuildStartTime;
    }

    public void setLastJenkinsBuildStartTime(String lastJenkinsBuildStartTime) {
        this.lastJenkinsBuildStartTime = lastJenkinsBuildStartTime;
    }

    public Long getLastJenkinsBuildEstimatedTime() {
        return lastJenkinsBuildEstimatedTime;
    }

    public void setLastJenkinsBuildEstimatedTime(Long lastJenkinsBuildEstimatedTime) {
        this.lastJenkinsBuildEstimatedTime = lastJenkinsBuildEstimatedTime;
    }

    public boolean isUseSameConfigInAllAppVpc() {
        return useSameConfigInAllAppVpc;
    }

    public void setUseSameConfigInAllAppVpc(boolean useSameConfigInAllAppVpc) {
        this.useSameConfigInAllAppVpc = useSameConfigInAllAppVpc;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
}
