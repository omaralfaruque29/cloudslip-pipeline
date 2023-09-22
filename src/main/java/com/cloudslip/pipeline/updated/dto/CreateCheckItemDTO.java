package com.cloudslip.pipeline.updated.dto;


import com.cloudslip.pipeline.updated.enums.Authority;

import java.util.List;

public class CreateCheckItemDTO {

    private String message;
    private List<Authority> authority;

    public CreateCheckItemDTO() {
    }

    public CreateCheckItemDTO(String message, List<Authority> authority) {
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
