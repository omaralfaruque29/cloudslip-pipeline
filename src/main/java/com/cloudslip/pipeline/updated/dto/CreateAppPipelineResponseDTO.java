package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.model.AppPipelineStep;

import java.util.List;

public class CreateAppPipelineResponseDTO extends BaseInputDTO{

    private List<AppPipelineStep> selectedAppPipeLineSteps;
    private List<AppPipelineStep> unselectedAppPipeLineSteps;

    public CreateAppPipelineResponseDTO() {
    }

    public CreateAppPipelineResponseDTO(List<AppPipelineStep> selectedAppPipeLineSteps, List<AppPipelineStep> unselectedAppPipeLineSteps) {
        this.selectedAppPipeLineSteps = selectedAppPipeLineSteps;
        this.unselectedAppPipeLineSteps = unselectedAppPipeLineSteps;
    }

    public List<AppPipelineStep> getSelectedAppPipeLineSteps() {
        return selectedAppPipeLineSteps;
    }

    public void setSelectedAppPipeLineSteps(List<AppPipelineStep> selectedAppPipeLineSteps) {
        this.selectedAppPipeLineSteps = selectedAppPipeLineSteps;
    }

    public List<AppPipelineStep> getUnselectedAppPipeLineSteps() {
        return unselectedAppPipeLineSteps;
    }

    public void setUnselectedAppPipeLineSteps(List<AppPipelineStep> unselectedAppPipeLineSteps) {
        this.unselectedAppPipeLineSteps = unselectedAppPipeLineSteps;
    }
}
