package com.cloudslip.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TriggerCreateAppRequest {

    private String name;
    private String domainUrl;
    private int replicas;

    private String packageNamePrefix = "com.";

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(final String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(final int replicas) {
        this.replicas = replicas;
    }

    public String getPackageName() {
        return packageNamePrefix.concat(getName());
    }
}
