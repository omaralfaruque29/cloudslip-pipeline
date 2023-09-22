package com.cloudslip.pipeline.updated.manager;

import com.cloudslip.pipeline.updated.dto.WebSocketMessagePayload;
import com.cloudslip.pipeline.updated.enums.WebSocketMessageType;

public interface WebSocketMessageManagerInterface {

    public void broadcastApplicationInitializationLog(String log, String target);

    public void broadcastApplicationInitializationStatus(String log, String target, Long estimatedTime, WebSocketMessageType webSocketMessageType);

    public void broadcastPipelineStepLog(String appCommitPipelineStepId, String gitCommitId, String log, String target);

    public void broadcastPipelineStepStatus(String appCommitPipelineStepId, String gitCommitId, String log, String target, WebSocketMessageType webSocketMessageType);

    public void broadcastPipelineStepStatus(String appCommitPipelineStepId, String gitCommitId, String log, String target, Long estimatedTime, WebSocketMessageType webSocketMessageType);

    public void broadcastMessage(WebSocketMessagePayload payload, String target);

}
