package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class AddCustomIngressDTO extends BaseInputDTO{

    private ObjectId appVpcId;
    private String customIngress;

    public AddCustomIngressDTO() {
    }

    public String getCustomIngress() {
        return customIngress;
    }

    public void setCustomIngress(String customIngress) {
        this.customIngress = customIngress;
    }

    public ObjectId getAppVpcId() {
        return appVpcId;
    }

    public void setAppVpcId(ObjectId appVpcId) {
        this.appVpcId = appVpcId;
    }
}
