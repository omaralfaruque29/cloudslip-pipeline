package com.cloudslip.pipeline.model.jenkins;

import java.util.Date;

public class PipelineStep {

    private String pipelineStepName;
    private String pipelineStepStatus;
    private int pipelineStepIndex;
    private Date pipelineStepTimeStarted;
    private Long pipelineStepEstimatedDuration;

    public PipelineStep() {
    }

    public PipelineStep(final String pipelineStepName, final String pipelineStepStatus, final int pipelineStepIndex, final Date pipelineStepTimeStarted, final Long pipelineStepEstimatedDuration) {
        this.pipelineStepName = pipelineStepName;
        this.pipelineStepStatus = pipelineStepStatus;
        this.pipelineStepIndex = pipelineStepIndex;
        this.pipelineStepTimeStarted = pipelineStepTimeStarted;
        this.pipelineStepEstimatedDuration = pipelineStepEstimatedDuration;
    }

    public String getPipelineStepName() {
        return pipelineStepName;
    }

    public void setPipelineStepName(final String pipelineStepName) {
        this.pipelineStepName = pipelineStepName;
    }

    public String getPipelineStepStatus() {
        return pipelineStepStatus;
    }

    public void setPipelineStepStatus(final String pipelineStepStatus) {
        this.pipelineStepStatus = pipelineStepStatus;
    }

    public int getPipelineStepIndex() {
        return pipelineStepIndex;
    }

    public void setPipelineStepIndex(final int pipelineStepIndex) {
        this.pipelineStepIndex = pipelineStepIndex;
    }

    public Date getPipelineStepTimeStarted() {
        return pipelineStepTimeStarted;
    }

    public void setPipelineStepTimeStarted(final Date pipelineStepTimeStarted) {
        this.pipelineStepTimeStarted = pipelineStepTimeStarted;
    }

    public Long getPipelineStepEstimatedDuration() {
        return pipelineStepEstimatedDuration;
    }

    public void setPipelineStepEstimatedDuration(final Long pipelineStepEstimatedDuration) {
        this.pipelineStepEstimatedDuration = pipelineStepEstimatedDuration;
    }
}
