package com.cloudslip.pipeline.updated.model.universal;

import com.cloudslip.pipeline.updated.model.BaseEntity;

import javax.validation.constraints.NotNull;

public class Region extends BaseEntity {

    @NotNull
    private String name;

    @NotNull
    private String code;

    private String description;


    public Region() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
