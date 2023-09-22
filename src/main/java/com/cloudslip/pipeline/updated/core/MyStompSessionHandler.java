package com.cloudslip.pipeline.updated.core;

import com.cloudslip.pipeline.updated.dto.WebSocketMessageDTO;
import com.cloudslip.pipeline.updated.dto.WebSocketMessagePayload;
import com.cloudslip.pipeline.updated.enums.WebSocketMessageType;
import com.cloudslip.pipeline.updated.service.AppFacadeWebSocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    private Logger logger = LogManager.getLogger(MyStompSessionHandler.class);

    private StompSession session;
    private StompHeaders connectedHeaders;

    @Autowired
    private AppFacadeWebSocketService appFacadeWebSocketService;


    public MyStompSessionHandler() {

    }

    private void init(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;
        this.connectedHeaders = connectedHeaders;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.init(session, connectedHeaders);
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/user/queue/pipeline-service", this);
        logger.info("Subscribed to /user/queue/user-message");
        //this.broadcastMessage(getSampleSocketMessage());
    }

    public void sendMessage(Object message) {
        session.send("/app/cloudslip-webservice/send/message", message);
    }

    public void broadcastMessage(Object message) {
        try {
            session.send("/app/cloudslip-webservice/broadcast/message", message);
        } catch (IllegalStateException ex) {
            logger.error("Illegal State Exception in Broadcast Message: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception in Broadcast Message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return WebSocketMessageDTO.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        WebSocketMessagePayload msg = (WebSocketMessagePayload) payload;
        logger.info("Received : " + msg.toString());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.info("Got an exception : " + exception);
        this.attemptToReconnect();
    }

    private String getSampleMessage() {
        return "This is a sample message";
    }

    private WebSocketMessageDTO getSampleSocketMessage() {
        return new WebSocketMessageDTO(new WebSocketMessagePayload("This is a sample socket message", WebSocketMessageType.GENERAL), "admin@cloudslip.com");
    }

    private void attemptToReconnect() {
        logger.info("Attempting to reconnect in 5 seconds...");
        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                appFacadeWebSocketService.connect();
            }
        }, 5, TimeUnit.SECONDS);
    }
}