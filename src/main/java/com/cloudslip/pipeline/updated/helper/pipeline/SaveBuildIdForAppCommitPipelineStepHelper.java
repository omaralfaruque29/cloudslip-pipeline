package com.cloudslip.pipeline.updated.helper.pipeline;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.SaveBuildIdForAppCommitPipelineStepInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SaveBuildIdForAppCommitPipelineStepHelper extends AbstractHelper {

    private SaveBuildIdForAppCommitPipelineStepInputDTO input;

    @Autowired
    private AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private ResponseDTO output = new ResponseDTO();

    private Optional<Application> application;

    private Optional<AppCommitState> appCommitState;

    private Optional<AppCommitPipelineStep> appCommitPipelineStep;


    public void init(BaseInput input, Object... extraParams) {
        this.input = (SaveBuildIdForAppCommitPipelineStepInputDTO)input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if ((requester == null) || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException("Unauthorized user!", this.getClass().getName());
        }
    }


    protected void checkValidity() {
        appCommitState = appCommitStateRepository.findByAppCommit_ApplicationIdAndAppCommit_GitCommitIdAndStatus(input.getApplicationId(), input.getCommitId(), Status.V);
        if(!appCommitState.isPresent()) {
            output.generateErrorResponse("App Commit State Not Found");
            throw new ApiErrorException("App Commit State Not Found", this.getClass().getName());
        }

        appCommitPipelineStep = appCommitPipelineStepRepository.findByIdAndStatus(input.getAppCommitPipelineStepId(), Status.V);
        if(!appCommitPipelineStep.isPresent()) {
            output.generateErrorResponse("AppCommitPipelineStep not found!");
            throw new ApiErrorException("AppCommitPipelineStep not found!", this.getClass().getName());
        }
        if(!appCommitPipelineStep.get().getAppPipelineStep().getStepType().equals(PipelineStepType.BUILD)) {
            output.generateErrorResponse("PipelineStepType is not BUILD. Can't add build id to this.");
            throw new ApiErrorException("PipelineStepType is not BUILD. Can't add build id to this.", this.getClass().getName());
        }
        application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("application not found!");
            throw new ApiErrorException("application not found!", this.getClass().getName());
        }
        checkAuthority(application.get());
    }


    protected void doPerform() {
        appCommitPipelineStep.get().setJenkinsBuildId(input.getJenkinsBuildId());
        appCommitPipelineStep.get().setEstimatedTime(input.getEstimatedTime());
        appCommitPipelineStep.get().setPipelineStartTime(Utils.formatZonedDateTime(ZonedDateTime.now()));
        appCommitPipelineStep.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        appCommitPipelineStep.get().setUpdatedBy(requester.getUsername());
        appCommitPipelineStepRepository.save(appCommitPipelineStep.get());

        boolean updatedInAppCommitState = false;
        List<AppEnvironmentStateForAppCommit> appEnvironmentStateList = appCommitState.get().getEnvironmentStateList();
        for(int i = 0; i < appEnvironmentStateList.size(); i++) {
            List<AppCommitPipelineStep> appCommitPipelineStepList = appEnvironmentStateList.get(i).getSteps();
            for(int j = 0; j < appCommitPipelineStepList.size(); j++) {
                if(appCommitPipelineStepList.get(j).getId().equals(this.appCommitPipelineStep.get().getId())) {
                    appCommitPipelineStepList.set(j, this.appCommitPipelineStep.get());
                    appEnvironmentStateList.get(i).setSteps(appCommitPipelineStepList);
                    appCommitState.get().setEnvironmentStateList(appEnvironmentStateList);
                    appCommitStateRepository.save(appCommitState.get());
                    updatedInAppCommitState = true;
                    break;
                }
            }
            if(updatedInAppCommitState) {
                break;
            }
        }

        output.generateSuccessResponse(null, String.format("Build Id '%s' successfully has been saved", input.getJenkinsBuildId()));
    }


    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean checkAuthority(Application application) {
        if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            if (!application.getTeam().getCompanyObjectId().equals(this.requester.getCompanyId())) {
                output.generateErrorResponse("Unauthorized user!");
                throw new ApiErrorException("Unauthorized user!", this.getClass().getName());
            }
        }
        if (requester.hasAuthority(Authority.ROLE_DEV) || requester.hasAuthority(Authority.ROLE_OPS)) {
            if (!application.getTeam().existInTeamIdList(this.requester.getTeamIdList())) {
                output.generateErrorResponse("Requester and application doesn't belong to same team.");
                throw new ApiErrorException("Requester and application doesn't belong to same team.", this.getClass().getName());
            }
        }
        return true;
    }
}
