package com.cloudslip.pipeline.updated.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConstant {
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_DEV = "ROLE_ADMIN";
    public static final String ROLE_OPS = "ROLE_OPS";

    @Value("${developer.alias}")
    private String DEVELOPER_ALIAS;

    public String getClusterAgentResponseKafkaTopic() {
        return "kt-" + DEVELOPER_ALIAS + "-cluster-agent-response";
    }
    public String getClusterAgentResponseConsumerGroup() {
        return "cg-" + DEVELOPER_ALIAS + "-cluster-agent-response";
    }
}
