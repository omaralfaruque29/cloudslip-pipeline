package com.cloudslip.pipeline.updated.model.dummy;

import java.io.Serializable;

public class AppGitInfo implements Serializable {

    private String gitAppId;

    private String gitAppName;

    private String branchName;

    public AppGitInfo() {
    }

    public AppGitInfo(String gitAppId, String gitAppName, String branchName) {
        this.gitAppId = gitAppId;
        this.gitAppName = gitAppName;
        this.branchName = branchName;
    }

    public String getGitAppId() {
        return gitAppId;
    }

    public void setGitAppId(String gitAppId) {
        this.gitAppId = gitAppId;
    }

    public String getGitAppName() {
        return gitAppName;
    }

    public void setGitAppName(String gitAppName) {
        this.gitAppName = gitAppName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
