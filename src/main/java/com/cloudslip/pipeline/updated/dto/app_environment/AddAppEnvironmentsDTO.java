package com.cloudslip.pipeline.updated.dto.app_environment;

import com.cloudslip.pipeline.updated.dto.BaseInputDTO;
import org.bson.types.ObjectId;

import java.util.List;

public class AddAppEnvironmentsDTO extends BaseInputDTO {
    private ObjectId companyId;
    private ObjectId applicationId;
    private boolean useSameConfig = false;
    private boolean autoSuccessorEnabled = true;
    private List<EnvironmentDTO> environmentList;
    private boolean forceRemove = false;

    public AddAppEnvironmentsDTO() {
    }

    public boolean isUseSameConfig() {
        return useSameConfig;
    }

    public void setUseSameConfig(boolean useSameConfig) {
        this.useSameConfig = useSameConfig;
    }

    public AddAppEnvironmentsDTO(ObjectId companyId, ObjectId applicationId, List<EnvironmentDTO> environmentList) {
        this.companyId = companyId;
        this.applicationId = applicationId;
        this.environmentList = environmentList;
    }

    public boolean isAutoSuccessorEnabled() {
        return autoSuccessorEnabled;
    }

    public void setAutoSuccessorEnabled(boolean autoSuccessorEnabled) {
        this.autoSuccessorEnabled = autoSuccessorEnabled;
    }

    public ObjectId getCompanyId() {
        return companyId;
    }

    public void setCompanyId(ObjectId companyId) {
        this.companyId = companyId;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public List<EnvironmentDTO> getEnvironmentList() {
        return environmentList;
    }

    public void setEnvironmentList(List<EnvironmentDTO> environmentList) {
        this.environmentList = environmentList;
    }

    public boolean isForceRemove() {
        return forceRemove;
    }

    public void setForceRemove(boolean forceRemove) {
        this.forceRemove = forceRemove;
    }
}
