package com.cloudslip.pipeline.updated.helper.app_pipe_line_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.CreateAppPipelineResponseDTO;
import com.cloudslip.pipeline.updated.dto.app_pipeline_step.CreateAppPipelineStepDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.PipelineStepType;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CreateAppPipeLineStepListHelper extends AbstractHelper {

    private CreateAppPipelineStepDTO input;
    private ResponseDTO output = new ResponseDTO();

    private List<ObjectId> selectedClusterId;

    private List<AppPipelineStep> selectedAppPipeLineStepList;
    private List<AppPipelineStep> unselectedAppPipeLineStepList;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppPipelineStepDTO) input;
        this.setOutput(output);
        this.selectedClusterId = new ArrayList<>();
        this.selectedAppPipeLineStepList = new ArrayList<>();
        this.unselectedAppPipeLineStepList = new ArrayList<>();
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
        this.addPipeLineSteps();
        this.deleteUnselectedPipeLineSteps();
        this.selectedAppPipeLineStepList = appPipelineStepRepository.findAllByAppEnvironmentIdAndStatus(input.getAppEnvironment().getObjectId(), Status.V);
        output.generateSuccessResponse(new CreateAppPipelineResponseDTO(selectedAppPipeLineStepList, this.unselectedAppPipeLineStepList), "Pipe Line Step For Each Vpc Has Been Created");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void addPipeLineSteps() {
        for (AppVpc appVpc : input.getAppVpcList()) {
            this.selectedClusterId.add(appVpc.getObjectId());
            AppPipelineStep appPipelineStep;
            Optional<AppPipelineStep> existingAppPipelineStep = appPipelineStepRepository.findByAppVpcIdAndAppEnvironmentIdAndStatus(appVpc.getObjectId(), input.getAppEnvironment().getObjectId(), Status.V);
            if (!existingAppPipelineStep.isPresent()) {
                appPipelineStep = new AppPipelineStep();
                appPipelineStep.setName("Deploy on '"+ appVpc.getVpc().getName()+"'");
                appPipelineStep.setAppVpcId(appVpc.getObjectId());
                appPipelineStep.setAppEnvironmentId(input.getAppEnvironment().getObjectId());
                appPipelineStep.setStepType(PipelineStepType.DEPLOY);
                appPipelineStep.setCreatedBy(requester.getUsername());
                appPipelineStep.setCreateDate(String.valueOf(LocalDateTime.now()));
                appPipelineStep.setCreateActionId(actionId);
                appPipelineStep.setStatus(Status.V);
            } else {
                appPipelineStep = existingAppPipelineStep.get();
                appPipelineStep.setUpdatedBy(requester.getUsername());
                appPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
                appPipelineStep.setLastUpdateActionId(actionId);
            }
            appPipelineStepRepository.save(appPipelineStep);
        }
    }

    private void deleteUnselectedPipeLineSteps() {
        List<AppPipelineStep> unselectedPipeLineStep = appPipelineStepRepository.findByAppVpcIdNotInAndAppEnvironmentIdAndStatus(this.selectedClusterId, input.getAppEnvironment().getObjectId(), Status.V);
        for (AppPipelineStep appPipelineStep: unselectedPipeLineStep) {
            if (appPipelineStep.getStepType() != PipelineStepType.BUILD) {
                appPipelineStep.setUpdatedBy(requester.getUsername());
                appPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
                appPipelineStep.setLastUpdateActionId(actionId);
                appPipelineStep.setStatus(Status.D);
                appPipelineStep.setSuccessors(null);
                appPipelineStep = appPipelineStepRepository.save(appPipelineStep);
                this.unselectedAppPipeLineStepList.add(appPipelineStep);
                this.deleteAppPipelineStepFromParent(appPipelineStep);
            }
        }
    }

    /*
        Delete App Pipe line Step As Successor From Other App Pipeline Step
     */
    private void deleteAppPipelineStepFromParent(AppPipelineStep unselectedAppPipelineStep) {
        List<AppPipelineStep> parentAppPipelineStepList = appPipelineStepRepository.findAllBySuccessorsAppPipelineStepIdAndStatus(unselectedAppPipelineStep.getObjectId(), Status.V);
        if (!parentAppPipelineStepList.isEmpty()) {
            for (AppPipelineStep parentPipelineStep : parentAppPipelineStepList) {
                parentPipelineStep.getSuccessors().removeIf(successorPipelineStep -> successorPipelineStep.getAppPipelineStep().getObjectId().toString().equals(unselectedAppPipelineStep.getObjectId().toString()));
                appPipelineStepRepository.save(parentPipelineStep);
                Optional<AppEnvironment> parentAppEnvironment = appEnvironmentRepository.findByIdAndStatus(parentPipelineStep.getAppEnvironmentObjectId(), Status.V);
                if (parentAppEnvironment.isPresent() && !parentAppEnvironment.get().getId().equals(input.getAppEnvironment().getId())) {
                    int pipelineIndex = parentAppEnvironment.get().getAppPipelineStepIndex(parentPipelineStep);
                    List<AppPipelineStep> pipelineStepList =  parentAppEnvironment.get().getAppPipelineStepList();
                    pipelineStepList.set(pipelineIndex, parentPipelineStep);
                    parentAppEnvironment.get().setAppPipelineStepList(pipelineStepList);
                    appEnvironmentRepository.save(parentAppEnvironment.get());
                }
            }
        }
    }
}
