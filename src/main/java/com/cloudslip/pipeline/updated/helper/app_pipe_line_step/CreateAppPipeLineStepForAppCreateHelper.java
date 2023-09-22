package com.cloudslip.pipeline.updated.helper.app_pipe_line_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.app_pipeline_step.CreateAppPipelineStepDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.PipelineStepType;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreateAppPipeLineStepForAppCreateHelper extends AbstractHelper {

    private CreateAppPipelineStepDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppPipelineStepDTO) input;
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
        AppPipelineStep appPipelineStep = new AppPipelineStep();
        appPipelineStep.setName("Build");
        appPipelineStep.setAppEnvironmentId(input.getAppEnvironment().getObjectId());
        appPipelineStep.setStepType(PipelineStepType.BUILD);
        appPipelineStep.setCreatedBy(requester.getUsername());
        appPipelineStep.setCreateDate(String.valueOf(LocalDateTime.now()));
        appPipelineStep.setCreateActionId(actionId);
        appPipelineStep.setStatus(Status.V);
        appPipelineStepRepository.save(appPipelineStep);
        output.generateSuccessResponse(appPipelineStepRepository.findAllByAppEnvironmentIdAndStatus(input.getAppEnvironment().getObjectId(), Status.V),
                "Pipe Line Step For Each Vpc Has Been Created");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
