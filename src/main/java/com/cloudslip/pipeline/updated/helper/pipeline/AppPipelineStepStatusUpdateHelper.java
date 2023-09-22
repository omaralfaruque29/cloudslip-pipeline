package com.cloudslip.pipeline.updated.helper.pipeline;

import com.cloudslip.pipeline.updated.dto.AppPipelineStepStatusUpdateDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.model.dummy.SuccessorPipelineStep;
import com.cloudslip.pipeline.updated.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppPipelineStepStatusUpdateHelper extends AbstractHelper {

    private AppPipelineStepStatusUpdateDTO input;

    @Autowired
    private AppCommitRepository appCommitRepository;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private AppEnvironmentRepository appEnvironmentRepository;


    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

    private ResponseDTO output = new ResponseDTO();

    private Optional<AppCommit> appCommit;
    private Optional<AppVpc> appVpc;
    private Optional<AppEnvironment> appEnvironment;
    private Optional<AppCommitState> appCommitState;
    private Optional<Application> application;

    private List<AppCommitPipelineStep> appCommitPipelineStep;

    private int index = 0;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AppPipelineStepStatusUpdateDTO)input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if ((requester == null) || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        appCommit = appCommitRepository.findByApplicationIdAndGitCommitIdAndStatus(input.getApplicationId(),input.getCommitId(), Status.V);
        if(!appCommit.isPresent()) {
            output.generateErrorResponse("AppCommit not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appCommitPipelineStep = appCommitPipelineStepRepository.findAllByAppCommitIdAndStatus(appCommit.get().getObjectId(), Status.V);
        if(appCommitPipelineStep.get(index) == null) {
            output.generateErrorResponse("AppCommitPipelineStep not founds!!");
            throw new ApiErrorException(this.getClass().getName());
        }
        if(!appCommitPipelineStep.get(index).getAppPipelineStep().getStepType().equals(PipelineStepType.BUILD)) {
            output.generateErrorResponse("PipelineStepType BUILD is not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appEnvironment = appEnvironmentRepository.findByIdAndStatus(appCommitPipelineStep.get(0).getAppPipelineStep().getAppEnvironmentObjectId(), Status.V);
        if(!appEnvironment.isPresent()) {
            output.generateErrorResponse("App Environment not found to deploy");
            throw new ApiErrorException(this.getClass().getName());
        }
        appCommitState = appCommitStateRepository.findByAppCommit_IdAndStatus(appCommit.get().getObjectId(), Status.V);
        if(!appCommitState.isPresent()) {
            output.generateErrorResponse("App Commit State not found to deploy");
            throw new ApiErrorException(this.getClass().getName());
        }

        application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("application not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        checkAuthority(application.get());
    }


    protected void doPerform() {
        appCommitPipelineStep.get(index).setType(input.getStatusType());
        if(input.getStatusType().equals(PipelineStepStatusType.PIPELINE_STARTED)) {
            appCommitPipelineStep.get(index).setPipelineStartTime(String.valueOf(LocalDateTime.now()));
        }
        appCommitPipelineStep.get(index).setUpdateDate(String.valueOf(LocalDateTime.now()));
        appCommitPipelineStep.get(index).setUpdatedBy(requester.getUsername());
        appCommitPipelineStepRepository.save(appCommitPipelineStep.get(index));
        updateStatusInAppCommitState();
        output.generateSuccessResponse(null, String.format("AppCommitPipelineStep status successfully updated to '%s'", input.getStatusType()));
    }

    private void updateStatusInAppCommitState() {
        for (AppEnvironmentStateForAppCommit environmentState : appCommitState.get().getEnvironmentStateList()) {
            if(environmentState.getAppEnvironment().getObjectId().equals(appEnvironment.get().getObjectId())) {
                for(AppCommitPipelineStep appCommitPipelineStep1 : environmentState.getSteps()) {
                    if(appCommitPipelineStep1.getObjectId().equals(appCommitPipelineStep.get(index).getObjectId())) {
                        appCommitPipelineStep1.setType(input.getStatusType());
                        appCommitPipelineStep1.setPipelineStartTime(String.valueOf(LocalDateTime.now()));
                        if (input.getStatusType() == PipelineStepStatusType.PIPELINE_SUCCESS && appCommitPipelineStep1.getAppPipelineStep().getSuccessors() != null
                                && !appCommitPipelineStep1.getAppPipelineStep().getSuccessors().isEmpty()) {
                            this.enableSuccessorsOfAppCommitPipelineStep(appCommitPipelineStep1);
                        }
                        appCommitStateRepository.save(appCommitState.get());
                        break;
                    }
                }
            }
        }
    }

    /*
        Enable the successor pipeline steps of completed app commit pipeline step

     */
    private void enableSuccessorsOfAppCommitPipelineStep(AppCommitPipelineStep appCommitPipelineStep) {
        List<SuccessorPipelineStep> successorPipelineStepList = appCommitPipelineStep.getAppPipelineStep().getSuccessors();
        for (SuccessorPipelineStep successorPipelineStep : successorPipelineStepList) {
            // check in every environment for each successor. if exists then update the app environment commit state and app commit state
            for (int appEnvStateIndex = 0; appEnvStateIndex < appCommitState.get().getEnvironmentStateList().size(); appEnvStateIndex++) {
                AppEnvironmentStateForAppCommit appCommitEnvState = appCommitState.get().getEnvironmentStateList().get(appEnvStateIndex);
                int appCommitPipeLineStepIndex = appCommitEnvState.getCommitPipelineStepIndex(successorPipelineStep.getAppPipelineStep());
                if (appCommitPipeLineStepIndex != -1) {
                    AppCommitPipelineStep successorAppCommitPipelineStep = appCommitEnvState.getSteps().get(appCommitPipeLineStepIndex);
                    successorAppCommitPipelineStep.setEnabled(true);
                    successorAppCommitPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
                    successorAppCommitPipelineStep.setUpdatedBy(requester.getUsername());
                    successorAppCommitPipelineStep.setLastUpdateActionId(actionId);
                    successorAppCommitPipelineStep = appCommitPipelineStepRepository.save(successorAppCommitPipelineStep);

                    appCommitEnvState.getSteps().set(appCommitPipeLineStepIndex, successorAppCommitPipelineStep);
                    appCommitEnvState.setUpdateDate(String.valueOf(LocalDateTime.now()));
                    appCommitEnvState.setUpdatedBy(requester.getUsername());
                    appCommitEnvState.setLastUpdateActionId(actionId);
                    appCommitEnvState = appEnvironmentStateForAppCommitRepository.save(appCommitEnvState);

                    appCommitState.get().getEnvironmentStateList().set(appEnvStateIndex, appCommitEnvState);
                }
            }
        }
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean checkAuthority(Application application) {
        if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            if (!application.getTeam().getCompanyObjectId().equals(this.requester.getCompanyId())) {
                output.generateErrorResponse("Unauthorized user!");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
        if (requester.hasAuthority(Authority.ROLE_DEV) || requester.hasAuthority(Authority.ROLE_OPS)) {
            if (!application.getTeam().existInTeamIdList(this.requester.getTeamIdList())) {
                output.generateErrorResponse("");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
        return true;
    }
}
