package com.cloudslip.pipeline.updated.helper.app_pipe_line_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeleteAllAppPipelineStepHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

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
        List<AppPipelineStep> appPipelineStepList = appPipelineStepRepository.findAllByAppEnvironmentIdAndStatus(input.getId(), Status.V);
        for (AppPipelineStep appPipelineStep: appPipelineStepList) {
            appPipelineStep.setStatus(Status.D);
            appPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appPipelineStep.setUpdatedBy(requester.getUsername());
            appPipelineStep.setLastUpdateActionId(actionId);
            appPipelineStepRepository.save(appPipelineStep);
        }
        output.generateSuccessResponse(null,  " application Pipeline Steps Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
