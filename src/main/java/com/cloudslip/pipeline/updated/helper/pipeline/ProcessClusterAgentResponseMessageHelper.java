package com.cloudslip.pipeline.updated.helper.pipeline;

import com.cloudslip.pipeline.updated.constant.ApplicationConstant;
import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaListener;
import com.cloudslip.pipeline.updated.manager.WebSocketMessageManager;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.repository.*;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProcessClusterAgentResponseMessageHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(ProcessClusterAgentResponseMessageHelper.class);

    private ClusterAgentResponseMessage responseMessage;
    private ResponseDTO output = new ResponseDTO();
    private Optional<AppVpc> appVpc;
    private Optional<AppCommit> appCommit;
    private Optional<AppEnvironment> appEnvironment;
    private Optional<AppCommitState> appCommitState;
    private Optional<AppCommitPipelineStep> appCommitPipelineStep;
    private Optional<Application> application;

    @Autowired
    private AppVpcRepository appVpcRepository;

    @Autowired
    private AppPipelineStepRepository appPipelineStepRepository;

    @Autowired
    private AppCommitRepository appCommitRepository;

    @Autowired
    private AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaListener kafkaListener;

    @Value("${cloudslip.kafka.producer.bootstrap-servers}")
    private String kafkaServer;

    @Autowired
    private WebSocketMessageManager webSocketMessageManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ApplicationConstant applicationConstant;

    @Autowired
    private CustomRestTemplate restTemplate;


    public void init(BaseInput input, Object... extraParams) {
        this.responseMessage = (ClusterAgentResponseMessage)input;
        this.setOutput(output);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    protected void checkPermission() {

    }


    protected void checkValidity() {

    }

    protected void checkValidityForAppDeploymentResponse() {
        try {
            if (!responseMessage.getExtras().keySet().contains("applicationId") || responseMessage.getExtras().get("applicationId").isEmpty()
                    || !responseMessage.getExtras().keySet().contains("gitCommitId") || responseMessage.getExtras().get("gitCommitId").isEmpty() ||
                    responseMessage.getAgentKafkaTopic() == null) {
                output.generateErrorResponse("Missing required values!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
            ObjectId applicationId = null;
            try {
                applicationId = new ObjectId(responseMessage.getExtras().get("applicationId"));
            } catch (Exception ex) {
                output.generateErrorResponse("Invalid application id to convert!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }

            if(responseMessage.getExtras().getOrDefault("appVpcId", null) == null) {
                output.generateErrorResponse("App VPC Id not found for deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
            appVpc = appVpcRepository.findByIdAndStatus(new ObjectId(responseMessage.getExtras().get("appVpcId")), Status.V);
            if (!appVpc.isPresent()) {
                output.generateErrorResponse("App VPC not found for deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }

            Optional<AppPipelineStep> appPipelineStep = appPipelineStepRepository.findByAppVpcIdAndStepTypeAndStatus(appVpc.get().getObjectId(), PipelineStepType.DEPLOY, Status.V);
            if (!appVpc.isPresent()) {
                output.generateErrorResponse("App Pipeline Step not found for deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }

            appCommit = appCommitRepository.findByApplicationIdAndGitCommitIdAndStatus(applicationId, responseMessage.getExtras().get("gitCommitId"), Status.V);
            if (!appCommit.isPresent()) {
                output.generateErrorResponse("App Commit not found for deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
            appCommitPipelineStep = appCommitPipelineStepRepository.findByAppCommitIdAndAppPipelineStep_IdAndStatus(appCommit.get().getObjectId(), appPipelineStep.get().getObjectId(), Status.V);
            if (!appCommitPipelineStep.isPresent()) {
                output.generateErrorResponse("App Commit Pipeline Step not found for deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }

            appCommitState = appCommitStateRepository.findByAppCommit_IdAndStatus(appCommit.get().getObjectId(), Status.V);
            if (!appCommitState.isPresent()) {
                output.generateErrorResponse("App Commit State not found to deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }

            appEnvironment = appEnvironmentRepository.findByIdAndStatus(appVpc.get().getAppEnvironmentObjectId(), Status.V);
            if (!appEnvironment.isPresent()) {
                output.generateErrorResponse("App Environment not found to deploy!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }

            application = applicationRepository.findByIdAndStatus(applicationId, Status.V);
            if (!application.isPresent()) {
                output.generateErrorResponse("Application not found to update status from agent!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
        } catch (Exception ex) {
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        }
    }


    protected void doPerform() {
        if(responseMessage.getCommand().equals("ADD_DEPLOYMENT")) {
            processMessageForDeploymentResponse();
        } else if(responseMessage.getCommand().equals("CREATE_NAMESPACE")) {
            processMessageForNamespaceCreation();
        } else if (responseMessage.getCommand().equals("REMOVE_NAMESPACE")){
            processMessageForNamespaceDeletion();
        }
    }

    private void processMessageForDeploymentResponse() {

        checkValidityForAppDeploymentResponse();

        WebSocketMessageType webSocketMessageType = null;

        if(isAllTaskStatusSuccess()) {
            appCommitPipelineStep.get().setType(PipelineStepStatusType.SUCCESS);
            webSocketMessageType = WebSocketMessageType.PIPELINE_STEP_SUCCESS;
        } else {
            appCommitPipelineStep.get().setType(PipelineStepStatusType.FAILED);
            webSocketMessageType = WebSocketMessageType.PIPELINE_STEP_FAILED;
        }

        appCommitPipelineStep.get().setLog(getAllTaskLog());
        appCommitPipelineStepRepository.save(appCommitPipelineStep.get());

        updateStatusInAppCommitState(appCommitPipelineStep.get().getType());
        updateOverallDeploymentStatus(appCommitPipelineStep.get().getType());

        if(webSocketMessageType != null) {
            if(application.get().getWebSocketSubscriberCount() > 0) {
                log.info("Broadcasting Pipeline Step Status: {}", webSocketMessageType);
                webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStep.get().getId(), responseMessage.getExtras().get("gitCommitId"), "", application.get().getWebSocketTopic(), webSocketMessageType);
            }
        }

    }

    private void processMessageForNamespaceCreation() {
        try {
            boolean allSuccess = true;
            if(responseMessage.getTaskStatusList() != null) {
                for(ClusterAgentTaskStatus taskStatus : responseMessage.getTaskStatusList()) {
                    if(!Utils.isStringEquals(taskStatus.getStatus(), "success")) {
                        allSuccess = false;
                    }
                }
            }

            String vpcId = responseMessage.getExtras().get("vpcId");
            VpcStatus targetVpcStatus = VpcStatus.INITIALIZATION_FAILED;
            if(allSuccess) {
                targetVpcStatus = VpcStatus.INITIALIZED;
            }
            HttpHeaders headers = Utils.generateHttpHeaders();
            HttpEntity<UpdateVpcStatusInputDTO> request = new HttpEntity<>(new UpdateVpcStatusInputDTO(new ObjectId(vpcId), targetVpcStatus), headers);
            ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/vpc/update-status", request, ResponseDTO.class);
            if(response.getStatus() == ResponseStatus.error){
                output.generateErrorResponse(response.getMessage());
                throw new ApiErrorException(this.getClass().getName());
            }
        } catch (Exception ex) {
            log.error("Error in Processing Response Message For Namespace Creation: " + ex.getMessage());
        }
    }

    private void processMessageForNamespaceDeletion() {
        try {
            boolean allSuccess = true;
            if(responseMessage.getTaskStatusList() != null) {
                for(ClusterAgentTaskStatus taskStatus : responseMessage.getTaskStatusList()) {
                    if(!Utils.isStringEquals(taskStatus.getStatus(), "success")) {
                        allSuccess = false;
                    }
                }
            }

            String vpcId = responseMessage.getExtras().get("vpcId");
            VpcStatus targetVpcStatus = VpcStatus.TERMINATION_FAILED;
            if(allSuccess) {
                targetVpcStatus = VpcStatus.TERMINATED;
            }
            HttpHeaders headers = Utils.generateHttpHeaders();
            HttpEntity<UpdateVpcStatusInputDTO> request = new HttpEntity<>(new UpdateVpcStatusInputDTO(new ObjectId(vpcId), targetVpcStatus), headers);
            ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/vpc/update-status", request, ResponseDTO.class);
            if(response.getStatus() == ResponseStatus.error){
                output.generateErrorResponse(response.getMessage());
                throw new ApiErrorException(this.getClass().getName());
            }
        } catch (Exception ex) {
            log.error("Error in Processing Response Message For Namespace deletion: " + ex.getMessage());
        }
    }



    private boolean isAllTaskStatusSuccess() {
        for(ClusterAgentTaskStatus taskStatus : responseMessage.getTaskStatusList()) {
            if(!Utils.isStringEquals(taskStatus.getStatus(), "success")) {
                return false;
            }
        }
        return true;
    }

    private String getAllTaskLog() {
        String log = "";
        for(ClusterAgentTaskStatus taskStatus : responseMessage.getTaskStatusList()) {
            if(taskStatus.getLog() != null) {
                log += taskStatus.getLog() != null ? (taskStatus.getLog() + "\n\n") : "";
            }
        }
        return log;
    }

    private void updateStatusInAppCommitState(PipelineStepStatusType statusType) {
        for (AppEnvironmentStateForAppCommit environmentState : appCommitState.get().getEnvironmentStateList()) {
            if(environmentState.getAppEnvironment().getObjectId().equals(appEnvironment.get().getObjectId())) {
                for(AppCommitPipelineStep appCommitPipelineStep : environmentState.getSteps()) {
                    if(appCommitPipelineStep.getObjectId().equals(this.appCommitPipelineStep.get().getObjectId())) {
                        appCommitPipelineStep.setType(statusType);
                        appCommitStateRepository.save(appCommitState.get());
                        break;
                    }
                }
            }
        }
    }

    private void commitKafkaOffset() {
        kafkaListener.commitOffset(applicationConstant.getClusterAgentResponseKafkaTopic(), applicationConstant.getClusterAgentResponseConsumerGroup(), (int)responseMessage.getKafkaOffset() + 1);
    }



    private void updateOverallDeploymentStatus(PipelineStepStatusType statusType) {
        try {
            AppVpc appVpc = this.appVpc.get();

            if (application.get().isBlueGreenDeploymentEnabled()) {
                //TODO: Need to implement for Blue Green
            } else {
                appVpc.getMainCommit().setStatusType(statusType);
            }
            appVpc = appVpcRepository.save(appVpc);

            List<AppVpc> appEnvAppVpcList = appEnvironment.get().getAppVpcList();
            for (int i = 0; i < appEnvAppVpcList.size(); i++) {
                if (Utils.isStringEquals(appEnvAppVpcList.get(i).getId(), appVpc.getId())) {
                    appEnvAppVpcList.set(i, appVpc);
                    appEnvironment.get().setAppVpcList(appEnvAppVpcList);
                    break;
                }
            }
            appEnvironmentRepository.save(appEnvironment.get());
        } catch (Exception ex) {
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void postPerformCheck() {
        this.commitKafkaOffset();
    }

    protected void doRollback() {
        this.commitKafkaOffset();
    }
}
