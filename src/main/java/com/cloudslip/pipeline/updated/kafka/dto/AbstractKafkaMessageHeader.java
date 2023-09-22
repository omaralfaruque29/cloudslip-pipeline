package com.cloudslip.pipeline.updated.kafka.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public abstract class AbstractKafkaMessageHeader implements Serializable {

    private Long offset;
    private String companyId;
    private String createDate;

    public AbstractKafkaMessageHeader() {
        this.createDate = String.valueOf(ZonedDateTime.now());
    }

    public AbstractKafkaMessageHeader(String companyId) {
        this.companyId = companyId;
        this.createDate = String.valueOf(ZonedDateTime.now());
    }

    public AbstractKafkaMessageHeader(Long offset, String companyId, String createDate) {
        this.offset = offset;
        this.companyId = companyId;
        this.createDate = createDate;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
