package com.cloudslip.pipeline.updated.helper.app_commit_state;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppCommitState;
import com.cloudslip.pipeline.updated.repository.AppCommitStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeleteAppCommitStateHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
    }


    protected void doPerform() {
        Optional<AppCommitState> appCommitState = appCommitStateRepository.findByAppCommit_IdAndStatus(input.getId(), Status.V);
        if (appCommitState.isPresent()) {
            appCommitState.get().setStatus(Status.D);
            appCommitState.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
            appCommitState.get().setUpdatedBy(requester.getUsername());
            appCommitState.get().setLastUpdateActionId(actionId);
            appCommitStateRepository.save(appCommitState.get());
        }
        output.generateSuccessResponse(null,  "All App Commit State Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
