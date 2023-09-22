package com.cloudslip.pipeline.updated.dto.vpcresourceupdate;

import org.bson.types.ObjectId;

import java.util.List;

public class EnvironmentInfoUpdateDTO {
    private ObjectId environmentId;
    private List<VpcResourceDTO> vpcList;

    public ObjectId getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(ObjectId environmentId) {
        this.environmentId = environmentId;
    }

    public List<VpcResourceDTO> getVpcList() {
        return vpcList;
    }

    public void setVpcList(List<VpcResourceDTO> vpcList) {
        this.vpcList = vpcList;
    }
}
