package com.cloudslip.pipeline.model.jenkins.wfapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StageFlowNode {

    private Links _links;
    private String id;
    private String status;
    private String parameterDescription;
    private long startTimeMillis;
    private long durationMillis;

    public Links get_links() {
        return _links;
    }

    public void set_links(final Links _links) {
        this._links = _links;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getParameterDescription() {
        return parameterDescription;
    }

    public void setParameterDescription(final String parameterDescription) {
        this.parameterDescription = parameterDescription;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(final long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(final long durationMillis) {
        this.durationMillis = durationMillis;
    }
}

