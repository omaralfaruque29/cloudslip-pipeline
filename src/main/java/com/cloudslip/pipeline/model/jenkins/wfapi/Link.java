package com.cloudslip.pipeline.model.jenkins.wfapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    private String href;

    public String getHref() {
        return href;
    }

    public void setHref(final String href) {
        this.href = href;
    }
}
