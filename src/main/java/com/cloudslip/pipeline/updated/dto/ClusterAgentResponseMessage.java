package com.cloudslip.pipeline.updated.dto;


import java.util.HashMap;
import java.util.List;

public class ClusterAgentResponseMessage extends BaseInputDTO {

    private String agentKafkaTopic;
    private String namespace;
    private String command;
    private String companyId;
    private HashMap<String, String> extras;
    private List<ClusterAgentTaskStatus> taskStatusList;
    private long kafkaOffset;

    public ClusterAgentResponseMessage() {
    }

    public ClusterAgentResponseMessage(String agentKafkaTopic, String namespace, String command, String companyId, HashMap<String, String> extras, List<ClusterAgentTaskStatus> taskStatusList, long kafkaOffset) {
        this.agentKafkaTopic = agentKafkaTopic;
        this.namespace = namespace;
        this.command = command;
        this.companyId = companyId;
        this.extras = extras;
        this.taskStatusList = taskStatusList;
        this.kafkaOffset = kafkaOffset;
    }

    public String getAgentKafkaTopic() {
        return agentKafkaTopic;
    }

    public void setAgentKafkaTopic(String agentKafkaTopic) {
        this.agentKafkaTopic = agentKafkaTopic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public HashMap<String, String> getExtras() {
        return extras;
    }

    public void setExtras(HashMap<String, String> extras) {
        this.extras = extras;
    }

    public List<ClusterAgentTaskStatus> getTaskStatusList() {
        return taskStatusList;
    }

    public void setTaskStatusList(List<ClusterAgentTaskStatus> taskStatusList) {
        this.taskStatusList = taskStatusList;
    }

    public long getKafkaOffset() {
        return kafkaOffset;
    }

    public void setKafkaOffset(long kafkaOffset) {
        this.kafkaOffset = kafkaOffset;
    }
}
