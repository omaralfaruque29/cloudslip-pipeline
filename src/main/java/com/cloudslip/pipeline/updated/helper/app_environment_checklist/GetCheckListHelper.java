package com.cloudslip.pipeline.updated.helper.app_environment_checklist;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetChecklistDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppEnvironmentChecklist;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.universal.Team;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentChecklistRepository;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GetCheckListHelper extends AbstractHelper {

    private GetChecklistDTO input;
    private ResponseDTO output = new ResponseDTO();

    private  Optional<Application> application;
    private Optional<AppEnvironment> appEnvironment;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppEnvironmentChecklistRepository appEnvironmentChecklistRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetChecklistDTO) input;
        this.setOutput(output);
        application = null;
        appEnvironment = null;
    }


    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
      application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && requester.hasAuthority(Authority.ROLE_ADMIN)
                && !application.get().getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }

        appEnvironment = appEnvironmentRepository.findByIdAndStatus(input.getAppEnvId(), Status.V);
        if(!appEnvironment.isPresent()) {
            output.generateErrorResponse("application Environment Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!appEnvironment.get().getApplicationId().equals(application.get().getObjectId().toString())) {
            output.generateErrorResponse(String.format("No application Environment Found Under application - %s",
                    application.get().getName()));
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        Optional<AppEnvironmentChecklist> appEnvironmentChecklist = appEnvironmentChecklistRepository.findByApplicationIdAndAppEnvironmentIdAndStatus(application.get().getObjectId(),
                appEnvironment.get().getObjectId(), Status.V);
        output.generateSuccessResponse(appEnvironmentChecklist, "application Environment Checklist Response");
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
