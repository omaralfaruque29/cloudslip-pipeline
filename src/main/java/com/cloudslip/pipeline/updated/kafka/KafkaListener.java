package com.cloudslip.pipeline.updated.kafka;

import com.cloudslip.pipeline.updated.dto.ClusterAgentResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class KafkaListener implements KafkaListenerInterface {

    private final Logger log = LoggerFactory.getLogger(KafkaListener.class);

    @Value("${cloudslip.kafka.producer.bootstrap-servers}")
    private String kafkaBroker;

    private int timeout = 5;


    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void subscribeTopic(String topicName, String consumerGroupId) {

        // Producing A Fake Message
        log.info("Producing A Fake Message for Topic: " + topicName);
        produceFakeMessage(kafkaBroker, topicName);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Consuming the Fake Message to Initialize the Consumer Group
        log.info("Consuming a Fake Message for Consumer Group: " + consumerGroupId);
        consumeFakeMessage(kafkaBroker, topicName, consumerGroupId);

        log.info("Consumer Group: " + consumerGroupId + " is subscribed to Topic: " + topicName);
    }

    @Override
    public void unsubscribeTopic(String topicName) {

    }

    @Override
    public List<ClusterAgentResponseMessage> consumeMessage(String topicName, String consumerGroupId) {
        if(consumerGroupId == null) {
            consumerGroupId = topicName;
        }
        KafkaConsumer kafkaConsumer = null;
        boolean errorOccurred = false;
        List<ClusterAgentResponseMessage> messageList = new ArrayList<>();
        long lastOffset = 1;
        try {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", kafkaBroker);
            properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("enable.auto.commit", "false");
            properties.put("group.id", consumerGroupId );
            kafkaConsumer = new KafkaConsumer(properties);
            List topics = new ArrayList();
            topics.add(topicName);
            kafkaConsumer.subscribe(topics);
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(timeout));

            for (ConsumerRecord record : records) {
                lastOffset = record.offset();
                ClusterAgentResponseMessage message = objectMapper.readValue((String)record.value(), ClusterAgentResponseMessage.class);
                message.setKafkaOffset(record.offset());
                messageList.add(message);
            }
        } catch (Exception ex) {
            errorOccurred = true;
            log.error(ex.getMessage());
        } finally {
            kafkaConsumer.close();
        }
        if(errorOccurred) {
            this.commitOffset(topicName, consumerGroupId, (int)(lastOffset + 1));
            return messageList;
        } else {
            return messageList;
        }
    }

    @Override
    public boolean commitOffset(String topicName, String consumerGroupId, int offset) {
        if(consumerGroupId == null) {
            consumerGroupId = topicName;
        }
        KafkaConsumer kafkaConsumer = null;
        boolean errorOccurred = false;
        try{
            Properties properties = new Properties();
            properties.put("bootstrap.servers", kafkaBroker);
            properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("group.id", consumerGroupId);
            properties.put("enable.auto.commit", "true");
            kafkaConsumer = new KafkaConsumer(properties);

            TopicPartition topicPartition = new TopicPartition(topicName, 0);
            List<TopicPartition> topics = Arrays.asList(topicPartition);
            kafkaConsumer.assign(topics);
            kafkaConsumer.seek(topicPartition, offset);

        } catch (Exception ex) {
            errorOccurred = true;
            log.error(ex.getMessage());
        } finally {
            kafkaConsumer.close();
        }
        return !errorOccurred;
    }


    private void consumeFakeMessage(String kafkaBroker, String topicName, String groupId){
        Consumer<String, String> kafkaConsumer = null;
        try {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", kafkaBroker);
            properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("group.id", groupId );

            kafkaConsumer = new KafkaConsumer(properties);
            List topics = new ArrayList();
            topics.add(topicName);

            kafkaConsumer.subscribe(topics);

            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(timeout + 5));
            for (ConsumerRecord record : records) {
                System.out.println(String.format("Topic - %s, Partition - %d, Offset %s, Key: %s, Value: %s", record.topic(), record.partition(), record.offset(), record.key(), record.value()));
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        } finally {
            kafkaConsumer.close();
        }

    }

    private void produceFakeMessage(String kafkaBroker, String topicName){
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaBroker);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer kafkaProducer = new KafkaProducer(properties);
        try {
            kafkaProducer.send(new ProducerRecord(topicName, "0", "Fake Message - Initialize Consumer Group"));

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            kafkaProducer.close();
        }
    }
}
