package com.cloudslip.pipeline.updated.helper.app_commit_state;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.CheckAppEnvStateChecklistDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentStateForAppCommitRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CheckAppEnvStateCheckListHelper extends AbstractHelper {

    private CheckAppEnvStateChecklistDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppCommitState> appCommitState;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CheckAppEnvStateChecklistDTO) input;
        this.setOutput(output);
        appCommitState = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if (input.getAppCommitStateId() == null || input.getAppEnvironmentId() == null) {
            output.generateErrorResponse("App Commit State Id and App Env State Id required!");
            throw new ApiErrorException(this.getClass().getName());
        }
        if (input.getCheckedItems() == null){
            output.generateErrorResponse("Check list is required!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appCommitState = appCommitStateRepository.findByIdAndEnvironmentStateListAppEnvironmentIdAndStatus(input.getAppCommitStateId(), input.getAppEnvironmentId(), Status.V);
        // Checking if a app commit state exists with provided app commit state id and app environment id
        if (!appCommitState.isPresent()) {
            output.generateErrorResponse("App Commit State not found with provided app commit state and app environment id!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        // Getting the App Environment State For App Commit from app commit state list
        int appEnvStateIndex = appCommitState.get().getEnvironmentStateIndexForEnvironment(input.getAppEnvironmentId());
        AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit = appCommitState.get().getEnvironmentStateList().get(appEnvStateIndex);
        this.validateAppEnvironmentState(appEnvironmentStateForAppCommit);

        // Get Check list items from The App Environment State For App Commit State
        List<Integer> selectedCheckItemIndexes = this.getCheckedItemIndexesInCheckItemList(appEnvironmentStateForAppCommit, input.getCheckedItems());

        // Check the provided checklist from the App Environment State For App Commit State checklist
        appEnvironmentStateForAppCommit = this.checkItemsInAppEnvStateChecklist(appEnvironmentStateForAppCommit, selectedCheckItemIndexes);
        appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
        appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
        appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
        appEnvironmentStateForAppCommit = appEnvironmentStateForAppCommitRepository.save(appEnvironmentStateForAppCommit);

        appCommitState.get().getEnvironmentStateList().set(appEnvStateIndex, appEnvironmentStateForAppCommit);
        appCommitState.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        appCommitState.get().setUpdatedBy(requester.getUsername());
        appCommitState.get().setLastUpdateActionId(actionId);
        output.generateSuccessResponse(appCommitStateRepository.save(appCommitState.get()), "Check List Updated Successfully");
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        check and uncheck items from check list & depending on check items check status, update the app env for app commit

     */
    private AppEnvironmentStateForAppCommit checkItemsInAppEnvStateChecklist(AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit,
                                                                             List<Integer> checkedItems) {

        /*
             boolean to track if any item unselected or not.
             if any item get unchecked then app env state for app commit will be disabled
         */
        boolean isAllItemChecked = true;
        for (int index = 0; index < appEnvironmentStateForAppCommit.getCheckList().size(); index++) {
            if (checkedItems.contains(index)) { // If check item index from app env state checklist contains in the checklist array checked by the user
                appEnvironmentStateForAppCommit = this.checkUncheckItem(appEnvironmentStateForAppCommit, index, true);
            } else if (appEnvironmentStateForAppCommit.getCheckList().get(index).isChecked()) { // if checked items are unchecked
                appEnvironmentStateForAppCommit = this.checkUncheckItem(appEnvironmentStateForAppCommit, index, false);
                isAllItemChecked = false;
            }
        }

        /*
            if the environment is not development then set enable app environment
            If all check items are checked then the app env for app commit will be enabled otherwise disabled
         */
        if (!appEnvironmentStateForAppCommit.getAppEnvironment().getEnvironment().getShortName().equalsIgnoreCase("dev")) {
            appEnvironmentStateForAppCommit.setEnabled(isAllItemChecked);
        }
        appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
        appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
        appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
        return appEnvironmentStateForAppCommit;
    }

    /*
        Check Uncheck the app env state check items depending on the authorities
     */
    private AppEnvironmentStateForAppCommit checkUncheckItem(AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit, int index, boolean checkStatus) {
        if (this.checkAccessOfCheckItemAuthority(appEnvironmentStateForAppCommit.getCheckList().get(index).getCheckItem())) {
            appEnvironmentStateForAppCommit.getCheckList().get(index).setChecked(checkStatus);
        } else {
            output.generateErrorResponse(String.format("User does not have authority to access check item - '%s'",
                    appEnvironmentStateForAppCommit.getCheckList().get(index).getCheckItem().getMessage()));
            throw new ApiErrorException(this.getClass().getName());
        }
        return appEnvironmentStateForAppCommit;
    }

    /*
        check if provided check item id exists in check list of app env state
        if exists then return the index numbers of the check items from the app env for app commit state's check list
     */
    private List<Integer> getCheckedItemIndexesInCheckItemList(AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit, List<ObjectId> checkedItems) {
        List<Integer> checkItemIndexList = new ArrayList<>();
        for (int count = 0; count < checkedItems.size(); count++) {
            int checkItemIndex = appEnvironmentStateForAppCommit.getCheckItemIndex(checkedItems.get(count));
            if (checkItemIndex == -1) {
                output.generateErrorResponse(String.format("'%s' environment does not have any check items with id - '%s'",
                        appEnvironmentStateForAppCommit.getAppEnvironment().getEnvironment().getName(), checkedItems.get(count)));
                throw new ApiErrorException(this.getClass().getName());

            }
            checkItemIndexList.add(checkItemIndex);
        }
        return checkItemIndexList;
    }

    /*
        Check User authority of app environment state and checklist item
     */
    private void validateAppEnvironmentState(AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit) {
        if (!this.checkAuthority(appEnvironmentStateForAppCommit.getAppEnvironment())) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (appEnvironmentStateForAppCommit.getCheckList() == null) {
            output.generateErrorResponse(String.format("'%s' environment check list has no items!",
                    appEnvironmentStateForAppCommit.getAppEnvironment().getEnvironment().getName()) );
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    /*
        check if user has authority to check or uncheck any check item in the checklist
    */
    private boolean checkAccessOfCheckItemAuthority(CheckItem checkItem) {
        if (requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN)
                || checkItem.getAuthority().isEmpty() || requester.hasAnyAuthority(checkItem.getAuthority())) {
            return true;
        }
        return false;
    }

    /*
        Check If User Has Authority To Access
     */
    private boolean checkAuthority(AppEnvironment appEnvironment) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return requester.getCompanyId().toString().equals(appEnvironment.getCompanyId());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && !requester.hasAuthority(Authority.ROLE_ADMIN)) {
            Optional<Application> application = applicationRepository.findByIdAndStatus(appEnvironment.getApplicationObjectId(), Status.V);
            return application.get().getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }
}
