package com.cloudslip.pipeline.updated.helper.app_env_for_app_commit;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironmentStateForAppCommit;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentStateForAppCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeleteAllAppEnvStateForAppCommitHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

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
        List<AppEnvironmentStateForAppCommit> appEnvironmentStateForAppCommitList = appEnvironmentStateForAppCommitRepository.findAllByAppEnvironmentIdAndStatus(input.getId(), Status.V);
        for (AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit: appEnvironmentStateForAppCommitList) {
            appEnvironmentStateForAppCommit.setStatus(Status.D);
            appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
            appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
            appEnvironmentStateForAppCommitRepository.save(appEnvironmentStateForAppCommit);
        }
        output.generateSuccessResponse(null,  "All App Env for App Commit Pipeline Step Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
