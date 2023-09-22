package com.cloudslip.pipeline.processor;

import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.cloudslip.pipeline.service.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeploymentProcessor {

    @Autowired
    private GithubService githubService;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    public void publishDeploymentConfig(String repoName, String path, String commitId) {
        String config = githubService.getDeploymentDescriptor(repoName, path, commitId);
        //kafkaPublisher.sendMessage(config);
    }
}
