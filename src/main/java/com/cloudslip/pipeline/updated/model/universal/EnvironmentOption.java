package com.cloudslip.pipeline.updated.model.universal;

import com.cloudslip.pipeline.updated.model.BaseEntity;

import javax.validation.constraints.NotNull;

public class EnvironmentOption extends BaseEntity {

    @NotNull
    private String name;

    @NotNull
    private String shortName;

    private String description;

    private int orderNo;

    private boolean isEnabled = true;

    public EnvironmentOption() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
