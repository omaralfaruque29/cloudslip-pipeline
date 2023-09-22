package com.cloudslip.pipeline.updated.model;

import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "app_environment_check_items")
public class CheckItem extends BaseEntity {

    private String message;
    private List<Authority> authority;

    public CheckItem() {
    }

    public CheckItem(String message, List<Authority> authority) {
        this.message = message;
        this.authority = authority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Authority> getAuthority() {
        return authority;
    }

    public void setAuthority(List<Authority> authority) {
        this.authority = authority;
    }
}
