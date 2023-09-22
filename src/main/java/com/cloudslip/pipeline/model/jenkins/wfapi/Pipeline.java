package com.cloudslip.pipeline.model.jenkins.wfapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pipeline {

    private Links _links;
    private String id;
    private String status;
    private long startTimeMillis;
    private long durationMillis;
    private List<Stage> stages;

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

    public List<Stage> getStages() {
        return stages;
    }

    public void setStages(final List<Stage> stages) {
        this.stages = stages;
    }
}
