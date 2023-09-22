package com.cloudslip.pipeline.updated.helper.pipeline;


import com.cloudslip.pipeline.model.jenkins.JenkinsBuildResponseModel;
import com.cloudslip.pipeline.model.jenkins.JenkinsConsoleLog;
import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.manager.PipelineActionManager;
import com.cloudslip.pipeline.updated.manager.WebSocketMessageManager;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import org.apache.http.NoHttpResponseException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TriggerPipelineBuildStepHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(TriggerPipelineBuildStepHelper.class);

    private TriggerPipelineFromStartInputDTO input;

    @Autowired
    private JenkinsServer jenkinsServer;

    @Autowired
    private JenkinsService jenkinsService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppCommitRepository appCommitRepository;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private SaveBuildIdForAppCommitPipelineStepHelper saveBuildIdForAppCommitPipelineStepHelper;

    @Autowired
    private PipelineActionManager pipelineActionManager;

    @Autowired
    private WebSocketMessageManager webSocketMessageManager;

    @Autowired
    private CustomRestTemplate customRestTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ObjectMapper objectMapper;


    private ResponseDTO output = new ResponseDTO();

    private Optional<Application> application;
    private Optional<AppCommit> appCommit;
    private AppCommitPipelineStep appCommitPipelineStep;
    private int retryCount = 0;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (TriggerPipelineFromStartInputDTO)input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if ((requester == null) ||  requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            throw new ApiErrorException("Unauthorized user!", this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if (input.getApplicationId() == null) {
            output.generateErrorResponse("Application Id required");
            throw new ApiErrorException(this.getClass().getName());
        }
        if (input.getCommitId() == null) {
            output.generateErrorResponse("Commit Id required");
            throw new ApiErrorException(this.getClass().getName());
        }
        application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("Application not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        checkAuthority(application.get());

        if(!application.get().getTeam().getCompanyObjectId().equals(requester.getCompanyId())) {
            output.generateErrorResponse("Permission denied!");
            throw new ApiErrorException(this.getClass().getName());
        }

        appCommit = appCommitRepository.findByApplicationIdAndGitCommitIdAndStatus(input.getApplicationId(),input.getCommitId(), Status.V);
        if(!appCommit.isPresent()) {
            output.generateErrorResponse("AppCommit not found!");
            throw new ApiErrorException(this.getClass().getName());
        }

        List<AppCommitPipelineStep> appCommitPipelineStepList = appCommitPipelineStepRepository.findAllByAppCommitIdAndStatus(appCommit.get().getObjectId(), Status.V);
        appCommitPipelineStep = appCommitPipelineStepList.size() > 0 ? appCommitPipelineStepList.get(0) : null;
        if(appCommitPipelineStep == null) {
            throw new ApiErrorException("AppCommitPipelineState not found", this.getClass().getName());
        }

        if(appCommitPipelineStep.isRunning()) {
            output.generateErrorResponse("Already running!");
            throw new ApiErrorException("App Commit Pipeline Step is already running", this.getClass().getName());
        }
    }

    private boolean checkAuthority(Application application) {
        if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            if (!application.getTeam().getCompanyObjectId().equals(this.requester.getCompanyId())) {
                output.generateErrorResponse("Unauthorized user!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
        }
        if (requester.hasAuthority(Authority.ROLE_DEV) || requester.hasAuthority(Authority.ROLE_OPS)) {
            if (!application.getTeam().existInTeamIdList(this.requester.getTeamIdList())) {
                output.generateErrorResponse("User doesn't exist in the team of the application");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
        }
        return true;
    }


    protected void doPerform() {
        try {
            String jobName = application.get().getAppBuildJobNameInJenkins();
            JobWithDetails job = jenkinsServer.getJob(jobName);
            Map<String, String> paramMap = getPipelineJobParam(input);
            QueueReference queueRef = job.build(paramMap);
            JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.buildResponseForQueuedJob(jobName, jobName, queueRef);

            webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStep.getId(), input.getCommitId(), "", application.get().getWebSocketTopic(), WebSocketMessageType.STARTING_PIPELINE_FOR_GIT_COMMIT);

            log.info("Pipeline Build triggered for application: {} and git commit: {}", input.getApplicationId(), input.getCommitId());

            if(application.get().getWebSocketSubscriberCount() > 0) {
                startLogFetchingThread(application.get(), input.getCommitId(), appCommitPipelineStep.getId(), jobName, queueRef, requester);
            } else {
                startFinalStatusCheckingThread(appCommitPipelineStep.getObjectId(), application.get(), input.getCommitId(), jobName, queueRef, requester);
            }

            output.generateSuccessResponse(jenkinsBuildResponseModel, String.format("Jenkins Job '%s' has been created successfully Triggered", input.getCommitId()));

        } catch (NoHttpResponseException ex) {
            log.error(ex.getMessage());
            retry();
        } catch(Exception ex) {
            log.error(ex.getMessage());
            output.generateErrorResponse("error occurred while triggering pipeline");
        }
    }

    private void startLogFetchingThread(Application application, String gitCommitId, String appCommitPipelineStepId, String jobName, QueueReference queueRef, User requester) {
        Thread thread = new Thread() {
            public void run(){
                try {
                    log.info("Build log fetching thread started for application: {} and git commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);

                    int logCurrentIndex = 0;

                    AppPipelineStepStatusUpdateDTO appPipelineStepStatusUpdateDTO = new AppPipelineStepStatusUpdateDTO(application.getObjectId(), gitCommitId, PipelineStepStatusType.RUNNING);
                    pipelineActionManager.appPipelineStepStatusUpdate(appPipelineStepStatusUpdateDTO, requester);

                    JenkinsBuildResponseModel jenkinsBuildResponseModel = null;
                    JenkinsConsoleLog jenkinsConsoleLog = null;
                    String jenkinsBuildId = null;
                    Long estimatedTime = null;
                    while(true) {
                        Thread.sleep(2000);
                        jenkinsBuildResponseModel = jenkinsService.getCurrentQueuedStatus(jobName, jobName, queueRef);
                        if(jenkinsBuildResponseModel != null && jenkinsBuildResponseModel.getBuildId() != null) {
                            jenkinsBuildId = jenkinsBuildResponseModel.getBuildId();
                            estimatedTime = jenkinsBuildResponseModel.getEstimatedDuration() <= 0 ? 50000L : jenkinsBuildResponseModel.getEstimatedDuration();
                            break;
                        }
                    }

                    log.info("Jenkins build id #{} fetched with estimated time: {} in Build Log Fetching thread for application: {} and commit: {}", jenkinsBuildId, estimatedTime, (application.getId() + " - " + application.getName()), gitCommitId);

                    //add jenkins build id to db
                    if(jenkinsBuildId != null) {
                        saveBuildIdForAppCommitPipelineStep(application.getObjectId(), gitCommitId, new ObjectId(appCommitPipelineStepId), jenkinsBuildId, estimatedTime);

                        webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStepId, gitCommitId, "", application.getWebSocketTopic(), estimatedTime, WebSocketMessageType.PIPELINE_STEP_RUNNING);

                        int hasMoreLogRetryCount = 0;
                        while(true) {
                            Thread.sleep(2000);

                            jenkinsConsoleLog = fetchAndBroadcastLog(jenkinsConsoleLog, jobName, jenkinsBuildId, logCurrentIndex, appCommitPipelineStepId, gitCommitId, application.getWebSocketTopic());
                            logCurrentIndex = jenkinsConsoleLog.getTextSize();

                            jenkinsBuildResponseModel = jenkinsService.getCurrentQueuedStatus(jobName, jobName, queueRef);

                            if(jenkinsBuildResponseModel.getStatus().equals("FAILURE")){
                                log.info("Breaking log fetching thread by Failure for application: {} and git commit id: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                fetchAndBroadcastLog(jenkinsConsoleLog, jobName, jenkinsBuildId, logCurrentIndex, appCommitPipelineStepId, gitCommitId, application.getWebSocketTopic());
                                webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStepId, gitCommitId, "", application.getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_FAILED);
                                break;

                            } else if(jenkinsBuildResponseModel.getNextBuildStatusUrl() == null && jenkinsBuildResponseModel.getStatus().equals("SUCCESS")) {
                                log.info("Breaking log fetching thread by Success for application: {} and git commit id: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                fetchAndBroadcastLog(jenkinsConsoleLog, jobName, jenkinsBuildId, logCurrentIndex, appCommitPipelineStepId, gitCommitId, application.getWebSocketTopic());
                                webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStepId, gitCommitId, "", application.getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_SUCCESS);
                                break;

                            } else if(!jenkinsConsoleLog.isHasMoreText()) {
                                hasMoreLogRetryCount++;
                                if(hasMoreLogRetryCount > 30) {
                                    log.info("Breaking log fetching thread by exceeding retry count for application: {} and git commit id: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                    break;
                                }
                            } else if(jenkinsConsoleLog.isHasMoreText()) {
                                hasMoreLogRetryCount = 0;
                            }
                        }
                        log.info("Build log fetching thread ends with status {} for application: {} and git commit: {}", jenkinsBuildResponseModel.getStatus(), (application.getId() + " - " + application.getName()), gitCommitId);
                    } else {
                        log.info("Build log fetching thread ends with a failure. could not get build id from jenkins for application: {} and git commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                    }

                } catch (InterruptedException ex) {
                    log.error("Build log fetching thread: " + ex.getMessage());
                } catch (Exception ex) {
                    log.error("Build log fetching thread: " + ex.getMessage());
                }
            }
        };
        thread.start();
    }


    private Map<String, String> getPipelineJobParam(TriggerPipelineFromStartInputDTO input) throws URISyntaxException{
        Map<String, String> pipelineJobParam = new HashMap<>();
        Company company = getCompany();
        pipelineJobParam.put("commitId", input.getCommitId());
        pipelineJobParam.put("applicationId", input.getApplicationId().toString());
        pipelineJobParam.put("dockerHubId", company.getDockerHubInfo().getDockerhubId());
        pipelineJobParam.put("dockerHubPassword", Utils.getBase64DecodedString(company.getDockerHubInfo().getDockerhubPassword()));
        Optional<Application> application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        pipelineJobParam.put("dockerHubRepository", application.get().getName().toLowerCase().trim().replaceAll(" ","_"));
        return pipelineJobParam;
    }

    private Company getCompany() {
        HttpHeaders headers = Utils.generateHttpHeaders(requester);
        HttpEntity<String> request = new HttpEntity<>("parameters", headers);
        ResponseEntity<ResponseDTO> response = customRestTemplate.exchange(applicationProperties.getUserManagementServiceBaseUrl() + "api/company/"+requester.getCompanyId(), HttpMethod.GET, request, ResponseDTO.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Company company = objectMapper.convertValue(response.getBody().getData(), new TypeReference<Company>() { });
        return company;
    }

    private JenkinsConsoleLog fetchAndBroadcastLog(JenkinsConsoleLog jenkinsConsoleLog, String jobName, String jenkinsBuildId, int logCurrentIndex, String appCommitPipelineStepId, String gitCommitId, String applicationWebSocketTopic) {
        try {
            jenkinsConsoleLog = jenkinsService.getConsoleLogFromSpecificIndex(jobName, Integer.parseInt(jenkinsBuildId), logCurrentIndex);
            if(jenkinsConsoleLog != null && jenkinsConsoleLog.getLogText() != null && jenkinsConsoleLog.getLogText().length() > 0) {
                webSocketMessageManager.broadcastPipelineStepLog(appCommitPipelineStepId, gitCommitId, jenkinsConsoleLog.getLogText(), applicationWebSocketTopic);
            }
            return jenkinsConsoleLog;
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            return jenkinsConsoleLog;
        }
    }


    private void startFinalStatusCheckingThread(ObjectId appCommitPipelineStepId, Application application, String gitCommitId, String jobName, QueueReference queueRef, User requester) {
        if(appCommitPipelineStepId == null) {
            log.error("AppCommitPipelineStateId is null while starting Final Status Checking thread.");
        } else {
            Thread thread = new Thread() {
                public void run() {
                    log.info("Starting Final Status checking thread of Pipeline Build Step for application: {} and commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                    try {
                        AppPipelineStepStatusUpdateDTO appPipelineStepStatusUpdateDTO = new AppPipelineStepStatusUpdateDTO(application.getObjectId(), gitCommitId, PipelineStepStatusType.RUNNING);
                        pipelineActionManager.appPipelineStepStatusUpdate(appPipelineStepStatusUpdateDTO, requester);

                        JenkinsBuildResponseModel jenkinsBuildResponseModel = null;
                        String jenkinsBuildId = null;
                        Long estimatedTime = null;

                        while (true) {
                            Thread.sleep(2000);
                            jenkinsBuildResponseModel = jenkinsService.getCurrentQueuedStatus(jobName, jobName, queueRef);
                            if (jenkinsBuildResponseModel != null && jenkinsBuildResponseModel.getBuildId() != null) {
                                jenkinsBuildId = jenkinsBuildResponseModel.getBuildId();
                                estimatedTime = jenkinsBuildResponseModel.getEstimatedDuration() <= 0 ? 50000L : jenkinsBuildResponseModel.getEstimatedDuration();
                                break;
                            }
                        }

                        log.info("Jenkins build id #{} fetched with estimated time: {} in Final Status Checking thread for application: {} and commit: {}", jenkinsBuildId, jenkinsBuildResponseModel.getEstimatedDuration(), (application.getId() + " - " + application.getName()), gitCommitId);
                        //add jenkins build id to db
                        if (jenkinsBuildId != null) {
                            saveBuildIdForAppCommitPipelineStep(application.getObjectId(), gitCommitId, appCommitPipelineStepId, jenkinsBuildId, jenkinsBuildResponseModel.getEstimatedDuration());
                        }

                        int retryCount = 0;

                        while(retryCount < 3) {
                            retryCount++;

                            Thread.sleep((estimatedTime + 10000));

                            AppCommitPipelineStep appCommitPipelineStep = appCommitPipelineStepRepository.findByIdAndStatus(appCommitPipelineStepId, Status.V).get();
                            if (!(appCommitPipelineStep.getType().equals(PipelineStepStatusType.PIPELINE_SUCCESS))) {

                                jenkinsBuildResponseModel = jenkinsService.getJobBuildInfo(jobName, Integer.parseInt(jenkinsBuildId));

                                if(jenkinsBuildResponseModel.getStatus().equals("FAILURE")) {
                                    log.info("Pipeline Build Step failed for application: {} and commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                    updateAppCommitPipelineStepStatusForFailure(appCommitPipelineStep.getType(), application.getObjectId(), gitCommitId, requester);
                                    webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStep.getId(), gitCommitId, "", application.getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_FAILED);
                                    break;
                                } else if(jenkinsBuildResponseModel.getStatus().equals("SUCCESS")) {
                                    log.info("Pipeline Build Step was a SUCCESS for application: {} and commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                    updateAppCommitPipelineStepStatus(PipelineStepStatusType.PIPELINE_SUCCESS, application.getObjectId(), gitCommitId, requester);
                                    webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStep.getId(), gitCommitId, "", application.getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_SUCCESS);
                                    break;
                                } else if(jenkinsBuildResponseModel.getStatus().equals("IN_PROGRESS")) {
                                    if(retryCount == 3) {
                                        log.info("Pipeline Build Step failed due to retry timeout for application: {} and commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                        updateAppCommitPipelineStepStatusForFailure(appCommitPipelineStep.getType(), application.getObjectId(), gitCommitId, requester);
                                        webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStep.getId(), gitCommitId, "", application.getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_FAILED);
                                        break;
                                    }
                                    continue;
                                }

                            } else {
                                log.info("Pipeline Build Step was a SUCCESS for application: {} and commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                                webSocketMessageManager.broadcastPipelineStepStatus(appCommitPipelineStep.getId(), gitCommitId, "", application.getWebSocketTopic(), WebSocketMessageType.PIPELINE_STEP_SUCCESS);
                                break;
                            }
                        }
                    } catch (InterruptedException ex) {
                        log.error("Final Status Checking Thread - Interrupted Exception: " + ex.getMessage());
                    } catch (Exception ex) {
                        log.error("Final Status Checking Thread - General Exception: " + ex.getMessage());
                    }
                    log.info("Finished Final Status checking thread of Pipeline Build Step for application: {} and commit: {}", (application.getId() + " - " + application.getName()), gitCommitId);
                }
            };
            thread.start();
        }
    }

    private void retry() {
        retryCount++;
        log.info("App Pipeline Trigger: Retry no " + Integer.toString(retryCount) + " for application: {} and git commit id: {}", (application.get().getId() + " - " + application.get().getName()), input.getCommitId());
        if(retryCount <= 3) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.error("Interrupted Exception while retrying app pipeline trigger for application: {}, git commit id: {}. Exception Message: " + ex.getMessage(), (application.get().getId() + " - " + application.get().getName()), input.getCommitId());
                output.generateErrorResponse("error occurred while retrying to trigger pipeline");
            }
            doPerform();
        } else {
            output.generateErrorResponse("error occurred while triggering pipeline (Jenkins connection error)!");
        }
    }


    private void updateAppCommitPipelineStepStatus(PipelineStepStatusType targetStatusType, ObjectId applicationId, String gitCommitId, User requester) {
        log.info("Pipeline Build Final Status update for application {} and git commit ID {} : Updated Status {}", (application.get().getId() + " - " + application.get().getName()), input.getCommitId(), targetStatusType);

        AppPipelineStepStatusUpdateDTO appPipelineStepStatusUpdateDTO = new AppPipelineStepStatusUpdateDTO();
        appPipelineStepStatusUpdateDTO.setApplicationId(applicationId);
        appPipelineStepStatusUpdateDTO.setCommitId(gitCommitId);
        appPipelineStepStatusUpdateDTO.setStatusType(targetStatusType);

        pipelineActionManager.appPipelineStepStatusUpdate(appPipelineStepStatusUpdateDTO, requester);
    }

    private void updateAppCommitPipelineStepStatusForFailure(PipelineStepStatusType currentPipelineStepStatusType, ObjectId applicationId, String gitCommitId, User requester) {
        log.info("Pipeline Build Final Status Check for application {} and git commit ID {} : Last Status {}", (application.get().getId() + " - " + application.get().getName()), input.getCommitId(), currentPipelineStepStatusType);

        PipelineStepStatusType targetStatusType = PipelineStepStatusType.PIPELINE_FAILED;

        if(currentPipelineStepStatusType.equals(PipelineStepStatusType.RUNNING)) {
            targetStatusType = PipelineStepStatusType.PIPELINE_START_FAILED;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.PIPELINE_STARTED)){
            targetStatusType = PipelineStepStatusType.PIPELINE_START_FAILED;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.CLONING_GIT)){
            targetStatusType = PipelineStepStatusType.GIT_CLONE_FAILED;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.GRADLE_BUILDING)){
            targetStatusType = PipelineStepStatusType.GRADLE_BUILD_FAILED;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.MAVEN_BUILDING)){
            targetStatusType = PipelineStepStatusType.MAVEN_BUILD_FAILED;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.BUILDING_IMAGE)){
            targetStatusType = PipelineStepStatusType.BUILDING_IMAGE;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.DEPLOYING_IMAGE)) {
            targetStatusType = PipelineStepStatusType.DEPLOY_IMAGE_FAILED;
        } else if(currentPipelineStepStatusType.equals(PipelineStepStatusType.REMOVING_UNUSED_DOCKER_IMAGE)){
            targetStatusType = PipelineStepStatusType.REMOVE_UNUSED_DOCKER_IMAGE_FAILED;
        }

        updateAppCommitPipelineStepStatus(targetStatusType, applicationId, gitCommitId, requester);
    }

    private void saveBuildIdForAppCommitPipelineStep(ObjectId applicationId, String gitCommitId, ObjectId appCommitPipelineStepId, String buildId, Long estimatedTime) {
        SaveBuildIdForAppCommitPipelineStepInputDTO input = new SaveBuildIdForAppCommitPipelineStepInputDTO();
        input.setApplicationId(applicationId);
        input.setCommitId(gitCommitId);
        input.setAppCommitPipelineStepId(appCommitPipelineStepId);
        input.setJenkinsBuildId(buildId);
        input.setEstimatedTime(estimatedTime);
        saveBuildIdForAppCommitPipelineStepHelper.execute(input, requester);
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
