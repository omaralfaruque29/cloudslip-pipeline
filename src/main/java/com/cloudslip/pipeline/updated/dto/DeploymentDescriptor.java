package com.cloudslip.pipeline.updated.dto;


import java.io.Serializable;

public class DeploymentDescriptor implements Serializable {

    private String imageName;
    private String dockerRegistryUrl;
    private int maxMemory;
    private int replica;

    public DeploymentDescriptor() {
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDockerRegistryUrl() {
        return dockerRegistryUrl;
    }

    public void setDockerRegistryUrl(String dockerRegistryUrl) {
        this.dockerRegistryUrl = dockerRegistryUrl;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getReplica() {
        return replica;
    }

    public void setReplica(int replica) {
        this.replica = replica;
    }
}
