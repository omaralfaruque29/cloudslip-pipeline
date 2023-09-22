package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;


@Document
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 8571261118900116242L;

    @Id
    private ObjectId id;
    private Status status = Status.V;
    private String createDate;
    private String updateDate;
    private String createdBy = "system";
    private String updatedBy;
    private ObjectId createActionId;
    private ObjectId lastUpdateActionId;

    public BaseEntity() {
    }

    public String getId() {
        if(id != null) {
            return id.toHexString();
        }
        return null;
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return id;
    }


    public void setId(final ObjectId id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean isValid() {
        return status == Status.V ? true : false;
    }

    @JsonIgnore
    public  boolean isDeleted() {
        return status == Status.D ? true : false;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @JsonIgnore
    public ObjectId getCreateActionId() {
        return createActionId;
    }

    public void setCreateActionId(ObjectId createActionId) {
        this.createActionId = createActionId;
    }

    @JsonIgnore
    public ObjectId getLastUpdateActionId() {
        return lastUpdateActionId;
    }

    public void setLastUpdateActionId(ObjectId lastUpdateActionId) {
        this.lastUpdateActionId = lastUpdateActionId;
    }
}
