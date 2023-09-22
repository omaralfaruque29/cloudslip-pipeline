package com.cloudslip.pipeline.updated.scheduler;


import com.cloudslip.pipeline.updated.constant.ApplicationConstant;
import com.cloudslip.pipeline.updated.dto.ClusterAgentResponseMessage;
import com.cloudslip.pipeline.updated.helper.pipeline.ProcessClusterAgentResponseMessageHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaListener;
import com.cloudslip.pipeline.updated.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClusterAgentResponseMessageFetchScheduler {

    private final Logger log = LoggerFactory.getLogger(ClusterAgentResponseMessageFetchScheduler.class);


    @Autowired
    private KafkaListener kafkaListener;

    @Autowired
    private ProcessClusterAgentResponseMessageHelper processClusterAgentResponseMessageHelper;

    @Autowired
    private ApplicationConstant applicationConstant;

    @Value("${cloudslip.kafka.run-cluster-agent-response-fetch-scheduler}")
    private String runClusterAgentResponseFetchScheduler;


    @Scheduled(fixedDelay = 5000)
    public void execute() {
        if(Utils.isStringEquals(runClusterAgentResponseFetchScheduler, "true")) {
            log.info("Fetching Vpc Agent Response Messages - " + System.currentTimeMillis() / 1000);

            List<ClusterAgentResponseMessage> messageList = kafkaListener.consumeMessage(applicationConstant.getClusterAgentResponseKafkaTopic(), applicationConstant.getClusterAgentResponseConsumerGroup());

            log.info("New Vpc Agent Response Message Count - " + Integer.toString(messageList.size()));

            for(ClusterAgentResponseMessage message : messageList) {
                processClusterAgentResponseMessageHelper.execute(message, null);
            }
        }
    }

}
