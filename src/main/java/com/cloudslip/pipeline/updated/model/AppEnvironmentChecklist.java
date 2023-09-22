package com.cloudslip.pipeline.updated.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "app_environment_checklist")
public class AppEnvironmentChecklist extends BaseEntity {

    @NotNull
    private ObjectId applicationId;

    @NotNull
    private ObjectId appEnvironmentId;

    private List<CheckItem> checklist;

    public AppEnvironmentChecklist() {
    }

    public AppEnvironmentChecklist(@NotNull ObjectId applicationId, @NotNull ObjectId appEnvironmentId, List<CheckItem> checklist) {
        this.applicationId = applicationId;
        this.appEnvironmentId = appEnvironmentId;
        this.checklist = checklist;
    }

    @JsonIgnore
    public ObjectId getApplicationObjectId() {
        return applicationId;
    }

    public String getApplicationId() {
        return applicationId.toHexString();
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    @JsonIgnore
    public ObjectId getAppEnvironmentObjectId() {
        return appEnvironmentId;
    }

    public String getAppEnvironmentId() {
        return appEnvironmentId.toHexString();
    }

    public void setAppEnvironmentId(ObjectId appEnvironmentId) {
        this.appEnvironmentId = appEnvironmentId;
    }

    public List<CheckItem> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<CheckItem> checklist) {
        this.checklist = checklist;
    }

    @JsonIgnore
    public int getCheckItemIndex(ObjectId checkItemId) {
        if (this.getChecklist().isEmpty()) {
            return -1;
        }
        for (int index = 0; index < this.getChecklist().size(); index++) {
            if (this.getChecklist().get(index).getId().equals(checkItemId.toString())) {
                return index;
            }
        }
        return -1;
    }
}
