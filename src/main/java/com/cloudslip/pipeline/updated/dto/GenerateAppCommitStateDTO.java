package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class GenerateAppCommitStateDTO extends BaseInputDTO  {
    private ObjectId applicationId;
    private String payloadInput;
    private String userAgent;
    private String githubDelivery;
    private String githubEvent;

    public GenerateAppCommitStateDTO() {
    }

    public GenerateAppCommitStateDTO(ObjectId applicationId, String payloadInput, String userAgent, String githubDelivery, String githubEvent) {
        this.applicationId = applicationId;
        this.payloadInput = payloadInput;
        this.userAgent = userAgent;
        this.githubDelivery = githubDelivery;
        this.githubEvent = githubEvent;
    }

    public ObjectId getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(ObjectId applicationId) {
        this.applicationId = applicationId;
    }

    public String getPayloadInput() {
        return payloadInput;
    }

    public void setPayloadInput(String payloadInput) {
        this.payloadInput = payloadInput;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getGithubDelivery() {
        return githubDelivery;
    }

    public void setGithubDelivery(String githubDelivery) {
        this.githubDelivery = githubDelivery;
    }

    public String getGithubEvent() {
        return githubEvent;
    }

    public void setGithubEvent(String githubEvent) {
        this.githubEvent = githubEvent;
    }
}
