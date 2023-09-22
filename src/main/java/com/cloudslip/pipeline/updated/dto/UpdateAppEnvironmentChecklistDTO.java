package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.enums.Authority;
import org.bson.types.ObjectId;

import java.util.List;

public class UpdateAppEnvironmentChecklistDTO extends BaseInputDTO{

    private ObjectId appEnvChecklistId;
    private ObjectId checkItemId;
    private String message;
    private List<Authority> authority;

    public UpdateAppEnvironmentChecklistDTO() {
    }

    public ObjectId getAppEnvChecklistId() {
        return appEnvChecklistId;
    }

    public void setAppEnvChecklistId(ObjectId appEnvChecklistId) {
        this.appEnvChecklistId = appEnvChecklistId;
    }

    public ObjectId getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(ObjectId checkItemId) {
        this.checkItemId = checkItemId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Authority> getAuthority() {
        return authority;
    }

    public void setAuthority(List<Authority> authority) {
        this.authority = authority;
    }
}
