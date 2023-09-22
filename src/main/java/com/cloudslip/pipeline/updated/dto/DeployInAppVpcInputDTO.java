package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.GitInfo;

public class DeployInAppVpcInputDTO extends BaseInputDTO {

    private AppCommitPipelineStep appCommitPipelineStep;
    private AppCommit appCommit;
    private AppVpc appVpc;
    private Application application;
    private GitInfo companyGitInfo;
    boolean canaryDeployment = false;

    public DeployInAppVpcInputDTO() {
    }

    public DeployInAppVpcInputDTO(AppCommitPipelineStep appCommitPipelineStep, AppCommit appCommit, AppVpc appVpc, Application application, GitInfo companyGitInfo, boolean canaryDeployment) {
        this.appCommitPipelineStep = appCommitPipelineStep;
        this.appCommit = appCommit;
        this.appVpc = appVpc;
        this.application = application;
        this.companyGitInfo = companyGitInfo;
        this.canaryDeployment = canaryDeployment;
    }

    public AppCommitPipelineStep getAppCommitPipelineStep() {
        return appCommitPipelineStep;
    }

    public void setAppCommitPipelineStep(AppCommitPipelineStep appCommitPipelineStep) {
        this.appCommitPipelineStep = appCommitPipelineStep;
    }

    public AppCommit getAppCommit() {
        return appCommit;
    }

    public void setAppCommit(AppCommit appCommit) {
        this.appCommit = appCommit;
    }

    public AppVpc getAppVpc() {
        return appVpc;
    }

    public void setAppVpc(AppVpc appVpc) {
        this.appVpc = appVpc;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public GitInfo getCompanyGitInfo() {
        return companyGitInfo;
    }

    public void setCompanyGitInfo(GitInfo companyGitInfo) {
        this.companyGitInfo = companyGitInfo;
    }

    public boolean isCanaryDeployment() {
        return canaryDeployment;
    }

    public void setCanaryDeployment(boolean canaryDeployment) {
        this.canaryDeployment = canaryDeployment;
    }
}
