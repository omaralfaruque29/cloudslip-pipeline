package com.cloudslip.pipeline.updated.helper.app_environment_checklist;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.AddAppEnvironmentChecklistDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.model.dummy.CheckItemForAppCommit;
import com.cloudslip.pipeline.updated.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddChecklistHelper extends AbstractHelper {

    private AddAppEnvironmentChecklistDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<Application> application;
    private Optional<AppEnvironment> appEnvironment;

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

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddAppEnvironmentChecklistDTO) input;
        this.setOutput(output);
        application = null;
        appEnvironment = null;
    }


    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (requester.hasAuthority(Authority.ROLE_ADMIN) && !application.get().getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appEnvironment = appEnvironmentRepository.findByIdAndStatus(input.getAppEnvironmentId(), Status.V);
        if(!appEnvironment.isPresent()) {
            output.generateErrorResponse("application Environment Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!appEnvironment.get().getApplicationId().equals(application.get().getObjectId().toString())) {
            output.generateErrorResponse(String.format("No application Environment Found Under application - %s", application.get().getName()));
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        AppEnvironmentChecklist appEnvironmentChecklist;
        Optional<AppEnvironmentChecklist> existingAppEnvironmentChecklist = appEnvironmentChecklistRepository.findByApplicationIdAndAppEnvironmentIdAndStatus(application.get().getObjectId(),
                appEnvironment.get().getObjectId(), Status.V);
        if(existingAppEnvironmentChecklist.isPresent()){
            appEnvironmentChecklist = existingAppEnvironmentChecklist.get();
        } else {
            appEnvironmentChecklist = new AppEnvironmentChecklist();
            appEnvironmentChecklist.setApplicationId(application.get().getObjectId());
            appEnvironmentChecklist.setAppEnvironmentId(appEnvironment.get().getObjectId());
        }

        List<CheckItem> existingCheckList = appEnvironmentChecklist.getChecklist() == null ? new ArrayList<>() : appEnvironmentChecklist.getChecklist();
        existingCheckList.addAll(this.getCheckItemList());
        appEnvironmentChecklist.setChecklist(existingCheckList);
        appEnvironmentChecklist.setCreatedBy(requester.getUsername());
        appEnvironmentChecklist.setCreateDate(String.valueOf(LocalDateTime.now()));
        appEnvironmentChecklist.setCreateActionId(actionId);
        appEnvironmentChecklist.setStatus(Status.V);
        appEnvironmentChecklist = appEnvironmentChecklistRepository.save(appEnvironmentChecklist);
        this.updateAppCommitState(appEnvironmentChecklist);
        output.generateSuccessResponse(appEnvironmentChecklist, "Item added to the Check List");
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private List<CheckItem> getCheckItemList(){
        if (input.getChecklists().isEmpty()) {
            output.generateErrorResponse("Check list is empty!");
            throw new ApiErrorException(this.getClass().getName());
        }
        List<CheckItem> checkItemList = new ArrayList<>();
        for (int index = 0; index < input.getChecklists().size(); index++) {
            if (input.getChecklists().get(index).getMessage() == null || input.getChecklists().get(index).getMessage().equals("")) {
                output.generateErrorResponse("Checklist Message is required!");
                throw new ApiErrorException(this.getClass().getName());
            } else if (input.getChecklists().get(index).getAuthority() == null) {
                output.generateErrorResponse(String.format("Checklist '%s' authority is missing!", input.getChecklists().get(index).getMessage()));
                throw new ApiErrorException(this.getClass().getName());
            }
            CheckItem checkItem = new CheckItem();
            checkItem.setMessage(input.getChecklists().get(index).getMessage());
            checkItem.setAuthority(input.getChecklists().get(index).getAuthority());
            checkItem.setCreatedBy(requester.getUsername());
            checkItem.setCreateDate(String.valueOf(LocalDateTime.now()));
            checkItem.setCreateActionId(actionId);
            checkItem.setStatus(Status.V);
            checkItemList.add(checkItem);
        }
        return checkItemRepository.saveAll(checkItemList);
    }

    /*
        Adding or Customizing the checklists in the app env state of existing app commit state
     */
    private void updateAppCommitState(AppEnvironmentChecklist appEnvironmentChecklist) {
        // Fetching all the app commit state list which has the provided app environment
        List<AppCommitState> existingAppCommitStateList = appCommitStateRepository.findAllByEnvironmentStateListAppEnvironmentIdAndStatus(appEnvironment.get().getObjectId(), Status.V);
        for (AppCommitState existingAppCommitState: existingAppCommitStateList) {
            // Fetching the app env index from app commit state's env list
            int environmentStateIndex = existingAppCommitState.getEnvironmentStateIndexForEnvironment(appEnvironment.get().getObjectId());

            AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit = existingAppCommitState.getEnvironmentStateList().get(environmentStateIndex);
            appEnvironmentStateForAppCommit.setCheckList(this.getCheckListItemListForAppCommitState(existingAppCommitState, environmentStateIndex, appEnvironmentChecklist.getChecklist()));
            appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
            appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
            appEnvironmentStateForAppCommit = appEnvironmentStateForAppCommitRepository.save(appEnvironmentStateForAppCommit);

            // Saving the app env state for app commit into app commit state
            existingAppCommitState.getEnvironmentStateList().set(environmentStateIndex, appEnvironmentStateForAppCommit);
            existingAppCommitState.setUpdateDate(String.valueOf(LocalDateTime.now()));
            existingAppCommitState.setUpdatedBy(requester.getUsername());
            existingAppCommitState.setLastUpdateActionId(actionId);
            appCommitStateRepository.save(existingAppCommitState);
        }
    }

    /*
        Fetching the check items and converting them to check item for app commit
     */
    private List<CheckItemForAppCommit> getCheckListItemListForAppCommitState(AppCommitState existingCommitState, int stateIndex, List<CheckItem> checkItemList) {
        List<CheckItemForAppCommit> checkItemForAppCommitList;
        if (existingCommitState.getEnvironmentStateList().get(stateIndex).getCheckList() == null || existingCommitState.getEnvironmentStateList().get(stateIndex).getCheckList().isEmpty()) {
            checkItemForAppCommitList = new ArrayList<>(); // if initially no checklist exists on the app env for app commit's check list
        } else {
            checkItemForAppCommitList = existingCommitState.getEnvironmentStateList().get(stateIndex).getCheckList();
        }
        for (CheckItem checkItem : checkItemList) {
            int checkItemIndexInCommitList = this.getCheckItemIndex(checkItemForAppCommitList, checkItem); // get then index of the check list
            if (checkItemIndexInCommitList == -1) { // if check item not exists in app commit state
                CheckItemForAppCommit checkItemForAppCommit = new CheckItemForAppCommit();
                checkItemForAppCommit.setCheckItem(checkItem);
                checkItemForAppCommit.setChecked(false);
                checkItemForAppCommitList.add(checkItemForAppCommit);
            }
        }
        return checkItemForAppCommitList;
    }

    /*
        get the index of check item in checklist
     */
    private int getCheckItemIndex(List<CheckItemForAppCommit> checkItemForAppCommitList, CheckItem checkItem) {
        for (int index = 0; index < checkItemForAppCommitList.size(); index++) {
            if (checkItemForAppCommitList.get(index).getCheckItem().getId().equals(checkItem.getId())) {
                return index;
            }
        }
        return -1;
    }
}
