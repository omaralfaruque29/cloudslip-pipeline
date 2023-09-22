package com.cloudslip.pipeline.updated.dto.kubeconfig;


import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import com.cloudslip.pipeline.updated.util.Utils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecretConfig implements Serializable {

    private String secretName;
    private String namespace;
    private List<NameValue> data;

    public SecretConfig() {
    }

    public SecretConfig(String secretName, String namespace, List<NameValue> data) {
        this.secretName = secretName;
        this.namespace = namespace;
        this.data = data;
    }

    public void generateForDockerhubCredentials(@NotNull String secretName, @NotNull String namespace, @NotNull String username, @NotNull String password) {
        this.secretName = secretName;
        this.namespace = namespace;
        username = Utils.getBase64EncodedString(username);
        data = new ArrayList<>(Arrays.asList(new NameValue(username, password)));
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<NameValue> getData() {
        return data;
    }

    public void setData(List<NameValue> data) {
        this.data = data;
    }
}
