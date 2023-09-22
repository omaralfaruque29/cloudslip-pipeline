package com.cloudslip.pipeline.updated.dto;

import org.bson.types.ObjectId;

public class DeleteObjectInputDTO extends BaseInputDTO {

    private ObjectId id;

    public DeleteObjectInputDTO() {
    }

    public DeleteObjectInputDTO(ObjectId id) {
        this.id = id;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
