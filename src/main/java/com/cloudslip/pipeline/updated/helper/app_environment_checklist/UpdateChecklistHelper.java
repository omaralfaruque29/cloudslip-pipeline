package com.cloudslip.pipeline.updated.helper.app_environment_checklist;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.UpdateAppEnvironmentChecklistDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UpdateChecklistHelper extends AbstractHelper {

    private UpdateAppEnvironmentChecklistDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppEnvironmentChecklist> appEnvironmentChecklist;
    private Optional<Application> application;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentChecklistRepository appEnvironmentChecklistRepository;

    @Autowired
    CheckItemRepository checkItemRepository;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (UpdateAppEnvironmentChecklistDTO) input;
        this.setOutput(output);
        appEnvironmentChecklist = null;
        application = null;
    }


    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if (input.getAppEnvChecklistId() == null || input.getCheckItemId() == null) {
            output.generateErrorResponse("Check item id and app env checklist id is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (input.getMessage() == null || input.getMessage().equals("")) {
            output.generateErrorResponse("Check Item Message Cannot Be Empty!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (input.getAuthority() == null) {
            output.generateErrorResponse(String.format("Checklist '%s' authority is missing!", input.getMessage()));
            throw new ApiErrorException(this.getClass().getName());
        }
        appEnvironmentChecklist = appEnvironmentChecklistRepository.findByIdAndStatus(input.getAppEnvChecklistId(), Status.V);
        if(!appEnvironmentChecklist.isPresent()) {
            output.generateErrorResponse("No application environment checklist found!");
            throw new ApiErrorException(this.getClass().getName());
        }

        application = applicationRepository.findByIdAndStatus(appEnvironmentChecklist.get().getApplicationObjectId(), Status.V);
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !application.get().getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        int checkItemIndex = this.appEnvironmentChecklist.get().getCheckItemIndex(input.getCheckItemId());
        if (checkItemIndex == -1) {
            output.generateErrorResponse(String.format("No item found in app environment checklist with id - '%s'!", input.getCheckItemId()));
            throw new ApiErrorException(this.getClass().getName());
        }

        appEnvironmentChecklist.get().setChecklist(this.getCheckList(checkItemIndex));
        appEnvironmentChecklist.get().setUpdatedBy(requester.getUsername());
        appEnvironmentChecklist.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        appEnvironmentChecklist.get().setLastUpdateActionId(actionId);
        AppEnvironmentChecklist savedAppEnvCheckList = appEnvironmentChecklistRepository.save(appEnvironmentChecklist.get());
        this.updateAppCommitState(savedAppEnvCheckList, checkItemIndex);
        output.generateSuccessResponse(savedAppEnvCheckList,"Checklist Item updated");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private List<CheckItem> getCheckList(int checkItemIndex){
        List<CheckItem> checkItemList = this.appEnvironmentChecklist.get().getChecklist();
        CheckItem checkItem = checkItemList.get(checkItemIndex);
        checkItem.setMessage(input.getMessage());
        checkItem.setAuthority(input.getAuthority());
        checkItem.setUpdatedBy(requester.getUsername());
        checkItem.setUpdateDate(String.valueOf(LocalDateTime.now()));
        checkItem.setLastUpdateActionId(actionId);
        checkItem = checkItemRepository.save(checkItem);
        checkItemList.set(checkItemIndex, checkItem);
        return checkItemList;
    }

    /*
        update check item in app commit state checklist
    */
    private void updateAppCommitState(AppEnvironmentChecklist savedAppEnvCheckList, int checkItemIndex) {
        CheckItem updatedCheckItem =  savedAppEnvCheckList.getChecklist().get(checkItemIndex);
        List<AppCommitState> existingAppCommitStateList = appCommitStateRepository.findAllByEnvironmentStateListAppEnvironmentIdAndCheckListCheckItemIdAndStatus(savedAppEnvCheckList.getAppEnvironmentObjectId(), updatedCheckItem.getObjectId(), Status.V);
        for (AppCommitState existingAppCommitState : existingAppCommitStateList) {
            int environmentStateIndex = existingAppCommitState.getEnvironmentStateIndexForEnvironment(savedAppEnvCheckList.getAppEnvironmentObjectId());

            //Update App Env For App commit state
            AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit = existingAppCommitState.getEnvironmentStateList().get(environmentStateIndex);
            appEnvironmentStateForAppCommit.getCheckList().get(checkItemIndex).setCheckItem(updatedCheckItem);
            appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
            appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
            appEnvironmentStateForAppCommit = appEnvironmentStateForAppCommitRepository.save(appEnvironmentStateForAppCommit);

            // Update App commit state
            existingAppCommitState.getEnvironmentStateList().set(environmentStateIndex, appEnvironmentStateForAppCommit);
            existingAppCommitState.setUpdateDate(String.valueOf(LocalDateTime.now()));
            existingAppCommitState.setUpdatedBy(requester.getUsername());
            existingAppCommitState.setLastUpdateActionId(actionId);
            appCommitStateRepository.save(existingAppCommitState);
        }
    }
}
