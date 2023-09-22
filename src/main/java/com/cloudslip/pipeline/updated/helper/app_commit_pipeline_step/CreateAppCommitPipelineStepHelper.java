package com.cloudslip.pipeline.updated.helper.app_commit_pipeline_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.CreateAppCommitPipelineStepDTO;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateAppCommitPipelineStepHelper extends AbstractHelper {

    private CreateAppCommitPipelineStepDTO input;
    private ResponseDTO output = new ResponseDTO();

    private List<AppCommitPipelineStep> appCommitPipelineStepList;

    @Autowired
    AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppCommitPipelineStepDTO) input;
        this.setOutput(output);
        appCommitPipelineStepList = new ArrayList<>();
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
        List<AppPipelineStep> pipelineStepList = appPipelineStepRepository.findAllByAppEnvironmentIdAndStatus(input.getAppEnvironment().getObjectId(), Status.V);
        for (AppPipelineStep pipelineStep : pipelineStepList) {
            AppCommitPipelineStep appCommitPipelineStep = new AppCommitPipelineStep();
            appCommitPipelineStep.setAppCommitId(input.getCommitId());
            appCommitPipelineStep.setAppPipelineStep(pipelineStep);

            /*
                Check if pipeline step is a successor of any other pipeline step.
                if the pipeline step has any parent pipeline step then it will be disabled
                if the pipeline step is build type then it is enabled
             */
            List<AppPipelineStep> parentPipelineSteps = appPipelineStepRepository.findAllBySuccessorsAppPipelineStepIdAndStatus(pipelineStep.getObjectId(), Status.V);
            if (pipelineStep.getStepType() == PipelineStepType.BUILD || parentPipelineSteps.isEmpty()) {
                appCommitPipelineStep.setEnabled(true);
            } else {
                appCommitPipelineStep.setEnabled(false);
            }
            appCommitPipelineStep.setCreatedBy(requester.getUsername());
            appCommitPipelineStep.setCreateDate(String.valueOf(LocalDateTime.now()));
            appCommitPipelineStep.setCreateActionId(actionId);
            appCommitPipelineStep.setStatus(Status.V);
            appCommitPipelineStepList.add(appCommitPipelineStep);
        }
        output.generateSuccessResponse(appCommitPipelineStepRepository.saveAll(appCommitPipelineStepList),
                "App Commit Pipeline Step Successfully Created");

    }



    protected void postPerformCheck() {
    }


    protected void doRollback() {

    }
}
