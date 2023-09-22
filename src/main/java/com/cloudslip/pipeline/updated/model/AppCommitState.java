package com.cloudslip.pipeline.updated.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "app_commit_state")
public class AppCommitState extends BaseEntity {

    @NotNull
    private AppCommit appCommit;

    @NotNull
    private List<AppEnvironmentStateForAppCommit> environmentStateList;


    public AppCommitState() {
    }

    public AppCommit getAppCommit() {
        return appCommit;
    }

    public void setAppCommit(AppCommit appCommit) {
        this.appCommit = appCommit;
    }

    public List<AppEnvironmentStateForAppCommit> getEnvironmentStateList() {
        return environmentStateList;
    }

    public void setEnvironmentStateList(List<AppEnvironmentStateForAppCommit> environmentStateList) {
        this.environmentStateList = environmentStateList;
    }

    @JsonIgnore
    public int getEnvironmentStateIndexForEnvironment (ObjectId appEnvironmentId) {
        int index = 0;
        if(this.environmentStateList == null){
            return -1;
        }
        for (AppEnvironmentStateForAppCommit appEnvState : this.environmentStateList) {
            if(appEnvState.getAppEnvironment().getId().equals(appEnvironmentId.toString())){
                return index;
            }
            index++;
        }
        return -1;
    }

    public AppCommitState clone(Gson gson) {
        return gson.fromJson(gson.toJson(this), AppCommitState.class);
    }
}
