package com.cloudslip.pipeline.updated.exception.model;


public class ApiErrorException extends RuntimeException {

    private String service;

    public ApiErrorException(final String service) {
        super();
        this.service = service;
    }

    public ApiErrorException(final String message, final String service) {
        super(message);
        this.service = service;
    }

    public ApiErrorException(final String message, final Throwable cause,
                             final String service) {
        super(message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public void setService(final String service) {
        this.service = service;
    }
}
