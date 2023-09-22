package com.cloudslip.pipeline.updated.dto;


import com.cloudslip.pipeline.updated.enums.ResponseStatus;

public class BaseResponseDTO implements BaseOutput {

    private ResponseStatus status;
    private String message;

    public BaseResponseDTO() {
    }

    public BaseResponseDTO(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
