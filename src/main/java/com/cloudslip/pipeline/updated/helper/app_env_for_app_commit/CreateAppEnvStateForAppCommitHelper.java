package com.cloudslip.pipeline.updated.helper.app_env_for_app_commit;

import com.cloudslip.pipeline.updated.dto.AppEnvStateForAppCommitDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.CreateAppCommitPipelineStepDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.model.CheckItem;
import com.cloudslip.pipeline.updated.model.dummy.CheckItemForAppCommit;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentChecklistRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentStateForAppCommitRepository;
import com.cloudslip.pipeline.updated.service.AppCommitPipelineStepService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CreateAppEnvStateForAppCommitHelper extends AbstractHelper {

    private AppEnvStateForAppCommitDTO input;
    private ResponseDTO output = new ResponseDTO();
    private List<AppEnvironmentStateForAppCommit> appEnvStateForAppCommitList;
    private List<AppEnvironment> appEnvironmentList;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    AppCommitPipelineStepService appCommitPipelineStepService;

    @Autowired
    AppEnvironmentChecklistRepository appEnvironmentChecklistRepository;

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvStateForAppCommitRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AppEnvStateForAppCommitDTO) input;
        this.setOutput(output);
        this.appEnvStateForAppCommitList = new ArrayList<>();
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
    }


    protected void doPerform() {
        this.appEnvironmentList = appEnvironmentRepository.findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(input.getApplication().getObjectId(), Status.V);
        // Commit States for Each Selected App Environment
        for(AppEnvironment appEnvironment : appEnvironmentList) {
            AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit = new AppEnvironmentStateForAppCommit();
            appEnvironmentStateForAppCommit.setAppEnvironment(appEnvironment);
            appEnvironmentStateForAppCommit.setAppCommitId(input.getCommitId());

            // For each app pipeline step creating pipeline step for app commit
            ResponseDTO appCommitPipelineStepListResponse = appCommitPipelineStepService.createAppCommitPipelineStep(new CreateAppCommitPipelineStepDTO(input.getCommitId(), appEnvironment) , requester, actionId);
            if (appCommitPipelineStepListResponse.getStatus() == ResponseStatus.error) {
                output.generateErrorResponse(appCommitPipelineStepListResponse.getMessage());
                throw new ApiErrorException(this.getClass().getName());
            }
            List<AppCommitPipelineStep> appCommitPipelineStepList = objectMapper.convertValue(appCommitPipelineStepListResponse.getData(),
                    new TypeReference<List<AppCommitPipelineStep>>() { });
            appEnvironmentStateForAppCommit.setSteps(appCommitPipelineStepList);
            List<CheckItemForAppCommit> checkItemForAppCommitList = getCheckListItemList(input.getApplication(), appEnvironment);
            appEnvironmentStateForAppCommit.setCheckList(checkItemForAppCommitList);

            //If environment is development or no check item exists in the check list then app state for app commit will be enabled
            if (appEnvironment.getEnvironment().getShortName().equalsIgnoreCase("dev") || checkItemForAppCommitList == null || checkItemForAppCommitList.isEmpty()) {
                appEnvironmentStateForAppCommit.setEnabled(true);
            } else {
                appEnvironmentStateForAppCommit.setEnabled(false);
            }
            appEnvironmentStateForAppCommit.setCreatedBy(requester.getUsername());
            appEnvironmentStateForAppCommit.setCreateDate(String.valueOf(LocalDateTime.now()));
            appEnvironmentStateForAppCommit.setCreateActionId(actionId);
            appEnvironmentStateForAppCommit.setStatus(Status.V);
            appEnvStateForAppCommitList.add(appEnvironmentStateForAppCommit);
        }
        output.generateSuccessResponse(appEnvStateForAppCommitRepository.saveAll(appEnvStateForAppCommitList),
                "App Commit Pipeline step has been successfully created for application");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private List<CheckItemForAppCommit> getCheckListItemList(Application application, AppEnvironment appEnvironment) {
        Optional<AppEnvironmentChecklist> appEnvironmentChecklist = appEnvironmentChecklistRepository.findByApplicationIdAndAppEnvironmentIdAndStatus(application.getObjectId(),
                appEnvironment.getObjectId(), Status.V);
        if (!appEnvironmentChecklist.isPresent()) {
            return null;
        } else if (appEnvironmentChecklist.get().getChecklist().size() == 0) {
            return null;
        }
        List<CheckItemForAppCommit> checkItemForAppCommitList = new ArrayList<>();
        for (CheckItem checkItem : appEnvironmentChecklist.get().getChecklist()) {
            CheckItemForAppCommit checkItemForAppCommit = new CheckItemForAppCommit();
            checkItemForAppCommit.setCheckItem(checkItem);
            checkItemForAppCommit.setChecked(false);
            checkItemForAppCommitList.add(checkItemForAppCommit);
        }
        return checkItemForAppCommitList;
    }
}
