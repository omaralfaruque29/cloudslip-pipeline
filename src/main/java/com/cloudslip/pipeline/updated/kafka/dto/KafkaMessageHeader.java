package com.cloudslip.pipeline.updated.kafka.dto;

import java.util.HashMap;

public class KafkaMessageHeader extends AbstractKafkaMessageHeader {

    private String namespace;
    private String command;
    private HashMap<String, String> extras;

    public KafkaMessageHeader() {
    }

    public KafkaMessageHeader(String command) {
        super();
        this.command = command;
    }
    public KafkaMessageHeader(String namespace, String command) {
        super();
        this.namespace = namespace;
        this.command = command;
    }

    public KafkaMessageHeader(String namespace, String command, String companyId) {
        super(companyId);
        this.namespace = namespace;
        this.command = command;
    }

    public KafkaMessageHeader(String namespace, String command, HashMap<String, String> extras) {
        super();
        this.namespace = namespace;
        this.command = command;
        this.extras = extras;
    }

    public KafkaMessageHeader(Long offset, String companyId, String createDate, String namespace, String command, HashMap<String, String> extras) {
        super(offset, companyId, createDate);
        this.namespace = namespace;
        this.command = command;
        this.extras = extras;
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

    public HashMap<String, String> getExtras() {
        return extras;
    }

    public void setExtras(HashMap<String, String> extras) {
        this.extras = extras;
    }

    public void addToExtra(String key, String value) {
        if(this.extras == null) {
            this.extras = new HashMap<>();
        }
        this.extras.put(key, value);
    }
}
