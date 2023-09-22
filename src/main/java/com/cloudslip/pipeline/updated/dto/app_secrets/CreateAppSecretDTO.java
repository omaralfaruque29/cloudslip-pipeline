package com.cloudslip.pipeline.updated.dto.app_secrets;

import com.cloudslip.pipeline.updated.dto.BaseInputDTO;
import com.cloudslip.pipeline.updated.model.dummy.AppSecretEnvironment;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import org.bson.types.ObjectId;

import java.util.List;

public class CreateAppSecretDTO extends BaseInputDTO {

    private String secretName;

    private ObjectId applicationId;

    private List<NameValue> dataList;

    private List<AppSecretEnvironment> environmentList;

    private boolean useAsEnvironmentVariable;

    public CreateAppSecretDTO() {
    }

    public CreateAppSecretDTO(String secretName, ObjectId applicationId, List<NameValue> dataList, List<AppSecretEnvironment> environmentList, boolean useAsEnvironmentVariable) {
        this.secretName = secretName;
        this.applicationId = applicationId;
        this.dataList = dataList;
        this.environmentList = environmentList;
        this.useAsEnvironmentVariable = useAsEnvironmentVariable;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public List<NameValue> getDataList() {
        return dataList;
    }

    public void setDataList(List<NameValue> dataList) {
        this.dataList = dataList;
    }

    public List<AppSecretEnvironment> getEnvironmentList() {
        return environmentList;
    }

    public void setEnvironmentList(List<AppSecretEnvironment> environmentList) {
        this.environmentList = environmentList;
    }

    public boolean isUseAsEnvironmentVariable() {
        return useAsEnvironmentVariable;
    }

    public void setUseAsEnvironmentVariable(boolean useAsEnvironmentVariable) {
        this.useAsEnvironmentVariable = useAsEnvironmentVariable;
    }
}
