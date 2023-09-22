package com.cloudslip.pipeline.updated.helper.app_commit_state;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.gitappcommit.GitAppCommitDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.enums.TriggerMode;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.manager.PipelineActionManager;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import com.cloudslip.pipeline.updated.model.AppEnvironmentStateForAppCommit;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.AppCommitService;
import com.cloudslip.pipeline.updated.service.AppEnvironmentStateForAppCommitService;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class SyncAppCommitStateHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private Optional<Application> application;

    private UserInfoResponseDTO userInfoResponse;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    AppCommitService appCommitService;

    @Autowired
    AppEnvironmentStateForAppCommitService appEnvironmentStateForAppCommitService;

    @Value("${github.commitSyncLimit}")
    private String gitCommitSyncLimit;

    @Value("${jenkins.app-build-trigger}")
    private String jenkinsAppBuildTrigger;

    @Autowired
    PipelineActionManager pipelineActionManager;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
        application = null;
        userInfoResponse = null;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.application = applicationRepository.findByIdAndStatus(input.getId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (application.get().getGitInfo() == null || application.get().getGitInfo().getGitAppId() == null || application.get().getGitInfo().getGitAppId().equals("")) {
            output.generateErrorResponse("application Repository Id Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }else if (!checkAuthority(application.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
        ResponseDTO response = this.getUserAndCompanyInfoResponse(application.get());
        this.userInfoResponse = objectMapper.convertValue(response.getData(), new TypeReference<UserInfoResponseDTO>() { });
        this.validateCompany(userInfoResponse.getCompanyInfo()); // check if company has git username and secret key to access github
    }


    protected void doPerform() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Sync code will appear here
        this.getGitCommitListForApplication(application.get(), userInfoResponse);
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void getGitCommitListForApplication(Application application, UserInfoResponseDTO userInfoResponse) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization","Token " + userInfoResponse.getCompanyInfo().getGitInfo().getSecretKey());
        HttpEntity<String> request = new HttpEntity<>(headers);
        String applicationRepoName = application.getName().trim().toLowerCase().replaceAll(" ", "-");
        final String gitUrl = "https://api.github.com/repos/"+userInfoResponse.getCompanyInfo().getGitInfo().getUsername()+"/"+applicationRepoName+"/commits";
        RestTemplate restTemplate = new RestTemplate();
        Object object = restTemplate.exchange(gitUrl, HttpMethod.GET, request, Object.class);
        Class clazz = object.getClass();
        Method method = clazz.getMethod("getBody");
        Object body = method.invoke(object, null);
        List<GitAppCommitDTO> gitAppCommitList = objectMapper.convertValue(body, new TypeReference<List<GitAppCommitDTO>>() { });
        List<AppCommitState> missingAppCommitStateList = new ArrayList<>();
        Optional<AppCommitState> existingLatestAppCommitState = appCommitStateRepository.findFirstByAppCommitApplicationIdAndStatusOrderByAppCommit_CommitDateDesc(application.getObjectId(), Status.V);
        if (!gitAppCommitList.isEmpty() && gitAppCommitList.size() > 0) {
            for (int gitCommitCount = 0; gitCommitCount < gitAppCommitList.size() && gitCommitCount < Integer.parseInt(gitCommitSyncLimit); gitCommitCount++) {
                String gitCommitId = gitAppCommitList.get(gitCommitCount).getSha();
                Optional<AppCommitState> existingAppCommitState = appCommitStateRepository.findByAppCommit_GitCommitIdAndStatus(gitCommitId, Status.V);
                if (!existingAppCommitState.isPresent()) {
                    String commitMessage = gitAppCommitList.get(gitCommitCount).getCommit().getMessage();
                    if (commitMessage.contains("\n\n")) {
                        commitMessage = commitMessage.substring(0, commitMessage.indexOf("\n\n"));
                    }
                    OffsetDateTime commitDateTime = OffsetDateTime.parse(gitAppCommitList.get(gitCommitCount).getCommit().getCommitter().getDate());
                    ResponseDTO appCommitCreateResponse = appCommitService.create(new CreateAppCommitDTO(application.getObjectId(), gitCommitId, commitMessage, new Date(commitDateTime.toInstant().toEpochMilli())), requester);
                    if (appCommitCreateResponse.getStatus() == ResponseStatus.error) {
                        output.generateErrorResponse(appCommitCreateResponse.getMessage());
                        throw new ApiErrorException(this.getClass().getName());
                    }
                    AppCommit appCommit = objectMapper.convertValue(appCommitCreateResponse.getData(), new TypeReference<AppCommit>() { });

                    // Creating App Env state for app commit
                    ResponseDTO appEnvStateForAppCommitResponse = appEnvironmentStateForAppCommitService.createAppEnvStateForApp(new AppEnvStateForAppCommitDTO(application, appCommit.getObjectId()), requester, actionId);
                    if (appEnvStateForAppCommitResponse.getStatus() == ResponseStatus.error) {
                        output.generateErrorResponse(appEnvStateForAppCommitResponse.getMessage());
                        throw new ApiErrorException(this.getClass().getName());
                    }
                    List<AppEnvironmentStateForAppCommit> environmentStateList = objectMapper.convertValue(appEnvStateForAppCommitResponse.getData(),
                            new TypeReference<List<AppEnvironmentStateForAppCommit>>() { });

                    AppCommitState appCommitState = new AppCommitState();
                    appCommitState.setAppCommit(appCommit);
                    appCommitState.setEnvironmentStateList(environmentStateList);
                    appCommitState.setStatus(Status.V);
                    missingAppCommitStateList.add(appCommitState);
                }
            }
        }
        String outputMessage = "";
        if (missingAppCommitStateList.size() > 0) { // if any missing commits found
            appCommitStateRepository.saveAll(missingAppCommitStateList);
            outputMessage = "App Commit State Successfully Synced";
        } else if (!existingLatestAppCommitState.isPresent()) { // if no commits exists
            outputMessage = "Nothing is committed";
        } else {
            outputMessage = "All files are up-to date";
        }
        output.generateSuccessResponse(appCommitStateRepository.findTop20ByAppCommitApplicationIdAndStatusOrderByAppCommit_CommitDateDesc(application.getObjectId(), Status.V), outputMessage);

        Optional<AppCommitState> updatedLatestAppCommitState = appCommitStateRepository.findFirstByAppCommitApplicationIdAndStatusOrderByAppCommit_CommitDateDesc(application.getObjectId(), Status.V);
        if (existingLatestAppCommitState.isPresent() && updatedLatestAppCommitState.isPresent()
                && existingLatestAppCommitState.get().getAppCommit().getCommitDate().before(updatedLatestAppCommitState.get().getAppCommit().getCommitDate())) {
            this.triggerApplicationJob(updatedLatestAppCommitState.get(), application);
        } else if (!existingLatestAppCommitState.isPresent() && updatedLatestAppCommitState.isPresent()) {
            this.triggerApplicationJob(updatedLatestAppCommitState.get(), application);
        }
    }

    private void triggerApplicationJob(AppCommitState appCommitState, Application application) {
        if (TriggerMode.valueOf(jenkinsAppBuildTrigger) == TriggerMode.AUTOMATIC) {
            TriggerPipelineFromStartInputDTO dto = new TriggerPipelineFromStartInputDTO(application.getObjectId(), appCommitState.getAppCommit().getGitCommitId());
            pipelineActionManager.triggerPipelineFromStart(dto, requester);
        }
    }

    private void validateCompany(Company company) {
        if (company.getGitInfo() == null) {
            output.generateErrorResponse("Company Git Info Required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (company.getGitInfo().getUsername() == null || company.getGitInfo().getUsername().equals("")) {
            output.generateErrorResponse("Git User name is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (company.getGitInfo().getSecretKey() == null || company.getGitInfo().getSecretKey().equals("")) {
            output.generateErrorResponse("Git Api Token is required!");
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
}
