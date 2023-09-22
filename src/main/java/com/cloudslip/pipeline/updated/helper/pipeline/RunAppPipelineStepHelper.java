package com.cloudslip.pipeline.updated.helper.pipeline;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.dummy.SuccessorPipelineStep;
import com.cloudslip.pipeline.updated.model.universal.Company;
import com.cloudslip.pipeline.updated.model.universal.User;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class RunAppPipelineStepHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(TriggerPipelineBuildStepHelper.class);

    private RunAppPipelineStepInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<AppCommitPipelineStep> appCommitPipelineStep;
    private Optional<AppCommit> appCommit;
    private Optional<AppVpc> appVpc;
    private Optional<Application> application;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private AppVpcRepository appVpcRepository;

    @Autowired
    private AppCommitRepository appCommitRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private TriggerPipelineBuildStepHelper triggerPipelineBuildStepHelper;

    @Autowired
    private DeployInAppVpcHelper deployInAppVpcHelper;

    @Autowired
    private RunAppPipelineStepHelper runAppPipelineStepHelper;


    public void init(BaseInput input, Object... extraParams) {
        this.input = (RunAppPipelineStepInputDTO)input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if ((requester == null) || (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) && !requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_DEV) && !requester.hasAuthority(Authority.ROLE_OPS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if(input.getAppCommitPipelineStepId() == null) {
            output.generateErrorResponse("No App Commit Pipeline Step Id  provided!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }

        appCommitPipelineStep = appCommitPipelineStepRepository.findById(input.getAppCommitPipelineStepId());
        if(!appCommitPipelineStep.isPresent() || appCommitPipelineStep.get().isDeleted()) {
            output.generateErrorResponse("App Commit Pipeline Step not found!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }

        appCommit = appCommitRepository.findById(appCommitPipelineStep.get().getAppCommitObjectId());
        if(!appCommit.isPresent() || appCommit.get().isDeleted()) {
            output.generateErrorResponse("App Commit not found!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }

        if(appCommitPipelineStep.get().getAppPipelineStep().getStepType() == PipelineStepType.DEPLOY) {
            appVpc = appVpcRepository.findById(appCommitPipelineStep.get().getAppPipelineStep().getAppVpcObjectId());
            if(!appVpc.isPresent() || appVpc.get().isDeleted()) {
                output.generateErrorResponse("App Vpc not found!");
                throw new ApiErrorException(output.getMessage(), this.getClass().getName());
            }
        }

        application = applicationRepository.findByIdAndStatus(appCommit.get().getApplicationObjectId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("Application not found!");
            throw new ApiErrorException(output.getMessage(), this.getClass().getName());
        }
    }


    protected void doPerform() {
        String companyId = application.get().getTeam().getCompanyId();

        HttpHeaders headers = Utils.generateHttpHeaders(requester);
        HttpEntity<String> request = new HttpEntity<>("parameters", headers);
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(applicationProperties.getUserManagementServiceBaseUrl() + "api/company/" + companyId, HttpMethod.GET, request, ResponseDTO.class);
        Company requesterCompany = null;
        if(response.hasBody() && response.getBody().getStatus() == ResponseStatus.success) {
            requesterCompany = objectMapper.convertValue(response.getBody().getData(), Company.class);
        }
        if(appCommitPipelineStep.get().getAppPipelineStep().getStepType() == PipelineStepType.BUILD) {
            buildApp();
        } else if(appCommitPipelineStep.get().getAppPipelineStep().getStepType() == PipelineStepType.DEPLOY) {
            deployInAppVpc(requesterCompany);
        } else if(appCommitPipelineStep.get().getAppPipelineStep().getStepType() == PipelineStepType.CUSTOM) {
            output.generateErrorResponse("Running custom pipeline step has not been implemented yet!");
        } else {
            output.generateErrorResponse("Unknown error");
        }
    }

    private void buildApp() {
        TriggerPipelineFromStartInputDTO input = new TriggerPipelineFromStartInputDTO();
        input.setApplicationId(application.get().getObjectId());
        input.setCommitId(appCommit.get().getGitCommitId());
        ResponseDTO triggerPipelineBuildStepResponse = (ResponseDTO) triggerPipelineBuildStepHelper.execute(input, requester, actionId);
        output.generateSuccessResponse(null, triggerPipelineBuildStepResponse.getMessage());

        //startTriggeringSuccessorStepsThread(appCommitPipelineStep.get(), requester, actionId);
    }

    private void deployInAppVpc(Company requesterCompany) {
        DeployInAppVpcInputDTO deployInAppVpcInput = null;
        if(appVpc.get().getMainCommit() == null) {
            deployInAppVpcInput = new DeployInAppVpcInputDTO(appCommitPipelineStep.get(), appCommit.get(), appVpc.get(), application.get(), requesterCompany.getGitInfo(), false);

        } else if(appVpc.get().getMainCommit() != null && !appCommit.get().getId().equals(appVpc.get().getMainCommit().getAppCommit().getId()) && appVpc.get().isCanaryDeploymentEnabled() && !input.isForceRun()) {
            output.generateWarningResponse(new TypeValueVO("DEPLOYMENT_EXIST_FOR_OTHER_COMMIT", "TRUE"), String.format("Commit '%s' is already deployed in this VPC for this Environment. Do you want to apply Rollout Update or deploy as Canary Deployment?", appVpc.get().getMainCommit().getAppCommit().getGitCommitId()));
            return;
        } else {
            deployInAppVpcInput = new DeployInAppVpcInputDTO(appCommitPipelineStep.get(), appCommit.get(), appVpc.get(), application.get(), requesterCompany.getGitInfo(), appVpc.get().isCanaryDeploymentEnabled() ? input.isCanaryDeployment() : false);
        }

        ResponseDTO deployInAppVpcResponse = (ResponseDTO) deployInAppVpcHelper.execute(deployInAppVpcInput, requester, actionId);
        output.generateSuccessResponse(deployInAppVpcResponse.getData(), deployInAppVpcResponse.getMessage());

        //startTriggeringSuccessorStepsThread(appCommitPipelineStep.get(), requester, actionId);
    }

    private void startTriggeringSuccessorStepsThread(AppCommitPipelineStep appCommitPipelineStep, User requester, ObjectId actionId) {

        if(appCommitPipelineStep.getAppPipelineStep().getSuccessors().size() > 0) {
            Thread thread = new Thread() {
                public void run() {
                try {
                    for (SuccessorPipelineStep successorPipelineStep : appCommitPipelineStep.getAppPipelineStep().getSuccessors()) {
                        if (successorPipelineStep.getTriggerMode().equals(TriggerMode.AUTOMATIC)) {
                            Optional<AppCommitPipelineStep> successorAppCommitPipelineStep = appCommitPipelineStepRepository.findByAppCommitIdAndAppPipelineStep_IdAndStatus(appCommitPipelineStep.getAppCommitObjectId(), successorPipelineStep.getAppPipelineStep().getObjectId(), Status.V);

                            if (successorAppCommitPipelineStep.isPresent() && successorAppCommitPipelineStep.get().isEnabled()) {
                                RunAppPipelineStepInputDTO input = new RunAppPipelineStepInputDTO(successorAppCommitPipelineStep.get().getObjectId(), false, true);
                                runAppPipelineStepHelper.execute(input, requester, actionId);
                            }
                        }
                    }

                } catch (Exception ex) {
                    log.error("Trigger Success Steps Thread: " + ex.getMessage());
                }
                }
            };
            thread.start();
        }
    }


    protected void postPerformCheck() {

    }

    protected void doRollback() {

    }
}
