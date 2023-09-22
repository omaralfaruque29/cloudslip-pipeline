package com.cloudslip.pipeline.updated.kafka;

import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class KafkaPublisher implements KafkaPublisherInterface {

    private final Logger log = LoggerFactory.getLogger(KafkaPublisher.class);


    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${cloudslip.kafka.producer.bootstrap-servers}")
    private String kafkaBroker;

    @Autowired
    private Environment env;


    @Override
    public boolean createTopic(String topicName) {
        if(!isKafkaActive()) {
            return false;
        }

        try {
            AdminClient client = AdminClient.create(kafkaAdmin.getConfig());
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            List<NewTopic> newTopicList = new ArrayList<>();
            newTopicList.add(newTopic);
            client.createTopics(newTopicList);
            client.close();
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean publishMessage(String topicName, KafkaMessage message) {
        try {
            String messageAsJson = objectMapper.writeValueAsString(message);
            this.publishMessage(topicName, messageAsJson);
            return true;
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean publishMessage(String topicName, String message) {
        if(!isKafkaActive()) {
            return false;
        }

        boolean response = true;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaBroker);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer kafkaProducer = new KafkaProducer(properties);

        try {
            kafkaProducer.send(new ProducerRecord(topicName, "", message));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            response = false;
        } finally {
            kafkaProducer.close();
        }
        return response;

    }



    @Override
    public boolean deleteTopic(String topicName) {
        if(!isKafkaActive()) {
            return false;
        }
        try {
            AdminClient client = AdminClient.create(kafkaAdmin.getConfig());
            List<String> topicList = new ArrayList<>();
            topicList.add(topicName);
            client.deleteTopics(topicList);
            client.close();
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    private boolean isKafkaActive() {
        if(!isKafkaForceUse() && (env.getActiveProfiles() == null || env.getActiveProfiles().length == 0 || Utils.isStringEquals(env.getActiveProfiles()[0], "dev"))) {
            return false;
        }
        return true;
    }

    public boolean isKafkaForceUse() {
        try {
            return Boolean.valueOf(env.getProperty("cloudslip.kafka.force-use"));
        } catch (Exception ex) {
            return false;
        }
    }
}
