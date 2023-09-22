package com.cloudslip.pipeline.updated.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

    @Value("${env.facade-service.base-url}")
    private String FACADE_SERVICE_BASE_URL;

    @Value("${env.facade-service.api-access-token}")
    private String FACADE_SERVICE_API_ACCESS_TOKEN;

    @Value("${env.facade-service.connect-to-web-socket}")
    private String CONNECT_TO_FACADE_SERVICE_WEB_SOCKET;

    @Value("${env.usermanagement-service.base-url}")
    private String USER_MANAGEMENT_SERVICE_BASE_URL;


    public ApplicationProperties() {
    }

    public String getUserManagementServiceBaseUrl() {
        return USER_MANAGEMENT_SERVICE_BASE_URL;
    }

    public String getFacadeServiceBaseUrl() {
        return FACADE_SERVICE_BASE_URL;
    }

    public String connectToFacadeServiceWebSocket() {
        return CONNECT_TO_FACADE_SERVICE_WEB_SOCKET;
    }

    public String getFacadeServiceApiAccessToken() {
        return FACADE_SERVICE_API_ACCESS_TOKEN;
    }

}
