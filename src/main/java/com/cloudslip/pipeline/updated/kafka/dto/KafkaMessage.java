package com.cloudslip.pipeline.updated.kafka.dto;

import java.io.Serializable;

public class KafkaMessage<T> implements Serializable {

    private KafkaMessageHeader header;
    private T body;

    public KafkaMessage() {
    }

    public KafkaMessage(KafkaMessageHeader header, T body) {
        this.header = header;
        this.body = body;
    }

    public KafkaMessageHeader getHeader() {
        return header;
    }

    public void setHeader(KafkaMessageHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
