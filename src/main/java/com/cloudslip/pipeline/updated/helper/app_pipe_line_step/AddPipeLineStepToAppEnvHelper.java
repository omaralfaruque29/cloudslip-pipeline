package com.cloudslip.pipeline.updated.helper.app_pipe_line_step;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto_helper.AppPipelineStepHelper;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.dummy.SuccessorPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.AppEnvironmentService;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.EnumUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddPipeLineStepToAppEnvHelper extends AbstractHelper {

    private AddCustomPipelineStepDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppEnvironment> appEnvironment;
    private Optional<Application> application;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    @Autowired
    AppEnvironmentService appEnvironmentService;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddCustomPipelineStepDTO) input;
        this.setOutput(output);
        appEnvironment = null;
        application = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if (input.getName() == null || input.getName().equals("")) {
            output.generateErrorResponse("App Pipeline Step Name is Required!");
            throw new ApiErrorException(this.getClass().getName());
        } else  if (input.getAppEnvironmentId() == null) {
            output.generateErrorResponse("application Environment Id is Required!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appEnvironment = appEnvironmentRepository.findByIdAndStatus(input.getAppEnvironmentId(), Status.V);
        if (!appEnvironment.isPresent()) {
            output.generateErrorResponse("application Environment Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        if (!checkAuthority(appEnvironment.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
        application = applicationRepository.findByIdAndStatus(appEnvironment.get().getApplicationObjectId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        List<SuccessorPipelineStep> successorPipelineSteps = null;
        if (input.getSuccessors() != null) {
            successorPipelineSteps = new ArrayList<>();
            for (AppPipelineStepHelper helper : input.getSuccessors()) {
                if (helper.getId() == null) {
                    output.generateErrorResponse("Successor App Pipeline Step Id is Required!");
                    throw new ApiErrorException(this.getClass().getName());
                }
                Optional<AppPipelineStep> pipelineStep = appPipelineStepRepository.findByIdAndStatus(helper.getId(), Status.V);
                if (!pipelineStep.isPresent()) {
                    output.generateErrorResponse(String.format("application Pipeline Step Not Found with Id- %s!", helper.getId().toHexString()));
                    throw new ApiErrorException(this.getClass().getName());
                }
                this.checkTriggerMode(helper);
                this.checkSuccessorPipelineAuthority(appEnvironment.get(), pipelineStep.get(), TriggerMode.valueOf(helper.getTriggerMode()));
                if (!containsDuplicateSuccessors(successorPipelineSteps, pipelineStep.get().getObjectId(), TriggerMode.valueOf(helper.getTriggerMode())))  {
                    successorPipelineSteps.add(new SuccessorPipelineStep(pipelineStep.get(), TriggerMode.valueOf(helper.getTriggerMode())));
                }
            }
        }
        AppPipelineStep appPipelineStep = createCustomPipelineStep(successorPipelineSteps);
        ResponseDTO appEnvironmentResponse = appEnvironmentService.addPipelineStepToList(appPipelineStep, appEnvironment.get().getObjectId(), requester, actionId);
        output.generateSuccessResponse(appEnvironmentResponse.getData(), appEnvironmentResponse.getMessage());
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        Check Validity Of Provided Trigger Mode
     */
    private void checkTriggerMode(AppPipelineStepHelper appPipelineStepHelper) {
        if (appPipelineStepHelper.getTriggerMode() == null || appPipelineStepHelper.getTriggerMode().equals("")) {
            output.generateErrorResponse("Successor Pipeline Step TriggerMode is Required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!EnumUtils.isValidEnum(TriggerMode.class, appPipelineStepHelper.getTriggerMode())) {
            output.generateErrorResponse("Invalid Successor Pipeline Step TriggerMode!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    /*
        Checking If Same Successor AppPipeline Step With Same TriggerMode is given more than one time
     */
    private boolean containsDuplicateSuccessors(List<SuccessorPipelineStep> successorPipelineSteps, ObjectId pipeLineStepId, TriggerMode triggerMode) {
        for (SuccessorPipelineStep successorPipelineStep : successorPipelineSteps) {
            if (successorPipelineStep.getAppPipelineStep().getObjectId().toString().equals(pipeLineStepId.toString())
                    && successorPipelineStep.getTriggerMode() == triggerMode) {
                return true;
            } else if (successorPipelineStep.getAppPipelineStep().getObjectId().toString().equals(pipeLineStepId.toString())
                    && successorPipelineStep.getTriggerMode() != triggerMode) {
                output.generateErrorResponse("Cannot Have Duplicate Pipeline Step with Different Trigger Mode As Successors!");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
        return false;
    }

    /*
        Check If User Has Authority To Access The AppPipeline Step Using AppPipeline Step's AppEnvironment
     */
    private boolean checkAuthority(AppEnvironment appEnvironment) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return requester.getCompanyId().toString().equals(appEnvironment.getCompanyId());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && !requester.hasAuthority(Authority.ROLE_ADMIN)) {
            Optional<Application> application = applicationRepository.findByIdAndStatus(appEnvironment.getApplicationObjectId(), Status.V);
            return application.get().getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }

    /*
        Checking Requested AppPipeline Whether Its Valid As A Successor Of Provided AppPipeline Step
     */
    private void checkSuccessorPipelineAuthority(AppEnvironment appEnvironment, AppPipelineStep successorAppPipelineStep, TriggerMode successorTriggerMode) {
        Optional<AppEnvironment> pipeLineAppEnv = appEnvironmentRepository.findByIdAndStatus(successorAppPipelineStep.getAppEnvironmentObjectId(), Status.V);
        if (!pipeLineAppEnv.isPresent()) {
            output.generateErrorResponse(String.format("application Environment Not Found For App Pipeline Step - '%s'!", successorAppPipelineStep.getObjectId()));
            throw new ApiErrorException(this.getClass().getName());
        } else if (!checkAuthority(pipeLineAppEnv.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!pipeLineAppEnv.get().getApplicationId().equals(appEnvironment.getApplicationId())) {
            output.generateErrorResponse(String.format("Access Denied for Pipeline Step - %s!", successorAppPipelineStep.getObjectId()));
            throw new ApiErrorException(this.getClass().getName());
        } else if (pipeLineAppEnv.get().getEnvironment().getOrderNo() < appEnvironment.getEnvironment().getOrderNo()) {
            output.generateErrorResponse(String.format("'%s' Environment of Pipeline Step '%s' Cannot Be Added As A Successor of '%s' Environment",
                    pipeLineAppEnv.get().getEnvironment().getName(), successorAppPipelineStep.getObjectId().toHexString(),
                    appEnvironment.getEnvironment().getName()));
            throw new ApiErrorException(this.getClass().getName());
        } else if (successorAppPipelineStep.getStepType() == PipelineStepType.BUILD) {
            output.generateErrorResponse("Cannot Add Build Step As A Successor!");
            throw new ApiErrorException(this.getClass().getName());
        }  else if (appEnvironment.getObjectId().toString().equals(pipeLineAppEnv.get().getObjectId().toString())) {
            output.generateErrorResponse("Cannot Add Previous Pipeline Step As Successor For Same Environment!");
            throw new ApiErrorException(this.getClass().getName());
        }

        /*
            If Successor AppPipeline's Trigger Mode is Automatic and Is Successor Of Other AppPipeline Step,
            Then the Successor AppPipeline step cannot be added As A Successor Of Provided AppPipeline Step
         */
        if (successorTriggerMode == TriggerMode.AUTOMATIC) {
            List<AppPipelineStep> existingAppPipelineSteps = appPipelineStepRepository.findAllBySuccessorsAppPipelineStepIdAndStatus(successorAppPipelineStep.getObjectId(), Status.V);
            if (!existingAppPipelineSteps.isEmpty()) {
                output.generateErrorResponse(String.format("Cannot Trigger Auto For App Pipeline Step '%s' which is a successor of other App Pipeline Steps", successorAppPipelineStep.getName()));
                throw new ApiErrorException(this.getClass().getName());
            }
        }

        // Checking For Existing AppPipeline Step Which Have A Successor Of The Provided AppPipeline Step With Auto Trigger
        Optional<AppPipelineStep> existsAsSuccessorWithAutoTrigger = appPipelineStepRepository.findBySuccessorsAppPipelineStepIdAndTriggerModeAndStatus(successorAppPipelineStep.getObjectId(), TriggerMode.AUTOMATIC, Status.V);
        if (existsAsSuccessorWithAutoTrigger.isPresent()) {
            output.generateErrorResponse(String.format("App Pipeline Step '%s' trigger auto by '%s'", successorAppPipelineStep.getName(), existsAsSuccessorWithAutoTrigger.get().getName()));
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private AppPipelineStep createCustomPipelineStep(List<SuccessorPipelineStep> successors) {
        AppPipelineStep appPipelineStep = new AppPipelineStep();
        appPipelineStep.setName(input.getName());
        appPipelineStep.setAppEnvironmentId(appEnvironment.get().getObjectId());
        appPipelineStep.setStepType(PipelineStepType.CUSTOM);
        appPipelineStep.setJenkinsUrl(input.getJenkinsUrl());
        appPipelineStep.setJenkinsApiToken(input.getJenkinsApiToken());
        appPipelineStep.setSuccessors(successors);
        appPipelineStep.setCreatedBy(requester.getUsername());
        appPipelineStep.setCreateDate(String.valueOf(LocalDateTime.now()));
        appPipelineStep.setCreateActionId(actionId);
        return appPipelineStepRepository.save(appPipelineStep);
    }
}
