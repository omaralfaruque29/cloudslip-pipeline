package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class GetObjectInputDTO extends BaseInputDTO {

    private ObjectId id;

    public GetObjectInputDTO() {
    }

    public GetObjectInputDTO(ObjectId id) {
        this.id = id;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
