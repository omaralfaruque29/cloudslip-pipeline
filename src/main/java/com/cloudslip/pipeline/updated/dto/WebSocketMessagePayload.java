package com.cloudslip.pipeline.updated.dto;

import com.cloudslip.pipeline.updated.enums.WebSocketMessageType;

import java.io.Serializable;

public class WebSocketMessagePayload<T> implements Serializable {

    private T data;
    private WebSocketMessageType type;

    public WebSocketMessagePayload() {
    }

    public WebSocketMessagePayload(T data, WebSocketMessageType type) {
        this.data = data;
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public WebSocketMessageType getType() {
        return type;
    }

    public void setType(WebSocketMessageType type) {
        this.type = type;
    }

    public void setValues(T data, WebSocketMessageType type) {
        this.data = data;
        this.type = type;
    }

    @Override
    public String toString() {
        return "WebSocketMessagePayload{" +
                "data=" + data +
                ", type=" + type +
                '}';
    }
}
