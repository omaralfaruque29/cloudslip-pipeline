package com.cloudslip.pipeline.updated.core;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.service.AppFacadeWebSocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class OnStartUpSocketConnectionInitializer {

    private Logger log = LogManager.getLogger(OnStartUpSocketConnectionInitializer.class);

    @Autowired
    private AppFacadeWebSocketService appFacadeWebSocketService;

    @Autowired
    private ApplicationProperties applicationProperties;


    @PostConstruct
    public void init(){
        // init code goes here
        if(applicationProperties.connectToFacadeServiceWebSocket() == "true" || applicationProperties.connectToFacadeServiceWebSocket().equals("true")) {
            appFacadeWebSocketService.connect();
        }
    }
}