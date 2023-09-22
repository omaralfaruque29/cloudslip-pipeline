package com.cloudslip.pipeline.updated.helper.app_environment;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.app_environment.AddDevelopmentEnvironmentDTO;
import com.cloudslip.pipeline.updated.dto.app_pipeline_step.CreateAppPipelineStepDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.service.AppVpcService;
import com.cloudslip.pipeline.updated.service.AppPipelineStepService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddDevelopmentEnvironmentHelper extends AbstractHelper {

    private AddDevelopmentEnvironmentDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppVpcService appVpcService;

    @Autowired
    AppPipelineStepService appPipelineStepService;

    @Autowired
    private ObjectMapper objectMapper;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddDevelopmentEnvironmentDTO) input;
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
        AppEnvironment appEnvironment = new AppEnvironment();
        appEnvironment.setApplicationId(input.getApplication().getObjectId());
        appEnvironment.setCompanyId(input.getCompany().getObjectId());
        appEnvironment.setEnvironment(input.getEnvironmentOption());
        appEnvironment.setCreatedBy(requester.getUsername());
        appEnvironment.setCreateDate(String.valueOf(LocalDateTime.now()));
        appEnvironment.setCreateActionId(actionId);
        appEnvironment.setStatus(Status.V);
        appEnvironment = appEnvironmentRepository.save(appEnvironment);
        this.setPipeLineStep(appEnvironment);
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void setPipeLineStep(AppEnvironment appEnvironment) {
        ResponseDTO appPipelineStepListResponse = appPipelineStepService.createAppPipeLineStepForAppCreate(new CreateAppPipelineStepDTO(appEnvironment, null), requester, actionId);
        appEnvironment.setAppPipelineStepList(objectMapper.convertValue(appPipelineStepListResponse.getData(), new TypeReference<List<AppPipelineStep>>() { }));
        output.generateSuccessResponse(appEnvironmentRepository.save(appEnvironment), "Development Environment successfully Added to application");
    }
}
