package com.cloudslip.pipeline.updated.helper.app_log;

import com.cloudslip.pipeline.model.jenkins.JenkinsConsoleLog;
import com.cloudslip.pipeline.service.JenkinsService;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetLogInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.helper.application.CreateApplicationTemplateHelper;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.model.AppCommitPipelineStep;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppCommitPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.AppCommitRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;

@Service
public class LogFetchHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(CreateApplicationTemplateHelper.class);

    @Autowired
    private JenkinsService jenkinsService;

    @Autowired
    private AppCommitPipelineStepRepository appCommitPipelineStepRepository;

    @Autowired
    private AppCommitRepository appCommitRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private GetLogInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<Application> application;
    private Optional<AppCommitPipelineStep> appCommitPipelineStep;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetLogInputDTO) input;
        this.setOutput(output);
    }

    protected void checkPermission() {
        if (requester == null || requester.hasAnyAuthority(Authority.ANONYMOUS, Authority.ROLE_AGENT_SERVICE, Authority.ROLE_GIT_AGENT)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void checkValidity() {
        if(input.getAppCommitPipelineStepId() == null || input.getFetchType() == null){
            output.generateErrorResponse("AppCommitPipeLineStepId and fetch type required");
            throw new ApiErrorException(this.getClass().getName());
        }
        appCommitPipelineStep = appCommitPipelineStepRepository.findByIdAndStatus(input.getAppCommitPipelineStepId(), Status.V);
        if(!appCommitPipelineStep.isPresent()) {
            output.generateErrorResponse("AppCommitPipelineStep not found");
            throw new ApiErrorException(this.getClass().getName());
        } else if(appCommitPipelineStep.get().getJenkinsBuildId() == null) {
            output.generateErrorResponse("Build Id found null!");
            throw new ApiErrorException(this.getClass().getName());
        }
        Optional<AppCommit> appCommit = appCommitRepository.findByIdAndStatus(appCommitPipelineStep.get().getAppCommitObjectId(), Status.V);
        application = applicationRepository.findByIdAndStatus(appCommit.get().getApplicationObjectId(), Status.V);
        if(requester.hasAuthority(Authority.ROLE_ADMIN)) {
            if(!requester.getCompanyIdAsString().equals(application.get().getTeam().getCompanyId())) {
                output.generateErrorResponse("This application does not belong to requester");
                throw new ApiErrorException(this.getClass().getName());
            }
        } else if(requester.hasAnyAuthority(Authority.ROLE_DEV, Authority.ROLE_OPS)) {
            if(requester.getTeamIdList() != null && requester.getTeamIdList().size() > 0){
                if(!requester.getTeamIdList().contains(application.get().getTeam().getObjectId())){
                    output.generateErrorResponse("This application does not belong to requester");
                    throw new ApiErrorException(this.getClass().getName());
                }
            } else {
                output.generateErrorResponse("Requester has no team");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
    }

    protected void doPerform() {
        JenkinsConsoleLog jenkinsConsoleLog = null;
        try {
            jenkinsConsoleLog = jenkinsService.getConsoleLogFromSpecificIndex(application.get().getAppBuildJobNameInJenkins(), Integer.parseInt(appCommitPipelineStep.get().getJenkinsBuildId()), 0);
            output.generateSuccessResponse(jenkinsConsoleLog.getLogText());
        } catch (Exception e) {
            log.error(e.getMessage());
            output.generateErrorResponse(e.getMessage());
        }
    }


    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
