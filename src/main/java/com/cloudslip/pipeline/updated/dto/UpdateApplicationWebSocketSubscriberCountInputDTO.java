package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.enums.UpdateApplicationWebSocketSubscriberCountType;

public class UpdateApplicationWebSocketSubscriberCountInputDTO extends BaseInputDTO {

    private String webSocketTopic;

    private UpdateApplicationWebSocketSubscriberCountType type;

    public UpdateApplicationWebSocketSubscriberCountInputDTO() {
    }

    public UpdateApplicationWebSocketSubscriberCountInputDTO(String webSocketTopic, UpdateApplicationWebSocketSubscriberCountType type) {
        this.webSocketTopic = webSocketTopic;
        this.type = type;
    }

    public String getWebSocketTopic() {
        return webSocketTopic;
    }

    public void setWebSocketTopic(String webSocketTopic) {
        this.webSocketTopic = webSocketTopic;
    }

    public UpdateApplicationWebSocketSubscriberCountType getType() {
        return type;
    }

    public void setType(UpdateApplicationWebSocketSubscriberCountType type) {
        this.type = type;
    }
}
