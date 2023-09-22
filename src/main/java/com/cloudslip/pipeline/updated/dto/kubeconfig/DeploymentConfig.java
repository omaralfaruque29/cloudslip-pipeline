package com.cloudslip.pipeline.updated.dto.kubeconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeploymentConfig {

    private String name;
    private String namespace;
    private int replicas;
    private RollingUpdateConfig rollingUpdateConfig;
    private List<PodContainerSpec> containerSpecList;

    public DeploymentConfig() {

    }

    public DeploymentConfig(String name, String namespace, int replicas, String dockerRepoName, String gitCommitId, int maxCpu, int maxMemory, int applicationPort, int metricsPort, String healthCheckUrl, List<String> envFromSecretList) {
        this.name = name;
        this.namespace = namespace;
        this.replicas = replicas;
        this.rollingUpdateConfig = new RollingUpdateConfig(replicas + 1);
        this.containerSpecList = new ArrayList<>(Arrays.asList(new PodContainerSpec(name, dockerRepoName, gitCommitId, maxCpu, maxMemory, healthCheckUrl, envFromSecretList, applicationPort, metricsPort)));
    }

    public DeploymentConfig(String name, String namespace, int replicas, int maxSurge, String dockerRepoName, String gitCommitId, int maxCpu, int maxMemory, int applicationPort, int metricsPort, String healthCheckUrl, List<String> envFromSecretList) {
        this(name, namespace, replicas, dockerRepoName, gitCommitId, maxCpu, maxMemory, applicationPort, metricsPort, healthCheckUrl, envFromSecretList);
        this.rollingUpdateConfig = new RollingUpdateConfig(maxSurge);
    }

    public DeploymentConfig(String name, String namespace, int replicas, int maxSurge, int maxUnavailable, String dockerRepoName, String gitCommitId, int maxCpu, int maxMemory, int applicationPort, int metricsPort, String healthCheckUrl, List<String> envFromSecretList) {
        this(name, namespace, replicas, dockerRepoName, gitCommitId, maxCpu, maxMemory, applicationPort, metricsPort, healthCheckUrl, envFromSecretList);
        this.rollingUpdateConfig = new RollingUpdateConfig(maxSurge, maxUnavailable);
    }

    public DeploymentConfig(String name, String namespace, int replicas, RollingUpdateConfig rollingUpdateConfig, List<PodContainerSpec> containerSpecList) {
        this.name = name;
        this.namespace = namespace;
        this.replicas = replicas;
        this.rollingUpdateConfig = rollingUpdateConfig;
        this.containerSpecList = containerSpecList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public RollingUpdateConfig getRollingUpdateConfig() {
        return rollingUpdateConfig;
    }

    public void setRollingUpdateConfig(RollingUpdateConfig rollingUpdateConfig) {
        this.rollingUpdateConfig = rollingUpdateConfig;
    }

    public List<PodContainerSpec> getContainerSpecList() {
        return containerSpecList;
    }

    public void setContainerSpecList(List<PodContainerSpec> containerSpecList) {
        this.containerSpecList = containerSpecList;
    }
}
