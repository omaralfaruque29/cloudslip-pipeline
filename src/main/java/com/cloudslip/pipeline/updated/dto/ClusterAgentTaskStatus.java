package com.cloudslip.pipeline.updated.dto;

import java.io.Serializable;

public class ClusterAgentTaskStatus implements Serializable {

    private String taskType;
    private String status;
    private String message;
    private String log;

    public ClusterAgentTaskStatus() {
    }

    public ClusterAgentTaskStatus(String taskType, String status, String message, String log) {
        this.taskType = taskType;
        this.status = status;
        this.message = message;
        this.log = log;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
