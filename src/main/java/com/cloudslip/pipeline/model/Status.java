package com.cloudslip.pipeline.model;

public enum Status {

    QUEUED("QUEUED"),
    RUNNING("IN_PROGRESS"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    NEVER("NEVER");

    Status(final String statusName) {
        this.statusName = statusName;
    }

    private String statusName;

    public String getStatusName() {
        return statusName;
    }
}
