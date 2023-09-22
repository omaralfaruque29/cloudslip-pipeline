package com.cloudslip.pipeline.updated.dto;

import java.io.Serializable;

public class WebSocketMessageDTO implements Serializable {

    private WebSocketMessagePayload payload;
    private String targetUser;

    public WebSocketMessageDTO() {
    }

    public WebSocketMessageDTO(WebSocketMessagePayload payload, String targetUser) {
        this.payload = payload;
        this.targetUser = targetUser;
    }

    public WebSocketMessagePayload getPayload() {
        return payload;
    }

    public void setPayload(WebSocketMessagePayload payload) {
        this.payload = payload;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public void setValues(WebSocketMessagePayload payload, String targetUser) {
        this.payload = payload;
        this.targetUser = targetUser;
    }
}
