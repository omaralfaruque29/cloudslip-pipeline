package com.cloudslip.pipeline.updated.helper.app_commit;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.repository.AppCommitRepository;
import com.cloudslip.pipeline.updated.service.AppCommitPipelineStepService;
import com.cloudslip.pipeline.updated.service.AppCommitStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeleteAllAppCommitHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppCommitRepository appCommitRepository;

    @Autowired
    AppCommitPipelineStepService appCommitPipelineStepService;

    @Autowired
    AppCommitStateService appCommitStateService;

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
        List<AppCommit> appCommitList = appCommitRepository.findAllByApplicationIdAndStatus(input.getId(), Status.V);
        for (AppCommit appCommit: appCommitList) {
            // Delete All App Commit Pipeline Steps For Each App Commit
            ResponseDTO appCommitPipelineStepResponse = appCommitPipelineStepService.deleteAllAppCommitPipelineStep(new GetObjectInputDTO(appCommit.getObjectId()), requester, actionId);
            if (appCommitPipelineStepResponse.getStatus() == ResponseStatus.error) {
                output.generateErrorResponse(appCommitPipelineStepResponse.getMessage());
                throw new ApiErrorException(this.getClass().getName());
            }

            // Delete Each App Commit State
            ResponseDTO appCommitStateResponse = appCommitStateService.deleteAppCommitState(new GetObjectInputDTO(appCommit.getObjectId()), requester, actionId);
            if (appCommitStateResponse.getStatus() == ResponseStatus.error) {
                output.generateErrorResponse(appCommitStateResponse.getMessage());
                throw new ApiErrorException(this.getClass().getName());
            }
            appCommit.setStatus(Status.D);
            appCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appCommit.setUpdatedBy(requester.getUsername());
            appCommit.setLastUpdateActionId(actionId);
            appCommitRepository.save(appCommit);
        }
        output.generateSuccessResponse(null,  "All application Commits Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
