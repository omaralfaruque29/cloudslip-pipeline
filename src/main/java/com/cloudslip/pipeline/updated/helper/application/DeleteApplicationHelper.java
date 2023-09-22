package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.kubeconfig.DeleteAppDeploymentConfig;
import com.cloudslip.pipeline.updated.dto.vpcresourceupdate.EnvironmentInfoUpdateDTO;
import com.cloudslip.pipeline.updated.dto.vpcresourceupdate.VpcResourceUpdateDTO;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessageHeader;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.AppCommitService;
import com.cloudslip.pipeline.updated.service.AppEnvironmentService;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.apache.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DeleteApplicationHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private boolean gitDelete = false;

    private String kafkaTopic;

    private final Logger log = LoggerFactory.getLogger(DeleteApplicationHelper.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    private Optional<Application> application;

    @Autowired
    JenkinsService jenkinsService;

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AppEnvironmentService appEnvironmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppCommitService appCommitService;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    private int retryCount = 0;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
        application = null;
        gitDelete = (boolean) extraParams[0];
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        application = applicationRepository.findByIdAndStatus(input.getId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }  else if (!checkAuthority(application.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        if (application.get().getAppCreateStatus() == ApplicationStatus.COMPLETED) {
            this.checkJenkinsJob(application.get());
            if (gitDelete) { // if git delete flag true then delete git repo
                this.deleteGitRepository(application.get());
            }
            this.deleteJenkinsJob(application.get());

            // Deleting All App Commits For A Particular application
            ResponseDTO appCommitResponse = appCommitService.deleteAllAppCommit(input, requester, actionId);
            if (appCommitResponse.getStatus() == ResponseStatus.error) {
                output.generateErrorResponse(appCommitResponse.getMessage());
                throw new ApiErrorException(this.getClass().getName());
            }
        }
        ResponseDTO appEnvironmentResponse = appEnvironmentService.deleteAppEnvironments(input, requester, actionId);
        if (appEnvironmentResponse.getStatus() == ResponseStatus.error) {
            output.generateErrorResponse(appEnvironmentResponse.getMessage());
            throw new ApiErrorException(this.getClass().getName());
        }
        List<AppEnvironment> deletedAppEnvList = objectMapper.convertValue(appEnvironmentResponse.getData(), new TypeReference<List<AppEnvironment>>() { });
        publishInKafka(application.get(), deletedAppEnvList);
        application.get().setStatus(Status.D);
        application.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        application.get().setUpdatedBy(requester.getUsername());
        application.get().setLastUpdateActionId(actionId);
        applicationRepository.save(application.get());
        output.generateSuccessResponse(null,  " application Deleted Successfully");
    }

    private void publishInKafka(Application application, List<AppEnvironment> deletedAppEnvList) {
        List<DeleteAppDeploymentConfig> deleteAppDeploymentConfigs = getAppDeploymentConfigList(application, deletedAppEnvList);
        if (!deleteAppDeploymentConfigs.isEmpty()) {
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
    }

    private List<DeleteAppDeploymentConfig> getAppDeploymentConfigList(Application application, List<AppEnvironment> deletedAppEnvList) {
        List<DeleteAppDeploymentConfig> deleteAppDeploymentConfigs = new ArrayList<>();
        for (AppEnvironment deletedAppEnvironment : deletedAppEnvList) {
            if (deletedAppEnvironment.getAppVpcList() != null) {
                for (AppVpc deletedAppVpc : deletedAppEnvironment.getAppVpcList()) {
                    if (deletedAppVpc.getMainCommit() != null && deletedAppVpc.getMainCommit().getStatusType() == PipelineStepStatusType.SUCCESS) {
                        DeleteAppDeploymentConfig deleteAppDeploymentConfig = new DeleteAppDeploymentConfig();
                        deleteAppDeploymentConfig.setNamespaceName(deletedAppVpc.getVpc().getNamespace());
                        deleteAppDeploymentConfig.setDeployedName(generatePodName(application, deletedAppEnvironment));
                        deleteAppDeploymentConfig.setDeleteDeployment(true);
                        deleteAppDeploymentConfig.setDeleteCanary(true);

                        if (deletedAppVpc.getIngressConfig() != null) {
                            if (deletedAppVpc.getIngressConfig().getDefaultIngressUrl() != null && !deletedAppVpc.getIngressConfig().getDefaultIngressUrl().equals("")) {
                                deleteAppDeploymentConfig.setDefaultIngressEnabled(true);
                            }
                            if (deletedAppVpc.getIngressConfig().getCustomIngressUrl() != null && !deletedAppVpc.getIngressConfig().getCustomIngressUrl().equals("")) {
                                deleteAppDeploymentConfig.setCustomIngressEnabled(true);
                            }
                        }

                        if (deletedAppVpc.getCanaryCommit() != null && deletedAppVpc.isCanaryDeploymentEnabled()) {
                            deleteAppDeploymentConfig.setCanaryEnabled(true);
                        }
                        deleteAppDeploymentConfig.setAutoConfigEnabled(deletedAppVpc.isAutoScalingEnabled());
                        if (!containsDuplicateAppDeploymentConfig(deleteAppDeploymentConfigs, deleteAppDeploymentConfig)) {
                            deleteAppDeploymentConfigs.add(deleteAppDeploymentConfig);
                        }

                        if (kafkaTopic == null || kafkaTopic.equals("")) {
                            kafkaTopic = deletedAppVpc.getVpc().getKubeCluster() != null ? deletedAppVpc.getVpc().getKubeCluster().getKafkaTopic() : "";
                        }
                    }
                }
            }
        }
        return deleteAppDeploymentConfigs;
    }


    private String generatePodName(Application application, AppEnvironment deletedAppEnvironment) {
        return Utils.removeAllSpaceWithDash(application.getName().toLowerCase()) + "-" + application.getObjectId() + "-" + deletedAppEnvironment.getEnvironment().getShortName().toLowerCase();
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void deleteGitRepository (Application application) {
        ResponseDTO response = this.getUserAndCompanyInfoResponse(application);
        UserInfoResponseDTO userInfoResponse = objectMapper.convertValue(response.getData(), new TypeReference<UserInfoResponseDTO>() { });
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization","token " + userInfoResponse.getCompanyInfo().getGitInfo().getSecretKey());
        HttpEntity<String> request = new HttpEntity<>(headers);
        String applicationRepoName = application.getName().trim().toLowerCase().replaceAll(" ", "-");
        final String gitUrl = "https://api.github.com/repos/"+userInfoResponse.getCompanyInfo().getGitInfo().getUsername()+"/"+applicationRepoName;
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.exchange(gitUrl, HttpMethod.DELETE, request, Object.class);
        } catch (HttpClientErrorException.NotFound ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("No repository found for the application!");
            throw new ApiErrorException(this.getClass().getName());
        } catch (HttpClientErrorException.Forbidden ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("User do not have authority to delete application repository!");
            throw new ApiErrorException(this.getClass().getName());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("Failed to delete application!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void checkJenkinsJob(Application application) {
        try {
            JobWithDetails job = jenkinsService.getJobDetails(application.getAppBuildJobNameInJenkins());
            if (job == null) {
                output.generateErrorResponse("application job not found!");
                throw new ApiErrorException(this.getClass().getName());
            }
        } catch(Exception ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("Failed to delete application!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void deleteJenkinsJob(Application application) {
        try {
            jenkinsService.deleteAppFromJenkins(application.getAppBuildJobNameInJenkins());
        } catch (NoHttpResponseException ex) {
            log.error(ex.getMessage());
            retry();
        } catch(Exception ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("error occurred while deleting job from pipeline");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void retry() {
        retryCount++;
        log.info("App Pipeline Trigger: Retry no " + Integer.toString(retryCount));
        if(retryCount <= 3) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
                output.generateErrorResponse("error occurred while retrying to trigger pipeline for application template delete");
            }
            this.deleteJenkinsJob(application.get());
        } else {
            output.generateErrorResponse("error occurred while triggering pipeline for application delete (Jenkins connection error)!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    private boolean checkAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) &&!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString());
        } else if (!requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }

    private ResponseDTO getUserAndCompanyInfoResponse(Application application) {
        HttpHeaders headers = Utils.generateHttpHeaders(requester);
        HttpEntity<GetObjectInputDTO> request = new HttpEntity<>(new GetObjectInputDTO(application.getTeam().getCompanyObjectId()), headers);
        ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/application/get-user-and-company-info", request, ResponseDTO.class);
        if(response.getStatus() == ResponseStatus.error){
            output.generateErrorResponse(response.getMessage());
            throw new ApiErrorException(this.getClass().getName());
        }
        return response;
    }

    private boolean containsDuplicateAppDeploymentConfig(List<DeleteAppDeploymentConfig> deleteAppDeploymentConfigs, DeleteAppDeploymentConfig deleteAppDeploymentConfig) {
        for (DeleteAppDeploymentConfig deploymentConfig : deleteAppDeploymentConfigs) {
            if (deploymentConfig.getNamespaceName().equals(deleteAppDeploymentConfig.getDeployedName()) && deploymentConfig.getDeployedName().equals(deleteAppDeploymentConfig.getDeployedName())) {
                return true;
            }
        }
        return false;
    }
}
