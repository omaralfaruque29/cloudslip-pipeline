package com.cloudslip.pipeline.updated.model.dummy;


import com.cloudslip.pipeline.updated.enums.DockerRegistryType;

import java.io.Serializable;

public class DockerHubInfo implements Serializable {

    private DockerRegistryType dockerRegistryType;
    private String dockerRegistryServer;
    private String dockerhubId;
    private String dockerhubPassword;
    private String dockerhubEmail;
    private String dockerConfigData;

    public DockerHubInfo() {}

    public DockerHubInfo(String dockerhubId, String dockerhubPassword) {
        this.dockerhubId = dockerhubId;
        this.dockerhubPassword = dockerhubPassword;
    }

    public DockerHubInfo(DockerRegistryType dockerRegistryType, String dockerRegistryServer, String dockerhubId, String dockerhubPassword, String dockerhubEmail) {
        this.dockerRegistryType = dockerRegistryType;
        this.dockerRegistryServer = dockerRegistryServer;
        this.dockerhubId = dockerhubId;
        this.dockerhubPassword = dockerhubPassword;
        this.dockerhubEmail = dockerhubEmail;
    }

    public DockerHubInfo(DockerRegistryType dockerRegistryType, String dockerRegistryServer, String dockerhubId, String dockerhubPassword, String dockerhubEmail, String dockerConfigData) {
        this.dockerRegistryType = dockerRegistryType;
        this.dockerRegistryServer = dockerRegistryServer;
        this.dockerhubId = dockerhubId;
        this.dockerhubPassword = dockerhubPassword;
        this.dockerhubEmail = dockerhubEmail;
        this.dockerConfigData = dockerConfigData;
    }

    public DockerRegistryType getDockerRegistryType() {
        return dockerRegistryType;
    }

    public void setDockerRegistryType(DockerRegistryType dockerRegistryType) {
        this.dockerRegistryType = dockerRegistryType;
    }

    public String getDockerRegistryServer() {
        return dockerRegistryServer;
    }

    public void setDockerRegistryServer(String dockerRegistryServer) {
        this.dockerRegistryServer = dockerRegistryServer;
    }

    public String getDockerhubId() {
        return dockerhubId;
    }

    public void setDockerhubId(String dockerhubId) {
        this.dockerhubId = dockerhubId;
    }

    public String getDockerhubPassword() {
        return dockerhubPassword;
    }

    public void setDockerhubPassword(String dockerhubPassword) {
        this.dockerhubPassword = dockerhubPassword;
    }

    public String getDockerhubEmail() {
        return dockerhubEmail;
    }

    public void setDockerhubEmail(String dockerhubEmail) {
        this.dockerhubEmail = dockerhubEmail;
    }

    public String getDockerConfigData() {
        return dockerConfigData;
    }

    public void setDockerConfigData(String dockerConfigData) {
        this.dockerConfigData = dockerConfigData;
    }
}
