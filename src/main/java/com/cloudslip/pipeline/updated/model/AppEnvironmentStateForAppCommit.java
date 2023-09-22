package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.model.dummy.CheckItemForAppCommit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "app_environment_state_for_app_commit")
public class AppEnvironmentStateForAppCommit extends BaseEntity {

    ObjectId appCommitId;

    @NotNull
    private AppEnvironment appEnvironment;

    @NotNull
    private List<AppCommitPipelineStep> steps;

    private boolean enabled = true;

    private List<CheckItemForAppCommit> checkList;


    public AppEnvironmentStateForAppCommit() {
    }

    public ObjectId getAppCommitId() {
        return appCommitId;
    }

    public void setAppCommitId(ObjectId appCommitId) {
        this.appCommitId = appCommitId;
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public List<AppCommitPipelineStep> getSteps() {
        return steps;
    }

    public void setSteps(List<AppCommitPipelineStep> steps) {
        this.steps = steps;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<CheckItemForAppCommit> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<CheckItemForAppCommit> checkList) {
        this.checkList = checkList;
    }

    @JsonIgnore
    public int getCommitPipelineStepIndex (AppPipelineStep appPipelineStep) {
        if(this.steps == null){
            return -1;
        }
        for (int index = 0; index < this.steps.size(); index++) {
            if (this.steps.get(index).getAppPipelineStep().getId().equals(appPipelineStep.getId())) {
                return index;
            }
        }
        return -1;
    }

    @JsonIgnore
    public int getCheckItemIndex (ObjectId checkItemId) {
        if(this.checkList == null){
            return -1;
        }
        for (int index = 0; index < this.checkList.size(); index++) {
            if (this.checkList.get(index).getCheckItem().getId().equals(checkItemId.toString())) {
                return index;
            }
        }
        return -1;
    }
}
