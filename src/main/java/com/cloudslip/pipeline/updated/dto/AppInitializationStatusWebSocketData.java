package com.cloudslip.pipeline.updated.dto;

import java.io.Serializable;

public class AppInitializationStatusWebSocketData implements Serializable {

    private String log;
    private Long estimatedTime;

    public AppInitializationStatusWebSocketData() {
    }

    public AppInitializationStatusWebSocketData(String log, Long estimatedTime) {
        this.log = log;
        this.estimatedTime = estimatedTime;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Long estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setValues(String log) {
        this.log = log;
    }

    public void setValues(String log, Long estimatedTime) {
        this.log = log;
        this.estimatedTime = estimatedTime;
    }
}
