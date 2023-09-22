package com.cloudslip.pipeline.updated.service;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.MyStompSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppFacadeWebSocketService {

    private final Logger log = LoggerFactory.getLogger(AppFacadeWebSocketService.class);

    @Autowired
    private MyStompSessionHandler stompSessionHandler;

    @Autowired
    private ApplicationProperties applicationProperties;


    public void connect() {
        log.info("Connecting to Facade Web Socket");
        String facadeServiceUrl = applicationProperties.getFacadeServiceBaseUrl();
        String facadeServiceApiAccessToken = applicationProperties.getFacadeServiceApiAccessToken();
        if(facadeServiceUrl.isEmpty() || facadeServiceUrl == null || facadeServiceUrl == "") {
            log.error("Facade Service URL is not defined");
            return;
        } else if(facadeServiceApiAccessToken.isEmpty() || facadeServiceApiAccessToken == null || facadeServiceApiAccessToken == "") {
            log.error("No API Access Token available to connect to Facade Web Socket");
            return;
        }
        try {
            List<Transport> transports = new ArrayList<>(1);
            transports.add(new WebSocketTransport( new StandardWebSocketClient()) );
            WebSocketClient transport = new SockJsClient(transports);
            WebSocketStompClient stompClient = new WebSocketStompClient(transport);

            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            //stompClient.setMessageConverter(new StringMessageConverter());
            facadeServiceUrl = facadeServiceUrl.replaceFirst("https://", "ws://");
            facadeServiceUrl = facadeServiceUrl.replaceFirst("http://", "ws://");
            String url = facadeServiceUrl + "web-socket?accessToken=" + facadeServiceApiAccessToken;
            stompClient.connect(url, stompSessionHandler);
        } catch (ResourceAccessException ex) {
            log.error("Failed to connect to Facade Web Socket");
        } catch (HttpClientErrorException ex) {
            log.error("Failed to connect to Facade Web Socket");
        } catch (Exception ex) {
            log.error("Failed to connect to Facade Web Socket");
        }
    }

    public void sendMessage(Object message) {
        stompSessionHandler.sendMessage(message);
    }

}
