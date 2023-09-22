package com.cloudslip.pipeline.updated.dto;

public class IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO extends BaseInputDTO {

    private String webSocketTopic;

    public IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO() {
    }

    public IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO(String webSocketTopic) {
        this.webSocketTopic = webSocketTopic;
    }

    public String getWebSocketTopic() {
        return webSocketTopic;
    }

    public void setWebSocketTopic(String webSocketTopic) {
        this.webSocketTopic = webSocketTopic;
    }
}
