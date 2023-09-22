package com.cloudslip.pipeline.updated.model.universal;

import java.io.Serializable;

public class GitInfo implements Serializable {

    private String gitProvider;
    private String username;
    private String apiUrl;
    private String secretKey;

    public GitInfo() {
    }

    public GitInfo(String gitProvider, String username, String apiUrl, String secretKey) {
        this.gitProvider = gitProvider;
        this.username = username;
        this.apiUrl = apiUrl;
        this.secretKey = secretKey;
    }
    public String getGitProvider() {
        return gitProvider;
    }

    public void setGitProvider(String gitProvider) {
        this.gitProvider = gitProvider;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
