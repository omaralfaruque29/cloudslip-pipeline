package com.cloudslip.pipeline.updated.model.dummy;

import com.cloudslip.pipeline.updated.enums.TriggerMode;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class SuccessorPipelineStep implements Serializable {

    @NotNull
    private AppPipelineStep appPipelineStep;

    private TriggerMode triggerMode = TriggerMode.MANUAL;

    public SuccessorPipelineStep() {
    }

    public SuccessorPipelineStep(@NotNull AppPipelineStep appPipelineStep, TriggerMode triggerMode) {
        this.appPipelineStep = appPipelineStep;
        this.triggerMode = triggerMode;
    }

    public AppPipelineStep getAppPipelineStep() {
        return appPipelineStep;
    }

    public void setAppPipelineStep(AppPipelineStep appPipelineStep) {
        this.appPipelineStep = appPipelineStep;
    }

    public TriggerMode getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(TriggerMode triggerMode) {
        this.triggerMode = triggerMode;
    }
}
