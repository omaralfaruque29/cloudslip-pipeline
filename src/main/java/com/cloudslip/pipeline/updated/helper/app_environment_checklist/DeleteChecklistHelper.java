package com.cloudslip.pipeline.updated.helper.app_environment_checklist;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import com.cloudslip.pipeline.updated.model.AppEnvironmentChecklist;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.CheckItem;
import com.cloudslip.pipeline.updated.model.dummy.CheckItemForAppCommit;
import com.cloudslip.pipeline.updated.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeleteChecklistHelper extends AbstractHelper {

    private DeleteChecklistDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppEnvironmentChecklist> appEnvironmentChecklist;
    private Optional<Application> application;


    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppEnvironmentChecklistRepository appEnvironmentChecklistRepository;

    @Autowired
    CheckItemRepository checkItemRepository;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (DeleteChecklistDTO) input;
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
        appEnvironmentChecklist = appEnvironmentChecklistRepository.findByIdAndStatus(input.getAppEnvChecklistId(), Status.V);
        if(!appEnvironmentChecklist.isPresent()) {
            output.generateErrorResponse("application Checklist Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        if (input.getCheckItemIds().isEmpty()) {
            output.generateErrorResponse("Check item id list is empty!");
            throw new ApiErrorException(this.getClass().getName());
        }
        application = applicationRepository.findByIdAndStatus(appEnvironmentChecklist.get().getApplicationObjectId(), Status.V);
        if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && requester.hasAuthority(Authority.ROLE_ADMIN)
                && !application.get().getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        this.deleteCheckList();
        if (this.appEnvironmentChecklist.get().getChecklist().isEmpty() || this.appEnvironmentChecklist.get().getChecklist().size() == 0) { // if all check items are removed
            appEnvironmentChecklist.get().setChecklist(null);
            appEnvironmentChecklist.get().setStatus(Status.D);
        }
        appEnvironmentChecklist.get().setUpdatedBy(requester.getUsername());
        appEnvironmentChecklist.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        appEnvironmentChecklist.get().setLastUpdateActionId(actionId);
        AppEnvironmentChecklist savedAppEnvCheckList = appEnvironmentChecklistRepository.save(appEnvironmentChecklist.get());
        this.updateAppCommitState(savedAppEnvCheckList);
        output.generateSuccessResponse(appEnvironmentChecklist.get(), "Item deleted");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
    update check item in app commit state checklist
 */
    private void updateAppCommitState(AppEnvironmentChecklist savedAppEnvCheckList) {
        List<AppCommitState> existingAppCommitStateList = appCommitStateRepository.findAllByEnvironmentStateListAppEnvironmentIdAndStatus(savedAppEnvCheckList.getAppEnvironmentObjectId(), Status.V);
        for (AppCommitState existingAppCommitState : existingAppCommitStateList) {
            int environmentStateIndex = existingAppCommitState.getEnvironmentStateIndexForEnvironment(savedAppEnvCheckList.getAppEnvironmentObjectId());
            if (savedAppEnvCheckList.getStatus() == Status.D) { // if all check items are removed
                existingAppCommitState.getEnvironmentStateList().get(environmentStateIndex).setCheckList(null);
            } else {
                existingAppCommitState = this.deleteCheckListFromAppCommitState(existingAppCommitState, environmentStateIndex, savedAppEnvCheckList);
            }
            appCommitStateRepository.save(existingAppCommitState);
        }
    }

    private AppCommitState deleteCheckListFromAppCommitState(AppCommitState existingAppCommitState, int environmentStateIndex, AppEnvironmentChecklist savedAppEnvCheckList) {
        List<CheckItemForAppCommit> existingAppCommitStateCheckList = existingAppCommitState.getEnvironmentStateList().get(environmentStateIndex).getCheckList();
        /*
            If we remove item starting from end then it wont affect the remaining order
         */
        for (int index = existingAppCommitStateCheckList.size() -1; index >= 0; index--) {
            if (!this.containsCheckItem(savedAppEnvCheckList.getChecklist(), existingAppCommitStateCheckList.get(index).getCheckItem())) {
                existingAppCommitStateCheckList.remove(index);
            }
        }
        existingAppCommitState.getEnvironmentStateList().get(environmentStateIndex).setCheckList(existingAppCommitStateCheckList);
        return existingAppCommitState;
    }

    private void deleteCheckList(){
        List<CheckItem> checkItemDeleteList = new ArrayList<>();
        for (int count = 0; count < input.getCheckItemIds().size(); count++) {
            int checkItemIndex = this.appEnvironmentChecklist.get().getCheckItemIndex(input.getCheckItemIds().get(count));
            if (checkItemIndex == -1) {
                output.generateErrorResponse(String.format("No item found in app environment checklist with id - '%s'!",
                        input.getCheckItemIds().get(count)));
                throw new ApiErrorException(this.getClass().getName());
            }
            CheckItem checkItem = this.appEnvironmentChecklist.get().getChecklist().get(checkItemIndex);
            checkItem.setStatus(Status.D);
            checkItem.setUpdatedBy(requester.getUsername());
            checkItem.setUpdateDate(String.valueOf(LocalDateTime.now()));
            checkItem.setLastUpdateActionId(actionId);
            checkItemDeleteList.add(checkItem);
            this.appEnvironmentChecklist.get().getChecklist().remove(checkItemIndex);

        }
        checkItemRepository.saveAll(checkItemDeleteList);
    }

    /*
    get the index of check item in checklist
 */
    private boolean containsCheckItem(List<CheckItem> checkItemList, CheckItem checkItem) {
        for (int index = 0; index < checkItemList.size(); index++) {
            if (checkItemList.get(index).getId().equals(checkItem.getId())) {
                return true;
            }
        }
        return false;
    }
}
