package com.cloudslip.pipeline.updated.helper.app_commit_pipeline_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeleteAllAppCommitPipelineStepHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppCommitPipelineStepRepository appCommitPipelineStepRepository;

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
        List<AppCommitPipelineStep> appCommitPipelineStepList = appCommitPipelineStepRepository.findAllByAppCommitIdAndStatus(input.getId(), Status.V);
        for (AppCommitPipelineStep appCommitPipelineStep: appCommitPipelineStepList) {
            appCommitPipelineStep.setStatus(Status.D);
            appCommitPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appCommitPipelineStep.setUpdatedBy(requester.getUsername());
            appCommitPipelineStep.setLastUpdateActionId(actionId);
            appCommitPipelineStepRepository.save(appCommitPipelineStep);
        }
        output.generateSuccessResponse(null,  "All App Commit Pipeline Step Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
