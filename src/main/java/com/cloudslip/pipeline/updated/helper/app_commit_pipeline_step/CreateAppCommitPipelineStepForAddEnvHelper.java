package com.cloudslip.pipeline.updated.helper.app_commit_pipeline_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.CreateAppCommitPipelineStepForAddEnvDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.PipelineStepType;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreateAppCommitPipelineStepForAddEnvHelper extends AbstractHelper {

    private CreateAppCommitPipelineStepForAddEnvDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppCommitPipelineStepForAddEnvDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
    }


    protected void doPerform() {
        AppCommitPipelineStep appCommitPipelineStep = new AppCommitPipelineStep();
        appCommitPipelineStep.setAppCommitId(input.getCommitId());
        appCommitPipelineStep.setAppPipelineStep(input.getAppPipelineStep());

        /*
            Check if pipeline step is a successor of any other pipeline step.
            if the pipeline step has any parent pipeline step then it will be disabled
            if the pipeline step is build type then it is enabled
         */
        List<AppPipelineStep> parentPipelineSteps = appPipelineStepRepository.findAllBySuccessorsAppPipelineStepIdAndStatus(input.getAppPipelineStep().getObjectId(), Status.V);
        if (input.getAppPipelineStep().getStepType() == PipelineStepType.BUILD || parentPipelineSteps.isEmpty()) {
            appCommitPipelineStep.setEnabled(true);
        } else {
            appCommitPipelineStep.setEnabled(false);
        }
        appCommitPipelineStep.setCreatedBy(requester.getUsername());
        appCommitPipelineStep.setCreateDate(String.valueOf(LocalDateTime.now()));
        appCommitPipelineStep.setCreateActionId(actionId);
        appCommitPipelineStep.setStatus(Status.V);
        output.generateSuccessResponse(appCommitPipelineStepRepository.save(appCommitPipelineStep),
                "App Commit Pipeline Step Successfully Created");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
