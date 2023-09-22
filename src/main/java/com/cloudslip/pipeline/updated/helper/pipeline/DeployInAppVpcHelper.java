package com.cloudslip.pipeline.updated.helper.pipeline;

import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.core.YamlObjectMapper;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.kubeconfig.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessageHeader;
import com.cloudslip.pipeline.updated.manager.WebSocketMessageManager;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.model.dummy.DeployedCommit;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import com.cloudslip.pipeline.updated.repository.*;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

@Component
public class DeployInAppVpcHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(DeployInAppVpcHelper.class);

    private DeployInAppVpcInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<AppEnvironment> appEnvironment;
    private Optional<AppCommitState> appCommitState;
    private List<AppSecret> appSecretList;
    private List<String> envFromSecretList;
    private AppKubeConfig appKubeConfig;

    private Hashtable<String, Boolean> codeExecutionLevelStatus;
    private AppCommitPipelineStep currentAppCommitPipelineStepOV; // OV => Old Value
    private AppCommitState currentAppCommitStateOV;
    private AppVpc currentAppVpcOV;
    private AppEnvironment currentAppEnvironmentOV;
    private AppCommitPipelineStep previousAppCommitPipelineStepOV;
    private AppCommitState previousAppCommitStateOV;


    @Autowired
    private AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private AppVpcRepository appVpcRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppSecretRepository appSecretRepository;

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private YamlObjectMapper yamlObjectMapper;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private WebSocketMessageManager webSocketMessageManager;

    @Autowired
    private Gson gson;


    @Value("${cloudslip.kafka.producer.bootstrap-servers}")
    private String kafkaServer;


    public void init(BaseInput input, Object... extraParams) {
        this.input = (DeployInAppVpcInputDTO)input;
        this.setOutput(output);
        this.codeExecutionLevelStatus = new Hashtable<String, Boolean>();
        this.envFromSecretList = new ArrayList<>();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    protected void checkPermission() {
        if ((requester == null) || (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) && !requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_DEV) && !requester.hasAuthority(Authority.ROLE_OPS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if(input.getAppCommitPipelineStep() == null) {
            output.generateErrorResponse("App Commit Pipeline Step not found to deploy!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
        if(input.getAppCommit() == null) {
            output.generateErrorResponse("App Commit not found to deploy!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
        if(input.getAppVpc() == null) {
            output.generateErrorResponse("App Vpc not found to deploy!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
        if(input.getApplication() == null) {
            output.generateErrorResponse("application not found to deploy");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
        if(input.getCompanyGitInfo() == null || input.getCompanyGitInfo().getUsername() == null || input.getCompanyGitInfo().getSecretKey() == null) {
            output.generateErrorResponse("Company Git Info not found to deploy");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
        appEnvironment = appEnvironmentRepository.findByIdAndStatus(input.getAppVpc().getAppEnvironmentObjectId(), Status.V);
        if(!appEnvironment.isPresent()) {
            output.generateErrorResponse("App Environment not found to deploy");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
        appCommitState = appCommitStateRepository.findByAppCommit_IdAndStatus(input.getAppCommit().getObjectId(), Status.V);
        if(!appCommitState.isPresent()) {
            output.generateErrorResponse("App Commit State not found to deploy");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }

        appSecretList = appSecretRepository.findByCompanyIdAndApplicationIdAndUseAsEnvironmentVariableAndStatus(requester.getCompanyId(),input.getApplication().getObjectId(),true, Status.V);
        if(appSecretList.size() > 0) {
            envFromSecretList = generateEnvFromSecretList(appSecretList);
        }

    }


    protected  void doPerform() {
        this.appKubeConfig = new AppKubeConfig();
        this.appKubeConfig.setName(input.getAppVpc().getDeploymentName());
        this.appKubeConfig.setCanaryDeployment(input.isCanaryDeployment());
        if(this.appKubeConfig.isCanaryDeployment()) {
            this.appKubeConfig.setName(this.appKubeConfig.getName() + "-canary");
        }

        this.setKubeDeployConfig();
        this.setAutoScalingConfig();
        this.setKubeServiceConfig();
        this.setKubeIngressConfig();

        DeployInAppVpcOutputDTO outputData = null;

        try {
            //Updating statuses
            updateStatusInAppCommitPipelineStep();
            updateStatusInAppCommitState();
            outputData = updateStateOfPreviousDeployment();
            updateOverallDeploymentStatus();

            //Publish Kafka Message for App Deployment
            KafkaMessageHeader header = new KafkaMessageHeader(
                    input.getAppVpc().getVpc().getNamespace(),
                    AgentCommand.ADD_DEPLOYMENT.toString(),
                    input.getApplication().getTeam().getCompanyId()
            );

            header.addToExtra("applicationId", input.getAppCommit().getApplicationId());
            header.addToExtra("gitCommitId", input.getAppCommit().getGitCommitId());
            header.addToExtra("appVpcId", input.getAppVpc().getId());
            header.addToExtra("appCommitPipelineStepId", input.getAppCommitPipelineStep().getId());
            header.addToExtra("organizationId", input.getApplication().getTeam().getOrganization().getId());
            header.addToExtra("teamId", input.getApplication().getTeam().getId());

            KafkaMessage message = new KafkaMessage(header, appKubeConfig);
            String messageAsJson = objectMapper.writeValueAsString(message);

            boolean publishedInKafka = kafkaPublisher.publishMessage(input.getAppVpc().getVpc().getKubeCluster().getKafkaTopic(), messageAsJson);
            if(publishedInKafka) {
                log.info("Deployment message published in Kafka: " + input.getAppVpc().getVpc().getKubeCluster().getKafkaTopic());
            } else {
                output.generateErrorResponse("Failed to send deployment instructions");
                throw new ApiErrorException("Failed to publish message in Kafka: " + input.getAppVpc().getVpc().getKubeCluster().getKafkaTopic(), this.getClass().getName());
            }

        } catch (JsonProcessingException ex) {
            output.generateErrorResponse("Failed to deploy! Please try again.");
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        } catch (Exception ex) {
            output.generateErrorResponse("Failed to deploy! Please try again.");
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        }

        output.generateSuccessResponse(outputData, "Deployment is now in pending. It will take a moment to start.");
        webSocketMessageManager.broadcastPipelineStepStatus(input.getAppCommitPipelineStep().getId(), input.getAppCommit().getGitCommitId(), output.getMessage(), input.getApplication().getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_PENDING);
    }


    private void setKubeDeployConfig() {
        int replicas = input.getAppVpc().getResourceDetails().getDesiredNumberOfInstance();
        int maxCpu = input.getAppVpc().getResourceDetails().getMaxCpu();
        int maxMemory = input.getAppVpc().getResourceDetails().getMaxMemory();

        if(this.input.isCanaryDeployment()) {
            replicas = 1;
            if(maxCpu > 500) {
                maxCpu = 500;
            }
            if(maxMemory > 512) {
                maxMemory = 512;
            }
        }

        DeploymentConfig deploymentConfig = new DeploymentConfig(this.appKubeConfig.getName(), input.getAppVpc().getVpc().getNamespace(),
                replicas, input.getApplication().getDockerRepoName(), input.getAppCommit().getGitCommitId(),
                maxCpu, maxMemory, input.getApplication().getPort(), input.getApplication().getMetricsPort(), input.getApplication().getHealthCheckUrl(), envFromSecretList);
        this.appKubeConfig.setDeploymentConfig(deploymentConfig);
    }

    private void setAutoScalingConfig() {
        if(input.isCanaryDeployment() || !input.getAppVpc().getResourceDetails().isAutoScalingEnabled()) {
            return;
        }
        AutoScalingConfig autoScalingConfig = new AutoScalingConfig(input.getAppVpc().getResourceDetails().getMinNumOfInstance(),
                input.getAppVpc().getResourceDetails().getMaxNumOfInstance(), input.getAppVpc().getResourceDetails().getCpuThreshold(),
                input.getAppVpc().getResourceDetails().getTransactionPerSecondThreshold());

        this.appKubeConfig.setAutoScalingEnabled(true);
        this.appKubeConfig.setAutoScalingConfig(autoScalingConfig);
    }


    private void setKubeServiceConfig() {
        ServiceConfig serviceConfig = new ServiceConfig(this.appKubeConfig.getName(), input.getApplication().getPort(), input.getApplication().getMetricsPort());
        this.appKubeConfig.setServiceConfig(serviceConfig);
    }


    private void setKubeIngressConfig() {
        if(!input.getApplication().isIngressEnabled()) {
            return;
        }
        this.appKubeConfig.setIngressEnabled(true);
        this.appKubeConfig.setIngressConfig(input.getAppVpc().getIngressConfig());
    }


    private String parseGitYamlResponseObjectToJsonString(Object responseObject, HttpEntity<String> request) {
        Class clazz = responseObject.getClass();
        Method method = null;
        String bodyString = "";
        try {
            method = clazz.getMethod("getBody");
            Object body = method.invoke(responseObject, null);

            GithubRepoFileRequestResponse repoFileRequestResponse = objectMapper.convertValue(body, GithubRepoFileRequestResponse.class);

            if(repoFileRequestResponse != null && repoFileRequestResponse.getDownload_url() != null) {
                ResponseEntity<String> downloadResponse = restTemplate.exchange(repoFileRequestResponse.getDownload_url(), HttpMethod.GET, request, String.class);
                if(downloadResponse.hasBody()) {
                    bodyString = downloadResponse.getBody();
                    bodyString = Utils.convertYamlToJson(bodyString, objectMapper, yamlObjectMapper);
                    return bodyString;
                }
            }

        } catch (NoSuchMethodException ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        } catch (IllegalAccessException ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        } catch (InvocationTargetException ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        }
        output.generateErrorResponse("Null response from git while fetching yml config");
        throw new ApiErrorException(output.getMessage(), this.getClass().getName());
    }


    private void updateStatusInAppCommitPipelineStep() {
        currentAppCommitPipelineStepOV = input.getAppCommitPipelineStep().clone(gson);
        input.getAppCommitPipelineStep().setType(PipelineStepStatusType.PENDING);
        input.getAppCommitPipelineStep().setPipelineStartTime(String.valueOf(LocalDateTime.now()));
        input.getAppCommitPipelineStep().setRunningAsCanary(input.isCanaryDeployment());
        appCommitPipelineStepRepository.save(input.getAppCommitPipelineStep());
        codeExecutionLevelStatus.put("UPDATED_CURRENT_APP_COMMIT_PIPELINE_STEP", true);
    }

    private void updateStatusInAppCommitState() {
        currentAppCommitStateOV = appCommitState.get().clone(gson);
        for (AppEnvironmentStateForAppCommit environmentState : appCommitState.get().getEnvironmentStateList()) {
            if(environmentState.getAppEnvironment().getObjectId().equals(appEnvironment.get().getObjectId())) {
                for(AppCommitPipelineStep appCommitPipelineStep : environmentState.getSteps()) {
                    if(appCommitPipelineStep.getObjectId().equals(input.getAppCommitPipelineStep().getObjectId())) {
                        appCommitPipelineStep.setType(PipelineStepStatusType.PENDING);
                        appCommitPipelineStep.setPipelineStartTime(String.valueOf(LocalDateTime.now()));
                        appCommitPipelineStep.setRunningAsCanary(input.isCanaryDeployment());
                        appCommitStateRepository.save(appCommitState.get());
                        codeExecutionLevelStatus.put("UPDATED_CURRENT_APP_COMMIT_STATE", true);
                        break;
                    }
                }
            }
        }
    }

    private void updateOverallDeploymentStatus() {
        AppVpc appVpc = input.getAppVpc();
        currentAppVpcOV = appVpc.clone(gson);
        currentAppEnvironmentOV = appEnvironment.get().clone(gson);

        Optional<Application> application = applicationRepository.findByIdAndStatus(appVpc.getApplicationId(), Status.V);

        if (!input.isCanaryDeployment() && appVpc.getMainCommit() != null && !appVpc.getMainCommit().getAppCommit().getId().equals(input.getAppCommit().getId())) {
            appVpc.setPreviousCommit(appVpc.getMainCommit());
        }

        if(!input.isCanaryDeployment()) {
            appVpc.setMainCommit(new DeployedCommit(input.getAppCommit(), PipelineStepStatusType.PENDING, application.get().isIngressEnabled() ? appVpc.getIngressConfig().getDefaultIngressUrl() : null, 100));
            if(appVpc.getCanaryCommit() != null && appVpc.getCanaryCommit().getAppCommit().getId().equals(input.getAppCommit().getId())) {
                appVpc.setCanaryCommit(null);

                // Publish kafka message to delete canary deployment in kubernetes cluster
                Optional<AppEnvironment> appEnv = appEnvironmentRepository.findByIdAndStatus(appVpc.getAppEnvironmentObjectId(), Status.V);
                List<DeleteAppDeploymentConfig> deleteAppDeploymentConfigs = new ArrayList<>();

                DeleteAppDeploymentConfig deleteAppDeploymentConfig = new DeleteAppDeploymentConfig();
                deleteAppDeploymentConfig.setNamespaceName(appVpc.getVpc().getNamespace());
                deleteAppDeploymentConfig.setDeployedName(generateCanaryPodName(application.get(), appEnv.get()));
                deleteAppDeploymentConfig.setDeleteDeployment(false);
                deleteAppDeploymentConfig.setDeleteCanary(true);
                deleteAppDeploymentConfig.setAutoConfigEnabled(appVpc.isAutoScalingEnabled());
                if (appVpc.getCanaryCommit() != null && appVpc.isCanaryDeploymentEnabled()) {
                    deleteAppDeploymentConfig.setCanaryEnabled(true);
                }
                deleteAppDeploymentConfigs.add(deleteAppDeploymentConfig);

                String kafkaTopic = appVpc.getVpc().getKubeCluster() != null ? appVpc.getVpc().getKubeCluster().getKafkaTopic() : "";
                publishInKafka(application.get(), deleteAppDeploymentConfigs, kafkaTopic);
            }
        } else {
            appVpc.setCanaryCommit(new DeployedCommit(input.getAppCommit(), PipelineStepStatusType.PENDING, application.get().isIngressEnabled() ? appVpc.getIngressConfig().getCustomIngressUrl() : null, 100));
        }

        appVpc = appVpcRepository.save(appVpc);
        codeExecutionLevelStatus.put("UPDATED_CURRENT_APP_VPC", true);

        List<AppVpc> appEnvAppVpcList =  appEnvironment.get().getAppVpcList();
        for(int i = 0; i < appEnvAppVpcList.size(); i++) {
            if(Utils.isStringEquals(appEnvAppVpcList.get(i).getId(), appVpc.getId())) {
                appEnvAppVpcList.set(i, appVpc);
                appEnvironment.get().setAppVpcList(appEnvAppVpcList);
                break;
            }
        }
        appEnvironmentRepository.save(appEnvironment.get());
        codeExecutionLevelStatus.put("UPDATED_CURRENT_APP_ENVIRONMENT", true);
    }

    private DeployInAppVpcOutputDTO updateStateOfPreviousDeployment() {
        // If main commit doesn't exist or current deploying commit and main commit is same, then no need to update these
        if (input.getAppVpc().getMainCommit() == null || input.getAppVpc().getMainCommit().getAppCommit().getId().equals(input.getAppCommit().getId())) {
            return null;
        }
        AppCommit previousActiveAppCommit;
        if(!input.isCanaryDeployment()) {
            previousActiveAppCommit = input.getAppVpc().getMainCommit().getAppCommit();
        } else {
            if(input.getAppVpc().getCanaryCommit() != null) {
                previousActiveAppCommit = input.getAppVpc().getCanaryCommit().getAppCommit();
            } else {
                return null;
            }
        }
        Optional<AppCommitState> previousActiveAppCommitState = appCommitStateRepository.findByAppCommit_IdAndStatus(previousActiveAppCommit.getObjectId(), Status.V);
        PipelineStepStatusType newStatusTypeOfPreviousActiveAppCommitPipelineStep = null;
        Optional<AppCommitPipelineStep> previousActiveAppCommitPipelineStep = appCommitPipelineStepRepository.findByAppCommitIdAndAppPipelineStep_IdAndStatus(previousActiveAppCommit.getObjectId(), input.getAppCommitPipelineStep().getAppPipelineStep().getObjectId(), Status.V);
        if(previousActiveAppCommitState.isPresent() && previousActiveAppCommitPipelineStep.isPresent()) {
            newStatusTypeOfPreviousActiveAppCommitPipelineStep = updatePreviousAppCommitPipelineStep(previousActiveAppCommitPipelineStep.get());
            updatePreviousAppCommitState(previousActiveAppCommitState.get(), previousActiveAppCommitPipelineStep.get());
            return new DeployInAppVpcOutputDTO(previousActiveAppCommitState.get().getObjectId(), previousActiveAppCommitPipelineStep.get().getObjectId(), newStatusTypeOfPreviousActiveAppCommitPipelineStep);
        }
        return null;
    }

    private PipelineStepStatusType updatePreviousAppCommitPipelineStep(AppCommitPipelineStep previousActiveAppCommitPipelineStep) {
        previousAppCommitPipelineStepOV = previousActiveAppCommitPipelineStep.clone(gson);
        if(previousActiveAppCommitPipelineStep.getType().equals(PipelineStepStatusType.SUCCESS)) {
            previousActiveAppCommitPipelineStep.setType(PipelineStepStatusType.SUCCESS_BUT_INACTIVE);
            previousActiveAppCommitPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appCommitPipelineStepRepository.save(previousActiveAppCommitPipelineStep);
            codeExecutionLevelStatus.put("UPDATED_PREVIOUS_APP_COMMIT_PIPELINE_STEP", true);
            return PipelineStepStatusType.SUCCESS_BUT_INACTIVE;
        }
        return null;
    }

    private void updatePreviousAppCommitState(AppCommitState previousActiveAppCommitState, AppCommitPipelineStep previousActiveAppCommitPipelineStep) {
        previousAppCommitStateOV = previousActiveAppCommitState.clone(gson);
        for (AppEnvironmentStateForAppCommit environmentState : previousActiveAppCommitState.getEnvironmentStateList()) {
            if(environmentState.getAppEnvironment().getObjectId().equals(appEnvironment.get().getObjectId())) {
                for(AppCommitPipelineStep appCommitPipelineStep : environmentState.getSteps()) {
                    if(appCommitPipelineStep.getObjectId().equals(previousActiveAppCommitPipelineStep.getObjectId())) {
                        if(appCommitPipelineStep.getType().equals(PipelineStepStatusType.SUCCESS)) {
                            appCommitPipelineStep.setType(PipelineStepStatusType.SUCCESS_BUT_INACTIVE);
                            appCommitPipelineStep.setPipelineStartTime(String.valueOf(LocalDateTime.now()));
                            appCommitStateRepository.save(previousActiveAppCommitState);
                            codeExecutionLevelStatus.put("UPDATED_PREVIOUS_APP_COMMIT_STATE", true);
                        }
                        break;
                    }
                }
            }
        }
    }


    protected void postPerformCheck() {
    }

    protected void doRollback() {
        if(codeExecutionLevelStatus.containsKey("UPDATED_CURRENT_APP_COMMIT_PIPELINE_STEP") && codeExecutionLevelStatus.get("UPDATED_CURRENT_APP_COMMIT_PIPELINE_STEP")) {
            appCommitPipelineStepRepository.save(currentAppCommitPipelineStepOV);
        }
        if(codeExecutionLevelStatus.containsKey("UPDATED_CURRENT_APP_COMMIT_STATE") && codeExecutionLevelStatus.get("UPDATED_CURRENT_APP_COMMIT_STATE")) {
            appCommitStateRepository.save(currentAppCommitStateOV);
        }
        if(codeExecutionLevelStatus.containsKey("UPDATED_PREVIOUS_APP_COMMIT_PIPELINE_STEP") && codeExecutionLevelStatus.get("UPDATED_PREVIOUS_APP_COMMIT_PIPELINE_STEP")) {
            appCommitPipelineStepRepository.save(previousAppCommitPipelineStepOV);
        }
        if(codeExecutionLevelStatus.containsKey("UPDATED_PREVIOUS_APP_COMMIT_STATE") && codeExecutionLevelStatus.get("UPDATED_PREVIOUS_APP_COMMIT_STATE")) {
            appCommitStateRepository.save(previousAppCommitStateOV);
        }
        if(codeExecutionLevelStatus.containsKey("UPDATED_CURRENT_APP_VPC") && codeExecutionLevelStatus.get("UPDATED_CURRENT_APP_VPC")) {
            appVpcRepository.save(currentAppVpcOV);
        }
        if(codeExecutionLevelStatus.containsKey("UPDATED_CURRENT_APP_ENVIRONMENT") && codeExecutionLevelStatus.get("UPDATED_CURRENT_APP_ENVIRONMENT")) {
            appEnvironmentRepository.save(currentAppEnvironmentOV);
        }
    }

    private String generateCanaryPodName(Application application, AppEnvironment deletedAppEnvironment) {
        return Utils.removeAllSpaceWithDash(application.getName().toLowerCase()) + "-" + application.getObjectId() + "-" + deletedAppEnvironment.getEnvironment().getShortName().toLowerCase();
    }

    private void publishInKafka(Application application, List<DeleteAppDeploymentConfig> deleteAppDeploymentConfigs, String kafkaTopic) {
        try {
            KafkaMessageHeader header = new KafkaMessageHeader(
                    AgentCommand.REMOVE_DEPLOYMENT.toString()
            );
            header.addToExtra("applicationId", application.getId());
            header.addToExtra("organizationId", application.getTeam().getOrganization().getId());
            header.addToExtra("teamId", application.getTeam().getId());

            KafkaMessage message = new KafkaMessage(header, deleteAppDeploymentConfigs);
            String messageAsJson = objectMapper.writeValueAsString(message);
            boolean publishedInKafka = kafkaPublisher.publishMessage(kafkaTopic, messageAsJson);
            if(publishedInKafka) {
                log.info("Deployment delete message published in Kafka: " + kafkaTopic);
            } else {
                output.generateErrorResponse("Failed to send deployment instructions");
                throw new ApiErrorException("Failed to publish message in Kafka: " + kafkaTopic, this.getClass().getName());
            }
        } catch (JsonProcessingException ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        } catch (Exception ex) {
            output.generateErrorResponse(ex.getMessage());
            throw new ApiErrorException(ex.getMessage(), this.getClass().getName());
        }
    }

    private List<String> generateEnvFromSecretList( List<AppSecret> appSecretList){
        for(int i =0; i < appSecretList.size(); i++){
            envFromSecretList.add(appSecretList.get(i).getUniqueName());
        }
        return envFromSecretList;
    }
}
