package com.cloudslip.pipeline.updated.helper.app_commit_state;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.manager.PipelineActionManager;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import com.cloudslip.pipeline.updated.model.AppEnvironmentStateForAppCommit;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.dummy.AppGitInfo;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.repository.AppCommitRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.AppCommitService;
import com.cloudslip.pipeline.updated.service.AppEnvironmentStateForAppCommitService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GenerateAppCommitStateHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(GenerateAppCommitStateHelper.class);

    private GenerateAppCommitStateDTO input;
    private ResponseDTO output = new ResponseDTO();

    private List<AppCommitState> appCommitStateList;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppCommitService appCommitService;

    @Autowired
    private AppEnvironmentStateForAppCommitService appEnvironmentStateForAppCommitService;

    @Autowired
    private AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private AppCommitRepository appCommitRepository;

    @Autowired
    private PipelineActionManager pipelineActionManager;

    private Optional<Application> application;

    @Value("${jenkins.app-build-trigger}")
    private String jenkinsAppBuildTrigger;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GenerateAppCommitStateDTO) input;
        this.setOutput(output);
        this.appCommitStateList = new ArrayList<>();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        application = null;
    }


    protected void checkPermission() {
        if (requester == null || !requester.hasAuthority(Authority.ROLE_GIT_AGENT) || requester.getUserType() != UserType.GIT) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        if(application.get().getCreationType() == ApplicationCreationType.FROM_GIT_SOURCE){
            int start = input.getPayloadInput().indexOf("heads/") + 6;
            int end = input.getPayloadInput().indexOf(",") - 1;
            String gitBranchName = input.getPayloadInput().substring(start, end);
            if(!gitBranchName.equals(application.get().getGitBranchName())){
                output.generateErrorResponse("git pushed into wrong branch");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
    }


    protected void doPerform() {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonPayloadObject = jsonParser.parse(input.getPayloadInput()).getAsJsonObject();
        JsonObject repository = jsonPayloadObject.getAsJsonObject("repository");
        Application currentApplication = application.get();
        if (currentApplication.getGitInfo() == null || currentApplication.getGitInfo().getGitAppId() == null || currentApplication.getGitInfo().getGitAppName() == null || currentApplication.getGitInfo().getBranchName() == null) {
            String gitAppId = repository.has("id") ? repository.get("id").getAsString() : "";
            String gitAppName = repository.has("name") ? repository.get("name").getAsString() : "";
            String gitAppBranch = repository.has("master_branch") ? repository.get("master_branch").getAsString() : "";
            AppGitInfo appGitInfo = new AppGitInfo(gitAppId, gitAppName, gitAppBranch);
            currentApplication.setGitInfo(appGitInfo);
            currentApplication = applicationRepository.save(currentApplication);
        }
        JsonArray commitArray = jsonPayloadObject.getAsJsonArray("commits");
        for (int i=0; i < commitArray.size(); i++) {
            JsonObject commit = commitArray.get(i).getAsJsonObject();
            String commitId = commit.get("id").getAsString();
            String commitMessage = commit.get("message").getAsString();
            if (commitMessage.contains("\n\n")) {
                commitMessage = commitMessage.substring(0, commitMessage.indexOf("\n\n"));
            }
            OffsetDateTime commitDateTime = OffsetDateTime.parse(commit.get("timestamp").getAsString());

            Optional<AppCommitState> existingAppCommitState = appCommitStateRepository.findByAppCommit_ApplicationIdAndAppCommit_GitCommitIdAndStatus(currentApplication.getObjectId(), commitId, Status.V);
            if (!existingAppCommitState.isPresent()) {
                AppCommitState appCommitState = new AppCommitState();

                // adding new commit for application in db
                ResponseDTO appCommitCreateResponse = appCommitService.create(new CreateAppCommitDTO(currentApplication.getObjectId(), commitId,commitMessage, new Date(commitDateTime.toInstant().toEpochMilli())), requester);
                AppCommit appCommit = objectMapper.convertValue(appCommitCreateResponse.getData(), new TypeReference<AppCommit>() { });

                // Create new app env state for the app commit in db
                ResponseDTO appEnvStateForAppCommitResponse = appEnvironmentStateForAppCommitService.createAppEnvStateForApp(new AppEnvStateForAppCommitDTO(currentApplication, appCommit.getObjectId()), requester, actionId);
                List<AppEnvironmentStateForAppCommit> environmentStateList = objectMapper.convertValue(appEnvStateForAppCommitResponse.getData(),
                        new TypeReference<List<AppEnvironmentStateForAppCommit>>() { });


                appCommitState.setAppCommit(appCommit);
                appCommitState.setEnvironmentStateList(environmentStateList);
                appCommitState.setStatus(Status.V);
                appCommitStateList.add(appCommitState);
            }
        }
        if (!appCommitStateList.isEmpty()) {
            appCommitStateList = appCommitStateRepository.saveAll(appCommitStateList);
            if (appCommitStateList.size() != 0) {
                this.triggerApplicationJob(appCommitStateList.get(appCommitStateList.size()-1), application.get(), requester);
            }
        }
        output.generateSuccessResponse(appCommitStateList, "App Commit State successfully created for application");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void triggerApplicationJob(AppCommitState appCommitState, Application application, User requester) {
        if (TriggerMode.valueOf(jenkinsAppBuildTrigger) == TriggerMode.AUTOMATIC) {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        int totalAppCommits = appCommitRepository.countAllByApplicationIdAndStatus(application.getObjectId(), Status.V);
                        if(totalAppCommits == 1) {
                            log.info("First Commit for application {}", application.getId());
                            Thread.sleep(5000);
                        }
                        log.info("Automatically triggering pipeline build job for application: {}, gitCommitId: {}", application.getName(), appCommitState.getAppCommit().getGitCommitId());
                        TriggerPipelineFromStartInputDTO dto = new TriggerPipelineFromStartInputDTO(application.getObjectId(), appCommitState.getAppCommit().getGitCommitId());
                        pipelineActionManager.triggerPipelineFromStart(dto, requester);
                    } catch (Exception ex) {

                    }
                }
            };
            thread.start();
        }
    }
}
