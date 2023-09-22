package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.model.jenkins.JenkinsBuildResponseModel;
import com.cloudslip.pipeline.model.jenkins.JenkinsConsoleLog;
import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.constant.JenkinsJobName;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.manager.WebSocketMessageManager;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import org.apache.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scala.App;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class CreateApplicationTemplateHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(CreateApplicationTemplateHelper.class);

    private CreateApplicationTemplateDTO input;
    private ResponseDTO output = new ResponseDTO();

    private ResponseDTO response = new ResponseDTO();
    private UserInfoResponseDTO userInfoResponse;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private CustomRestTemplate restTemplate;

    private Application savedApplication;

    private List<AppEnvironment> appEnvironmentList;
    private Company company;

    @Autowired
    private JenkinsServer jenkinsServer;

    @Autowired
    private JenkinsService jenkinsService;

    @Autowired
    private WebSocketMessageManager webSocketMessageManager;


    @Value("${jenkins.url}")
    private String jenkinsUrl;

    @Value("${jenkins.user}")
    private String jenkinsUser;

    @Value("${jenkins.token}")
    private String jenkinsAccessToken;

    @Value("${github.webhook-url}")
    private String gitWebhookUrl;

    @Value("${pipeline-step.update-status-url}")
    private String pipelineUpdateStatusUrl;

    private int retryCount = 0;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateApplicationTemplateDTO) input;
        this.setOutput(output);
        this.response = (ResponseDTO) extraParams[0];
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.userInfoResponse = objectMapper.convertValue(response.getData(), new TypeReference<UserInfoResponseDTO>() { });
        savedApplication = null;
        appEnvironmentList = new ArrayList<>();
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if (input.getApplicationId() == null) {
            output.generateErrorResponse("application Id required!");
            throw new ApiErrorException(this.getClass().getName());
        }
        Optional<Application> application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!checkAuthority(application.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
        savedApplication = application.get();
        if (savedApplication.getApplicationState() == ApplicationState.PENDING_APP_DETAILS_ADDED || savedApplication.getApplicationState() == ApplicationState.PENDING_APP_VPC_AND_CONFIG_DETAILS_ADDED) {
            output.generateErrorResponse("application advance config needs to be added before creating application template");
            throw new ApiErrorException(this.getClass().getName());
        }
        appEnvironmentList = appEnvironmentRepository.findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(savedApplication.getObjectId(), Status.V);
        if (appEnvironmentList.size() == 0) {
            output.generateErrorResponse("No application Environment is Selected!");
            throw new ApiErrorException(this.getClass().getName());
        }

        ResponseDTO responseDTO = fetchCompany();
        company = objectMapper.convertValue(responseDTO.getData(), new TypeReference<Company>() { });
        if(company == null){
            output.generateErrorResponse("requester does not belong to any company");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        if (savedApplication.getAppCreateStatus() == ApplicationStatus.PENDING || savedApplication.getAppCreateStatus() == ApplicationStatus.FAILED) {
            savedApplication = updateAppCreateStatus(savedApplication, ApplicationStatus.IN_PROGRESS, ApplicationState.STARTING_INITIALIZATION);
            this.triggerPipelineJob(savedApplication, appEnvironmentList);
            output.generateSuccessResponse(savedApplication, "Initializing application");
        } else if (savedApplication.getAppCreateStatus() == ApplicationStatus.COMPLETED) {
            output.generateSuccessResponse(savedApplication, "application Template Already has been created");
        } else if (savedApplication.getAppCreateStatus() == ApplicationStatus.IN_PROGRESS) {
            output.generateSuccessResponse(savedApplication, "application Template Creation is in progress");
        }
    }


    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean checkAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString());
        } else if (!requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }

    private void triggerPipelineJob(Application application, List<AppEnvironment> appEnvironmentList) {
        try {
            Map<String, String> paramMap = this.getParametersForGenerator(application, appEnvironmentList);
            String jobName = this.getJenkinsJobName(application);
            JobWithDetails job = jenkinsServer.getJob(jobName);
            QueueReference queueRef = job.build(paramMap);
            JenkinsBuildResponseModel jenkinsBuildResponseModel = jenkinsService.buildResponseForQueuedJob(jobName, jobName, queueRef);

            if (jenkinsBuildResponseModel == null || jenkinsBuildResponseModel.getStatus().equals("FAILURE")) {
                log.error("Create application Template Failed. Null or Failure response from jenkins. Jenkins Response Status: %s", jenkinsBuildResponseModel.getStatus());
                updateAppCreateStatus(savedApplication, ApplicationStatus.FAILED, ApplicationState.INITIALIZATION_FAILED);

            } else {

                startLogFetchThread(savedApplication, jobName, queueRef);
            }

        } catch (NoHttpResponseException ex) {
            log.error(ex.getMessage());
            retry();
        } catch (Exception ex) {
            log.error("Error occurred while triggering pipeline. (Exception)");
            log.error(ex.getMessage());
            savedApplication.setAppCreateStatus(ApplicationStatus.FAILED);
            savedApplication.setApplicationState(ApplicationState.INITIALIZATION_FAILED);
            applicationRepository.save(savedApplication);
            output.generateErrorResponse("error occurred while triggering pipeline");
        }
    }

    private void startLogFetchThread(final Application application, final String jobName, final QueueReference queueRef) {
        Thread thread = new Thread() {
            public void run() {
                log.info(String.format("Starting log fetch thread of app creating job for application: %s and broadcasting topic: %s", application.getId(), application.getWebSocketTopic()));

                int logCurrentIndex = 0;
                JenkinsBuildResponseModel jenkinsBuildResponseModel = null;
                JenkinsConsoleLog jenkinsConsoleLog = null;

                try {
                    while (true) {
                        Thread.sleep(2000);
                        jenkinsBuildResponseModel = jenkinsService.getCurrentQueuedStatus(jobName, jobName, queueRef);
                        if (jenkinsBuildResponseModel != null && jenkinsBuildResponseModel.getBuildId() != null && !jenkinsBuildResponseModel.getBuildId().equals("")) {
                            break;
                        }
                    }

                    // Save buildId for fetching log in future
                    String buildId = jenkinsBuildResponseModel.getBuildId();
                    Long estimatedTime = jenkinsBuildResponseModel.getEstimatedDuration() <= 0 ? 30000L : jenkinsBuildResponseModel.getEstimatedDuration();

                    updateAppLastJenkinsBuildIdAndState(application, buildId, estimatedTime, ApplicationState.INITIALIZATION_RUNNING);

                    if (!jenkinsBuildResponseModel.getStatus().equals("FAILURE")) {
                        webSocketMessageManager.broadcastApplicationInitializationStatus("", application.getWebSocketTopic(), estimatedTime, WebSocketMessageType.APP_INITIALIZATION_RUNNING);

                        while (true) {
                            Thread.sleep(2000);
                            jenkinsConsoleLog = fetchAndBroadcastLog(jenkinsConsoleLog, jobName, buildId, logCurrentIndex, application.getWebSocketTopic());
                            logCurrentIndex = jenkinsConsoleLog.getTextSize();

                            jenkinsBuildResponseModel = jenkinsService.getCurrentQueuedStatus(jobName, jobName, queueRef);
                            if (jenkinsBuildResponseModel.getStatus().equals("FAILURE")) {
                                if (application.getAppCreateStatus() == ApplicationStatus.IN_PROGRESS) {
                                    log.error("1 - Create application Template Failed while in Progress. Jenkins Response Status: %s", jenkinsBuildResponseModel.getStatus());
                                    updateAppCreateStatus(application, ApplicationStatus.FAILED, ApplicationState.INITIALIZATION_FAILED);
                                    webSocketMessageManager.broadcastApplicationInitializationStatus("", application.getWebSocketTopic(), null, WebSocketMessageType.APP_INITIALIZATION_FAILED);
                                }
                                fetchAndBroadcastLog(jenkinsConsoleLog, jobName, buildId, logCurrentIndex, application.getWebSocketTopic());
                                break;
                            } else if (jenkinsBuildResponseModel.getNextBuildStatusUrl() == null && jenkinsBuildResponseModel.getStatus().equals("SUCCESS")) {
                                if (application.getAppCreateStatus() == ApplicationStatus.IN_PROGRESS) {
                                    updateAppCreateStatus(application, ApplicationStatus.COMPLETED, ApplicationState.INITIALIZATION_SUCCESS);
                                    webSocketMessageManager.broadcastApplicationInitializationStatus("", application.getWebSocketTopic(), null, WebSocketMessageType.APP_INITIALIZATION_SUCCESS);
                                }
                                fetchAndBroadcastLog(jenkinsConsoleLog, jobName, buildId, logCurrentIndex, application.getWebSocketTopic());
                                break;
                            }
                        }
                    } else {
                        log.error("2 - Create application Template Failed while in Progress. Jenkins Response Status: %s", jenkinsBuildResponseModel.getStatus());
                        updateAppCreateStatus(application, ApplicationStatus.FAILED, ApplicationState.INITIALIZATION_FAILED);
                        webSocketMessageManager.broadcastApplicationInitializationStatus("", application.getWebSocketTopic(), null, WebSocketMessageType.APP_INITIALIZATION_FAILED);
                    }

                } catch (InterruptedException e) {
                    updateAppCreateStatus(application, ApplicationStatus.FAILED, ApplicationState.INITIALIZATION_FAILED);
                    log.error("InterruptedException: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    updateAppCreateStatus(application, ApplicationStatus.FAILED, ApplicationState.INITIALIZATION_FAILED);
                    log.error("General Exception: " + e.getMessage());
                    e.printStackTrace();
                }
                log.info(String.format("Finished log fetch thread of app creating job with status %s for application: %s", jenkinsBuildResponseModel.getStatus(), application.getId()));
            }
        };
        thread.start();
    }

    private JenkinsConsoleLog fetchAndBroadcastLog(JenkinsConsoleLog jenkinsConsoleLog, String jobName, String buildId, int logCurrentIndex, String applicationWebSocketTopic) {
        log.info("Trying to fetch log & broadcast - jobName: {}, buildId: {}, logCurrentIndex: {}, websocketTopic: {}", jobName, buildId, logCurrentIndex, applicationWebSocketTopic);
        try {
            jenkinsConsoleLog = jenkinsService.getConsoleLogFromSpecificIndex(jobName, Integer.parseInt(buildId), logCurrentIndex);
            if (jenkinsConsoleLog != null && jenkinsConsoleLog.getLogText() != null && jenkinsConsoleLog.getLogText().length() > 0) {
                log.info("Sending Log... {}", jenkinsConsoleLog.getTextSize());
                webSocketMessageManager.broadcastApplicationInitializationLog(jenkinsConsoleLog.getLogText(), applicationWebSocketTopic);
            } else {
                log.info("No Log... {}", logCurrentIndex);
            }
            return jenkinsConsoleLog;
        } catch (Exception e) {
            log.error("Error Fetching Log & Broadcast {}: " + e.getMessage(), logCurrentIndex);
        } finally {
            return jenkinsConsoleLog;
        }
    }

    private Application updateAppCreateStatus(Application application, ApplicationStatus applicationStatus, ApplicationState applicationState) {
        log.info("Create application Template Thread: Updating status and state to {} and {} for application {}", applicationStatus, applicationState, application.getName());
        application = applicationRepository.findById(application.getObjectId()).get();
        application.setAppCreateStatus(applicationStatus);
        application.setApplicationState(applicationState);
        application.setUpdateDate(String.valueOf(LocalDateTime.now()));
        return applicationRepository.save(application);
    }

    private Application updateAppLastJenkinsBuildIdAndState(Application application, String buildId, Long estimatedTime, ApplicationState applicationState) {
        log.info("Create application Template Thread: Updating state to {} for application {}", applicationState, application.getName());
        application = applicationRepository.findById(application.getObjectId()).get();
        application.setLastJenkinsBuildIdForAppCreation(buildId);
        application.setLastJenkinsBuildEstimatedTime(estimatedTime);
        application.setLastJenkinsBuildStartTime(Utils.formatZonedDateTime(ZonedDateTime.now()));
        application.setApplicationState(applicationState);
        application.setUpdateDate(Utils.formatZonedDateTime(ZonedDateTime.now()));
        return applicationRepository.save(application);
    }

    private void retry() {
        retryCount++;
        log.info("App Pipeline Trigger: Retry no " + Integer.toString(retryCount));
        if (retryCount <= 3) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.error("On Retry Interrupted Exception:" + ex.getMessage());
                ex.printStackTrace();
                output.generateErrorResponse("error occurred while retrying to trigger pipeline for application template creation");
            }
            this.triggerPipelineJob(savedApplication, appEnvironmentList);
        } else {
            savedApplication.setAppCreateStatus(ApplicationStatus.FAILED);
            applicationRepository.save(savedApplication);
            output.generateErrorResponse("error occurred while triggering pipeline for application template creation (Jenkins connection error)!");
        }
    }

    private Map<String, String> getParametersForGenerator(Application application, List<AppEnvironment> appEnvironmentList){
        Map<String, String> allParamMap = getCommonParametersForGenerator(application);
        if (application.getType() == ApplicationType.SPRING_BOOT) {
            String appEnvironmentParameter = this.getAppEnvironmentParameter(appEnvironmentList);
            allParamMap.put("applicationBuildType", application.getBuildType().toString().toLowerCase());
            allParamMap.put("applicationPackage", application.getPackageName());
            allParamMap.put("applicationType", application.getType().toString().toLowerCase());
            allParamMap.put("appEnvironments", appEnvironmentParameter);
        }
        if(application.getCreationType() == ApplicationCreationType.FROM_GIT_SOURCE){
            if(!allParamMap.containsKey("applicationType")){
                allParamMap.put("applicationType", application.getType().toString().toLowerCase());
            }
            allParamMap.put("gitRepositoryName", application.getGitRepositoryName());
            allParamMap.put("gitBranchName", application.getGitBranchName());
        }
        return allParamMap;
    }

    private Map<String, String> getCommonParametersForGenerator(Application application) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("applicationName", application.getName().toLowerCase().replaceAll(" ", "-"));
        paramMap.put("gitUsername", userInfoResponse.getCompanyInfo().getGitInfo().getUsername());
        paramMap.put("gitAuthToken", userInfoResponse.getCompanyInfo().getGitInfo().getSecretKey());
        paramMap.put("gitWebhookUrl", gitWebhookUrl + "?appId=" + application.getObjectId() + "&accessToken=" + input.getGitAgentAccessToken());
        paramMap.put("pipelineUpdateStatusUrl", pipelineUpdateStatusUrl + "?accessToken=" + input.getGitAgentAccessToken());
        paramMap.put("jenkinsUrl", jenkinsUrl);
        paramMap.put("jenkinsUsername", jenkinsUser);
        paramMap.put("jenkinsApiToken", jenkinsAccessToken);
        paramMap.put("gitProvider", company.getGitInfo().getGitProvider());
        paramMap.put("jenkinsAppBuildName", application.getAppBuildJobNameInJenkins());
        return paramMap;
    }

    private String getJenkinsJobName(Application application) {
        if (application.getCreationType() ==  ApplicationCreationType.NEW_APP && application.getType() == ApplicationType.SPRING_BOOT) {
            return JenkinsJobName.SPRING_BOOT_APP_TEMPLATE;
        } else if (application.getCreationType() ==  ApplicationCreationType.NEW_APP && application.getType() == ApplicationType.EXPRESS_JS) {
            return JenkinsJobName.EXPRESS_JS_APP_TEMPLATE;
        } else if (application.getCreationType() ==  ApplicationCreationType.NEW_APP && application.getType() == ApplicationType.WORDPRESS){
            return JenkinsJobName.WORDPRESS_APP_TEMPLATE;
        }else if (application.getCreationType() ==  ApplicationCreationType.NEW_APP && application.getType() == ApplicationType.LARAVEL) {
            return JenkinsJobName.LARAVEL_APP_TEMPLATE;
        } else if (application.getCreationType() ==  ApplicationCreationType.NEW_APP && application.getType() == ApplicationType.DOT_NET) {
            return JenkinsJobName.DOT_NET_APP_TEMPLATE;
        } else if (application.getCreationType() == ApplicationCreationType.FROM_GIT_SOURCE){
            return JenkinsJobName.EXISTING_APP_TEMPLATE;
        }
        return "";
    }

    private String getAppEnvironmentParameter(List<AppEnvironment> appEnvironmentList) {
        String appEnvironmentParameter = "";
        for (int countAppEnv = 0; countAppEnv < appEnvironmentList.size(); countAppEnv++) {
            if (appEnvironmentList.get(countAppEnv).getAppVpcList() != null && !appEnvironmentList.get(countAppEnv).getAppVpcList().isEmpty()) {
                appEnvironmentParameter += appEnvironmentList.get(countAppEnv).getEnvironment().getShortName().toLowerCase().trim().replaceAll(" ", "_") + ":";
                for (int countCluster = 0; countCluster < appEnvironmentList.get(countAppEnv).getAppVpcList().size(); countCluster++) {
                    if (countCluster == appEnvironmentList.get(countAppEnv).getAppVpcList().size() - 1) {
                        appEnvironmentParameter += appEnvironmentList.get(countAppEnv).getAppVpcList().get(countCluster).getVpc().getName().toLowerCase().trim().replaceAll(" ", "_");
                    } else {
                        appEnvironmentParameter += appEnvironmentList.get(countAppEnv).getAppVpcList().get(countCluster).getVpc().getName().toLowerCase().trim().replaceAll(" ", "_") + ",";
                    }
                }
                if (countAppEnv < appEnvironmentList.size() - 1 && !appEnvironmentList.get(countAppEnv).getAppVpcList().isEmpty()) {
                    appEnvironmentParameter += ";";
                }
            }
        }
        return appEnvironmentParameter;
    }

    protected ResponseDTO fetchCompany() {
        HttpHeaders httpHeaders = Utils.generateHttpHeaders(requester);
        HttpEntity<String> request = new HttpEntity<>("parameters", httpHeaders);
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(applicationProperties.getUserManagementServiceBaseUrl() + "api/company/" + requester.getCompanyIdAsString(), HttpMethod.GET, request, ResponseDTO.class);
        return response.getBody();
    }
}
