package com.cloudslip.pipeline.updated.helper.app_environment;

import com.cloudslip.pipeline.updated.dto.AddPipelineStepToListDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentStateForAppCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AddPipelineStepToListHelper extends AbstractHelper {

    private AddPipelineStepToListDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppEnvironment> appEnvironment;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

    @Autowired
    AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddPipelineStepToListDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.appEnvironment = appEnvironmentRepository.findByIdAndStatus(input.getAppEnvironmentId(), Status.V);
        if (!appEnvironment.isPresent()) {
            output.generateErrorResponse("application Environment Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        List<AppPipelineStep> appPipelineStepList = appEnvironment.get().getAppPipelineStepList();
        int pipelineStepIndex = appEnvironment.get().getAppPipelineStepIndex(input.getAppPipelineStep());
        if (pipelineStepIndex == -1) {
            appPipelineStepList.add(input.getAppPipelineStep());
        } else {
            appPipelineStepList.set(pipelineStepIndex, input.getAppPipelineStep());
        }
        appEnvironment.get().setAppPipelineStepList(appPipelineStepList);
        AppEnvironment savedAppEnvironment = appEnvironmentRepository.save(appEnvironment.get());
        this.updateAppCommitState(savedAppEnvironment, input.getAppPipelineStep());
        output.generateSuccessResponse(appEnvironmentRepository.save(appEnvironment.get()),
                String.format("Pipeline Steps '%s' added to application Environment - %s",
                        this.input.getAppPipelineStep().getObjectId().toHexString(), appEnvironment.get().getObjectId().toHexString()));
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        Updating the app commit state list with the app pipeline successor
     */
    private void updateAppCommitState(AppEnvironment appEnvironment, AppPipelineStep appPipelineStep) {
        List<AppCommitState> appCommitStateList = appCommitStateRepository.findAllByAppCommitApplicationIdAndStatus(appEnvironment.getApplicationObjectId(), Status.V);
        for (AppCommitState appCommitState : appCommitStateList) {
            int environmentStateIndex = appCommitState.getEnvironmentStateIndexForEnvironment(appEnvironment.getObjectId()); // get index of the app env state from list of env states
            if (environmentStateIndex != -1) { // environment exists in app commit state
                AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit = appCommitState.getEnvironmentStateList().get(environmentStateIndex);
                int pipeLineStepIndex = this.getAppCommitPipelineStepIndex(appEnvironmentStateForAppCommit.getSteps(), appPipelineStep);
                if (pipeLineStepIndex != -1) {
                    AppCommitPipelineStep appCommitPipelineStep  = appCommitState.getEnvironmentStateList().get(environmentStateIndex).getSteps().get(pipeLineStepIndex);
                    appCommitPipelineStep.getAppPipelineStep().setSuccessors(appPipelineStep.getSuccessors());
                    appCommitPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
                    appCommitPipelineStep.setUpdatedBy(requester.getUsername());
                    appCommitPipelineStep.setLastUpdateActionId(actionId);
                    appCommitPipelineStep = appCommitPipelineStepRepository.save(appCommitPipelineStep);

                    appEnvironmentStateForAppCommit.getSteps().set(pipeLineStepIndex, appCommitPipelineStep);
                    appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
                    appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
                    appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
                }
                appEnvironmentStateForAppCommit = appEnvironmentStateForAppCommitRepository.save(appEnvironmentStateForAppCommit);

                appCommitState.getEnvironmentStateList().set(environmentStateIndex, appEnvironmentStateForAppCommit);
                appCommitState.setUpdateDate(String.valueOf(LocalDateTime.now()));
                appCommitState.setUpdatedBy(requester.getUsername());
                appCommitState.setLastUpdateActionId(actionId);
            }
            appCommitStateRepository.save(appCommitState);
        }
    }

    /*
        Getting the index of pipeline from app commit pipeline step list
     */
    private int getAppCommitPipelineStepIndex (List<AppCommitPipelineStep> appCommitPipelineStepList, AppPipelineStep appPipelineStep) {
        int index = 0;
        for (AppCommitPipelineStep appCommitPipelineStep : appCommitPipelineStepList) {
            if(appPipelineStep.getId().equals(appCommitPipelineStep.getAppPipelineStep().getId())){
                return index;
            }
            index++;
        }
        return -1;
    }
}
