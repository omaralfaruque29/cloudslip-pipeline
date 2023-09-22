package com.cloudslip.pipeline.model.jenkins.wfapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {

    private Link self;
    private Link log;

    public Link getSelf() {
        return self;
    }

    public void setSelf(final Link self) {
        this.self = self;
    }

    public Link getLog() {
        return log;
    }

    public void setLog(final Link log) {
        this.log = log;
    }
}
