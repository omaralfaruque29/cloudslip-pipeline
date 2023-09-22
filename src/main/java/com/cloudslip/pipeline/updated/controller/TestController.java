package com.cloudslip.pipeline.updated.controller;

import com.cloudslip.pipeline.updated.dto.DeploymentDescriptor;
import com.cloudslip.pipeline.updated.enums.AgentCommand;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessageHeader;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private ObjectMapper objectMapper;


    @RequestMapping(value = "/kafka-publish/{topicName}", method = RequestMethod.GET)
    public ResponseEntity<?> publishToKafka(@PathVariable("topicName") String topicName) throws URISyntaxException {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        DeploymentDescriptor deploymentDescriptor = new DeploymentDescriptor();
        deploymentDescriptor.setImageName("whyxn");
        deploymentDescriptor.setDockerRegistryUrl("dockerhub/whyxn");
        deploymentDescriptor.setMaxMemory(2048);
        deploymentDescriptor.setReplica(2);

        String jsonString = "";
        KafkaMessage message = null;

        try {
            String bodyString = objectMapper.writeValueAsString(deploymentDescriptor);
            message = new KafkaMessage(new KafkaMessageHeader("cloudslip", AgentCommand.ADD_DEPLOYMENT.toString()), bodyString);
            jsonString = objectMapper.writeValueAsString(message);
            System.out.println(jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaPublisher.publishMessage(topicName, jsonString);
        ResponseDTO response = new ResponseDTO().generateSuccessResponse(message);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = "/kafka/create-topic/{topicName}", method = RequestMethod.GET)
    public ResponseEntity<?> createKafkaTopic(@PathVariable("topicName") String topicName) throws URISyntaxException {
        kafkaPublisher.createTopic(topicName);
        ResponseDTO response = new ResponseDTO().generateSuccessResponse(topicName, "Topic Created");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = "/kafka/delete-topic/{topicName}", method = RequestMethod.GET)
    public ResponseEntity<?> deleteKafkaTopic(@PathVariable("topicName") String topicName) throws URISyntaxException {
        kafkaPublisher.deleteTopic(topicName);
        ResponseDTO response = new ResponseDTO().generateSuccessResponse(topicName, "Topic Deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
