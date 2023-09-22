package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.core.MyStompSessionHandler;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.WebSocketMessageType;
import com.cloudslip.pipeline.updated.helper.pipeline.TriggerPipelineBuildStepHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageManager implements WebSocketMessageManagerInterface {

    private final Logger log = LoggerFactory.getLogger(TriggerPipelineBuildStepHelper.class);

    @Autowired
    private MyStompSessionHandler stompSessionHandler;


    @Override
    public void broadcastApplicationInitializationLog(String log, String target) {
        WebSocketMessagePayload payload = new WebSocketMessagePayload(log, WebSocketMessageType.APP_INITIALIZATION_LOG);
        this.broadcastMessage(payload, target);
    }

    @Override
    public void broadcastApplicationInitializationStatus(String log, String target, Long estimatedTime, WebSocketMessageType webSocketMessageType) {
        AppInitializationStatusWebSocketData data = new AppInitializationStatusWebSocketData(log, estimatedTime);
        WebSocketMessagePayload payload = new WebSocketMessagePayload(data, webSocketMessageType);
        this.broadcastMessage(payload, target);
    }

    @Override
    public void broadcastPipelineStepLog(String appCommitPipelineStepId, String gitCommitId, String log, String target) {
        PipelineRunningStepLogWebSocketData data = new PipelineRunningStepLogWebSocketData(gitCommitId, appCommitPipelineStepId, log);
        WebSocketMessagePayload payload = new WebSocketMessagePayload(data, WebSocketMessageType.PIPELINE_STEP_LOG);
        this.broadcastMessage(payload, target);
    }

    @Override
    public void broadcastPipelineStepStatus(String appCommitPipelineStepId, String gitCommitId, String log, String target, WebSocketMessageType webSocketMessageType) {
        this.broadcastPipelineStepStatus(appCommitPipelineStepId, gitCommitId, log, target, null, webSocketMessageType);
    }

    @Override
    public void broadcastPipelineStepStatus(String appCommitPipelineStepId, String gitCommitId, String log, String target, Long estimatedTime, WebSocketMessageType webSocketMessageType) {
        PipelineStepStatusWebSocketData data = new PipelineStepStatusWebSocketData(gitCommitId, appCommitPipelineStepId, log, estimatedTime);
        WebSocketMessagePayload payload = new WebSocketMessagePayload(data, webSocketMessageType);
        this.broadcastMessage(payload, target);
    }

    @Override
    public void broadcastMessage(WebSocketMessagePayload payload, String target) {
        WebSocketMessageDTO message = new WebSocketMessageDTO(payload, target);
        stompSessionHandler.broadcastMessage(message);
    }
}
