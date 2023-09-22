package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.enums.PipelineStepStatusType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;

public class DeployInAppVpcOutputDTO extends BaseOutputDTO {

    private ObjectId previousAppCommitStateId;
    private ObjectId previousActiveAppCommitPipelineStepId;
    private PipelineStepStatusType updatedPipelineStepStatusType;

    public DeployInAppVpcOutputDTO() {
    }

    public DeployInAppVpcOutputDTO(ObjectId previousAppCommitStateId, ObjectId previousActiveAppCommitPipelineStepId, PipelineStepStatusType updatedPipelineStepStatusType) {
        this.previousAppCommitStateId = previousAppCommitStateId;
        this.previousActiveAppCommitPipelineStepId = previousActiveAppCommitPipelineStepId;
        this.updatedPipelineStepStatusType = updatedPipelineStepStatusType;
    }

    public String getPreviousAppCommitStateId() {
        return previousAppCommitStateId != null ? previousAppCommitStateId.toHexString() : null;
    }

    @JsonIgnore
    public ObjectId getPreviousAppCommitStateObjectId() {
        return previousAppCommitStateId;
    }

    public void setPreviousAppCommitStateId(ObjectId previousAppCommitStateId) {
        this.previousAppCommitStateId = previousAppCommitStateId;
    }

    public String getPreviousActiveAppCommitPipelineStepId() {
        return previousActiveAppCommitPipelineStepId != null ? previousActiveAppCommitPipelineStepId.toHexString() : null;
    }

    @JsonIgnore
    public ObjectId getPreviousActiveAppCommitPipelineStepObjectId() {
        return previousActiveAppCommitPipelineStepId;
    }

    public void setPreviousActiveAppCommitPipelineStepId(ObjectId previousActiveAppCommitPipelineStepId) {
        this.previousActiveAppCommitPipelineStepId = previousActiveAppCommitPipelineStepId;
    }

    public PipelineStepStatusType getUpdatedPipelineStepStatusType() {
        return updatedPipelineStepStatusType;
    }

    public void setUpdatedPipelineStepStatusType(PipelineStepStatusType updatedPipelineStepStatusType) {
        this.updatedPipelineStepStatusType = updatedPipelineStepStatusType;
    }
}
