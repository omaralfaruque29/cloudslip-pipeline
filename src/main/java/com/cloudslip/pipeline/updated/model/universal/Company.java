package com.cloudslip.pipeline.updated.model.universal;

import com.cloudslip.pipeline.updated.model.BaseEntity;
import com.cloudslip.pipeline.updated.model.dummy.DockerHubInfo;

public class Company extends BaseEntity {

    private String name;
    private String businessEmail;
    private String website;
    private String address;
    private String phoneNo;
    private boolean enabled = true;
    private GitInfo gitInfo;
    private DockerHubInfo dockerHubInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public GitInfo getGitInfo() {
        return gitInfo;
    }

    public void setGitInfo(GitInfo gitInfo) {
        this.gitInfo = gitInfo;
    }

    public DockerHubInfo getDockerHubInfo() {
        return dockerHubInfo;
    }

    public void setDockerHubInfo(DockerHubInfo dockerHubInfo) {
        this.dockerHubInfo = dockerHubInfo;
    }
}
