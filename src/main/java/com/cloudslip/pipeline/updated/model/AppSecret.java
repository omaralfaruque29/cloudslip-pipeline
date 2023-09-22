package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.model.dummy.AppSecretEnvironment;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "app_secrets")
public class AppSecret extends BaseEntity {

    private ObjectId applicationId;

    private String secretName;

    private String uniqueName;

    private ObjectId companyId;

    private List<NameValue> dataList;

    private List<AppSecretEnvironment> environmentList;

    private boolean useAsEnvironmentVariable;

    public AppSecret() {
    }

    public AppSecret(ObjectId applicationId, String secretName, String uniqueName, ObjectId companyId, List<NameValue> dataList, List<AppSecretEnvironment> environmentList, boolean useAsEnvironmentVariable) {
        this.applicationId = applicationId;
        this.secretName = secretName;
        this.uniqueName = uniqueName;
        this.companyId = companyId;
        this.dataList = dataList;
        this.environmentList = environmentList;
        this.useAsEnvironmentVariable = useAsEnvironmentVariable;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public ObjectId getCompanyId() {
        return companyId;
    }

    public void setCompanyId(ObjectId companyId) {
        this.companyId = companyId;
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
