package com.cloudslip.pipeline.updated.dto.kubeconfig;

import com.cloudslip.pipeline.updated.model.dummy.NameValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PodContainerSpec {
    private String name;
    private String imagePullPolicy = "Always";
    private String image;
    private PodResourceSpec resourceSpec;
    private List<String> envFromSecretList;
    private List<NameValue> environmentVariableList;
    private List<Integer> portList;
    private String healthCheckUrl;


    public PodContainerSpec() {
        this.environmentVariableList = new ArrayList<>();
    }

    public PodContainerSpec(String name, String imagePullPolicy, String image, PodResourceSpec resourceSpec, List<String> envFromSecretList, List<NameValue> environmentVariableList, List<Integer> portList, String healthCheckUrl) {
        this.name = name;
        this.imagePullPolicy = imagePullPolicy;
        this.image = image;
        this.resourceSpec = resourceSpec;
        this.envFromSecretList = envFromSecretList;
        this.environmentVariableList = environmentVariableList;
        this.portList = portList;
        this.healthCheckUrl = healthCheckUrl;
    }

    public PodContainerSpec(String name, String dockerRepoName, String gitCommitId, int maxCpu, int maxMemory, String healthCheckUrl, List<String> envFromSecretList ,Integer... ports) {
        this.name = name;
        this.image = dockerRepoName + ":" + gitCommitId;
        this.resourceSpec = new PodResourceSpec(maxCpu, maxMemory);
        this.envFromSecretList = envFromSecretList;
        this.environmentVariableList = new ArrayList<>(Arrays.asList(new NameValue("GIT_COMMIT", gitCommitId)));
        this.portList = new ArrayList<>(Arrays.asList(ports));
        this.healthCheckUrl = healthCheckUrl;
    }

    public PodContainerSpec(String name, String dockerRepoName, String gitCommitId, int maxCpu, int maxMemory, String imagePullPolicy, String healthCheckUrl, List<String> envFromSecretList, Integer... ports) {
        this(name, dockerRepoName, gitCommitId, maxCpu, maxMemory, healthCheckUrl, envFromSecretList, ports);
        this.imagePullPolicy = imagePullPolicy;
    }

    public void addEnvironmentVariable(NameValue envNameValue) {
        this.environmentVariableList.add(envNameValue);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public PodResourceSpec getResourceSpec() {
        return resourceSpec;
    }

    public void setResourceSpec(PodResourceSpec resourceSpec) {
        this.resourceSpec = resourceSpec;
    }

    public List<NameValue> getEnvironmentVariableList() {
        return environmentVariableList;
    }

    public void setEnvironmentVariableList(List<NameValue> environmentVariableList) {
        this.environmentVariableList = environmentVariableList;
    }

    public List<String> getEnvFromSecretList() {
        return envFromSecretList;
    }

    public void setEnvFromSecretList(List<String> envFromSecretList) {
        this.envFromSecretList = envFromSecretList;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public List<Integer> getPortList() {
        return portList;
    }

    public void setPortList(List<Integer> portList) {
        this.portList = portList;
    }
}
