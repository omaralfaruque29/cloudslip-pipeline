package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.dummy.CheckItemForAppCommit;

import java.util.List;

public class UpdateAppEnvironmentStateChecklistInputDTO extends BaseInputDTO {

    private List<CheckItemForAppCommit> updatedChecklist;

    public UpdateAppEnvironmentStateChecklistInputDTO() {
    }

    public List<CheckItemForAppCommit> getUpdatedChecklist() {
        return updatedChecklist;
    }

    public void setUpdatedChecklist(List<CheckItemForAppCommit> updatedChecklist) {
        this.updatedChecklist = updatedChecklist;
    }
}
