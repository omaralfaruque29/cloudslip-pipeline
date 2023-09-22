package com.cloudslip.pipeline.updated.kafka;


import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;

public interface KafkaPublisherInterface {

    public boolean createTopic(String topicName);

    public boolean publishMessage(String topicName, KafkaMessage message);

    public boolean publishMessage(String topicName, String message);

    public boolean deleteTopic(String topicName);
}
