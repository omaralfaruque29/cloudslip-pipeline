package com.cloudslip.pipeline.updated.helper.pipeline;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.kubeconfig.AppKubeConfig;
import com.cloudslip.pipeline.updated.dto.kubeconfig.AutoScalingConfig;
import com.cloudslip.pipeline.updated.dto.kubeconfig.DeploymentConfig;
import com.cloudslip.pipeline.updated.dto.kubeconfig.ServiceConfig;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessageHeader;
import com.cloudslip.pipeline.updated.manager.WebSocketMessageManager;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RollbackDeployedApplicationHelper extends AbstractHelper {


    private final Logger log = LoggerFactory.getLogger(RollbackDeployedApplicationHelper.class);

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppPipelineStep> appPipelineStep;
    private Optional<AppVpc> appVpc;
    private Optional<AppEnvironment> appEnvironment;
    private Optional<Application> application;
    private Optional<AppCommitPipelineStep> currentAppCommitPipelineStep;
    private Optional<AppCommitPipelineStep> previousAppCommitPipelineStep;
    private List<String> envFromSecretList;
    private List<AppSecret> appSecretList;

    @Autowired
    AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    @Autowired
    AppVpcRepository appVpcRepository;

    @Autowired
    AppCommitRepository appCommitRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private AppSecretRepository appSecretRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private WebSocketMessageManager webSocketMessageManager;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
        appPipelineStep = null;
        appVpc = null;
        appEnvironment = null;
        application = null;
        currentAppCommitPipelineStep = null;
        previousAppCommitPipelineStep = null;
        this.envFromSecretList = new ArrayList<>();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        appPipelineStep = appPipelineStepRepository.findByIdAndStatus(input.getId(), Status.V);
        if (!appPipelineStep.isPresent()) {
            output.generateErrorResponse("App pipeline step not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appVpc = appVpcRepository.findByIdAndStatus(appPipelineStep.get().getAppVpcObjectId(), Status.V);
        if (!appVpc.isPresent()) {
            output.generateErrorResponse("App Vpc not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appSecretList = appSecretRepository.findByCompanyIdAndApplicationIdAndUseAsEnvironmentVariableAndStatus(requester.getCompanyId(),appVpc.get().getApplicationId(),true, Status.V);
        if(appSecretList.size() > 0) {
            envFromSecretList = generateEnvFromSecretList(appSecretList);
        }

    }


    protected void doPerform() {
        Optional<AppCommit> currentAppCommit = null;
        Optional<AppCommit> previousAppCommit = null;
        if (appVpc.get().getMainCommit() != null) {
            currentAppCommit = appCommitRepository.findByIdAndStatus(appVpc.get().getMainCommit().getAppCommit().getObjectId(), Status.V);
        }
        if (appVpc.get().getPreviousCommit() != null) {
            previousAppCommit = appCommitRepository.findByIdAndStatus(appVpc.get().getPreviousCommit().getAppCommit().getObjectId(), Status.V);
        }

        if (currentAppCommit != null && previousAppCommit != null) {
            currentAppCommitPipelineStep = appCommitPipelineStepRepository.findByAppCommitIdAndAppPipelineStep_IdAndStatus(currentAppCommit.get().getObjectId(), appPipelineStep.get().getObjectId(), Status.V);
            previousAppCommitPipelineStep = appCommitPipelineStepRepository.findByAppCommitIdAndAppPipelineStep_IdAndStatus(previousAppCommit.get().getObjectId(), appPipelineStep.get().getObjectId(), Status.V);
            appVpc.get().getMainCommit().setAppCommit(previousAppCommit.get());
            appVpc.get().setPreviousCommit(null);
            AppVpc savedAppVpc = appVpcRepository.save(appVpc.get());

            application = applicationRepository.findByIdAndStatus(savedAppVpc.getApplicationId(), Status.V);
            appEnvironment = appEnvironmentRepository.findByIdAndStatus(savedAppVpc.getAppEnvironmentObjectId(), Status.V);
            int vpcIndex = appEnvironment.get().getAppVpcIndex(savedAppVpc.getObjectId());
            if (vpcIndex != -1) {
                appEnvironment.get().getAppVpcList().set(vpcIndex, savedAppVpc);
                appEnvironmentRepository.save(appEnvironment.get());
            }
        } else if (currentAppCommit != null) {
            output.generateErrorResponse("No previous commits exists!");
            throw new ApiErrorException(this.getClass().getName());
        } else {
            output.generateErrorResponse("No Commits are deployed yet!");
            throw new ApiErrorException(this.getClass().getName());
        }

        this.pushToKafka();
        output.generateSuccessResponse(appVpc.get(),"Vpc Roll backed Successfully");
        webSocketMessageManager.broadcastPipelineStepStatus(previousAppCommitPipelineStep.get().getId(), appVpc.get().getMainCommit().getAppCommit().getGitCommitId(), output.getMessage(), application.get().getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_PENDING);
    }

    private void pushToKafka() {
        AppKubeConfig appKubeConfig = new AppKubeConfig();
        appKubeConfig.setName(application.get().getUniqueName() + "-" + appEnvironment.get().getEnvironment().getShortName().toLowerCase());
        appKubeConfig = this.setKubeDeployConfig(appKubeConfig);
        appKubeConfig = this.setAutoScalingConfig(appKubeConfig);
        appKubeConfig = this.setKubeServiceConfig(appKubeConfig);
        appKubeConfig = this.setKubeIngressConfig(appKubeConfig);

        try {
            KafkaMessageHeader header = new KafkaMessageHeader(
                    appVpc.get().getVpc().getNamespace(),
                    AgentCommand.ADD_DEPLOYMENT.toString(),
                    application.get().getTeam().getCompanyId()
            );

            header.addToExtra("applicationId", appVpc.get().getMainCommit().getAppCommit().getApplicationId());
            header.addToExtra("gitCommitId", appVpc.get().getMainCommit().getAppCommit().getGitCommitId());
            header.addToExtra("organizationId", application.get().getTeam().getOrganization().getId());
            header.addToExtra("teamId", application.get().getTeam().getId());

            KafkaMessage message = new KafkaMessage(header, appKubeConfig);
            String messageAsJson = objectMapper.writeValueAsString(message);

            boolean publishedInKafka = kafkaPublisher.publishMessage(appVpc.get().getVpc().getKubeCluster().getKafkaTopic(), messageAsJson);
            if(publishedInKafka) {
                log.info("Rollback message published in Kafka: " + appVpc.get().getVpc().getKubeCluster().getKafkaTopic());
            } else {
                output.generateErrorResponse("Failed to send rollback instructions");
                throw new ApiErrorException("Failed to publish message in Kafka: " + appVpc.get().getVpc().getKubeCluster().getKafkaTopic(), this.getClass().getName());
            }

            //Updating status
            updateStatusInAppCommitPipelineStep();
            updateStatusInAppCommitState();

        } catch (JsonProcessingException ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        } catch (Exception ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        }
    }


    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private AppKubeConfig setKubeDeployConfig(AppKubeConfig appKubeConfig) {
        DeploymentConfig deploymentConfig = new DeploymentConfig(appKubeConfig.getName(), appVpc.get().getVpc().getNamespace(),
                appVpc.get().getResourceDetails().getDesiredNumberOfInstance(), application.get().getDockerRepoName(), appVpc.get().getMainCommit().getAppCommit().getGitCommitId(),
                appVpc.get().getResourceDetails().getMaxCpu(), appVpc.get().getResourceDetails().getMaxMemory(), application.get().getPort(), application.get().getMetricsPort(), application.get().getHealthCheckUrl(),envFromSecretList);

        appKubeConfig.setDeploymentConfig(deploymentConfig);
        return appKubeConfig;
    }

    private AppKubeConfig setAutoScalingConfig(AppKubeConfig appKubeConfig) {
        if(!appVpc.get().getResourceDetails().isAutoScalingEnabled()) {
            return appKubeConfig;
        }
        AutoScalingConfig autoScalingConfig = new AutoScalingConfig(appVpc.get().getResourceDetails().getMinNumOfInstance(),
                appVpc.get().getResourceDetails().getMaxNumOfInstance(), appVpc.get().getResourceDetails().getCpuThreshold(),
                appVpc.get().getResourceDetails().getTransactionPerSecondThreshold());

        appKubeConfig.setAutoScalingEnabled(true);
        appKubeConfig.setAutoScalingConfig(autoScalingConfig);
        return appKubeConfig;
    }

    private AppKubeConfig setKubeServiceConfig(AppKubeConfig appKubeConfig) {
        ServiceConfig serviceConfig = new ServiceConfig(appKubeConfig.getName(), application.get().getPort(), application.get().getMetricsPort());
        appKubeConfig.setServiceConfig(serviceConfig);
        return  appKubeConfig;
    }

    private AppKubeConfig setKubeIngressConfig(AppKubeConfig appKubeConfig) {
        if(!application.get().isIngressEnabled()) {
            return appKubeConfig;
        }
        appKubeConfig.setIngressEnabled(true);
        appKubeConfig.setIngressConfig(appVpc.get().getIngressConfig());
        return appKubeConfig;
    }

    private void updateStatusInAppCommitPipelineStep() {
        currentAppCommitPipelineStep.get().setType(PipelineStepStatusType.SUCCESS_BUT_INACTIVE);
        appCommitPipelineStepRepository.save(currentAppCommitPipelineStep.get());

        previousAppCommitPipelineStep.get().setType(PipelineStepStatusType.PENDING);
        previousAppCommitPipelineStep.get().setPipelineStartTime(String.valueOf(LocalDateTime.now()));
        appCommitPipelineStepRepository.save(previousAppCommitPipelineStep.get());
    }

    private void updateStatusInAppCommitState() {
        Optional<AppCommitState> newAppCommitState = appCommitStateRepository.findByEnvironmentStateListAppCommitPipelineStep_IdAndStatus(currentAppCommitPipelineStep.get().getObjectId(), Status.V);
        Optional<AppCommitState> previousAppCommitState = appCommitStateRepository.findByEnvironmentStateListAppCommitPipelineStep_IdAndStatus(previousAppCommitPipelineStep.get().getObjectId(), Status.V);

        for (int appEnvStateIndex = 0; appEnvStateIndex < newAppCommitState.get().getEnvironmentStateList().size(); appEnvStateIndex++) {
            AppEnvironmentStateForAppCommit appCommitEnvState = newAppCommitState.get().getEnvironmentStateList().get(appEnvStateIndex);
            int appCommitPipeLineStepIndex = appCommitEnvState.getCommitPipelineStepIndex(currentAppCommitPipelineStep.get().getAppPipelineStep());
            if (appCommitPipeLineStepIndex != -1) {
                newAppCommitState.get().getEnvironmentStateList().get(appEnvStateIndex).getSteps().set(appCommitPipeLineStepIndex, currentAppCommitPipelineStep.get());
                appCommitStateRepository.save(newAppCommitState.get());
                break;
            }
        }

        for (int appEnvStateIndex = 0; appEnvStateIndex < previousAppCommitState.get().getEnvironmentStateList().size(); appEnvStateIndex++) {
            AppEnvironmentStateForAppCommit appCommitEnvState = newAppCommitState.get().getEnvironmentStateList().get(appEnvStateIndex);
            int appCommitPipeLineStepIndex = appCommitEnvState.getCommitPipelineStepIndex(previousAppCommitPipelineStep.get().getAppPipelineStep());
            if (appCommitPipeLineStepIndex != -1) {
                previousAppCommitState.get().getEnvironmentStateList().get(appEnvStateIndex).getSteps().set(appCommitPipeLineStepIndex, previousAppCommitPipelineStep.get());
                appCommitStateRepository.save(previousAppCommitState.get());
                break;
            }
        }
    }

    private List<String> generateEnvFromSecretList( List<AppSecret> appSecretList){
        for(int i =0; i < appSecretList.size(); i++){
            envFromSecretList.add(appSecretList.get(i).getUniqueName());
        }
        return envFromSecretList;
    }
}
