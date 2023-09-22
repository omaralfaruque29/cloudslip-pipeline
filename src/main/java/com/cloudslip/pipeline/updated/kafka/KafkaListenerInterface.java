package com.cloudslip.pipeline.updated.kafka;

import com.cloudslip.pipeline.updated.dto.ClusterAgentResponseMessage;

import java.util.List;

public interface KafkaListenerInterface {

    public void subscribeTopic(String topicName, String consumerGroupId);

    public void unsubscribeTopic(String topicName);

    public List<ClusterAgentResponseMessage> consumeMessage(String topicName, String consumerGroupId);

    public boolean commitOffset(String topic, String consumerGroupId, int offset);

}
