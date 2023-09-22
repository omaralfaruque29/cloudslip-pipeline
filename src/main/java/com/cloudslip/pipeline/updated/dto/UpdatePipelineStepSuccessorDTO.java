package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.dto_helper.AppPipelineStepHelper;
import org.bson.types.ObjectId;

import java.util.List;

public class UpdatePipelineStepSuccessorDTO extends BaseInputDTO{
    private ObjectId pipelineStepId;
    private List<AppPipelineStepHelper> successors;

    public UpdatePipelineStepSuccessorDTO() {
    }

    public UpdatePipelineStepSuccessorDTO(ObjectId pipelineStepId, List<AppPipelineStepHelper> successors) {
        this.pipelineStepId = pipelineStepId;
        this.successors = successors;
    }

    public ObjectId getPipelineStepId() {
        return pipelineStepId;
    }

    public void setPipelineStepId(ObjectId pipelineStepId) {
        this.pipelineStepId = pipelineStepId;
    }

    public List<AppPipelineStepHelper> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<AppPipelineStepHelper> successors) {
        this.successors = successors;
    }
}
