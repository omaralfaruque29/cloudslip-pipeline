package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.application.CreateApplicationResponseDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.application.UpdateApplicationDTO;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UpdateApplicationHelper extends AbstractHelper {

    private UpdateApplicationDTO input;
    private ResponseDTO output = new ResponseDTO();

    private ResponseDTO response = new ResponseDTO();
    private CreateApplicationResponseDTO updateApplicationResponse;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ApplicationRepository applicationRepository;

    Optional<Application> application;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (UpdateApplicationDTO) input;
        this.setOutput(output);
        this.response = (ResponseDTO) extraParams[0];
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.updateApplicationResponse = objectMapper.convertValue(response.getData(), new TypeReference<CreateApplicationResponseDTO>() { });
        this.application = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.checkRequiredFields();
        this.checkEnums();
        this.validateApplication();
    }


    protected void doPerform() {
        application.get().setName(this.input.getName().trim().replaceAll(" +", " "));
        application.get().setTeam(this.updateApplicationResponse.getTeam());
        application.get().setCreationType(ApplicationCreationType.valueOf(input.getAppCreationType()));
        application.get().setType(ApplicationType.valueOf(input.getApplicationType()));
        if (ApplicationType.valueOf(input.getApplicationType()) == ApplicationType.SPRING_BOOT) {
            application.get().setPackageName(input.getPackageName().trim());
            application.get().setBuildType(ApplicationBuildType.valueOf(input.getApplicationBuildType()));
        }
        application.get().setUpdatedBy(requester.getUsername());
        application.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        application.get().setCreateActionId(actionId);

        applicationRepository.save(application.get());
        output.generateSuccessResponse(application.get(), String.format("application '%s' has successfully updated", application.get().getName()));
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void checkRequiredFields() {
        if (input.getApplicationId() == null) {
            output.generateErrorResponse("application id is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (input.getName() == null || input.getName().equals("")) {
            output.generateErrorResponse("application name is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (input.getAppCreationType() == null || input.getAppCreationType().equals("")) {
            output.generateErrorResponse("application creation type is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (input.getApplicationType() == null || input.getApplicationType().equals("")) {
            output.generateErrorResponse("application type is required!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void checkEnums() {
        if (!EnumUtils.isValidEnum(ApplicationCreationType.class, input.getAppCreationType())) {
            output.generateErrorResponse("Invalid application creation type!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!EnumUtils.isValidEnum(ApplicationType.class, input.getApplicationType())) {
            output.generateErrorResponse("Invalid application type!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (ApplicationType.valueOf(input.getApplicationType()) == ApplicationType.SPRING_BOOT) {
            this.checkPackageAndBuildType();
        }
    }

    /*
        application Build Type is required if user chooses Spring Boot App
    */
    private void checkPackageAndBuildType() {
        if (input.getPackageName() == null || input.getPackageName().equals("")) {
            output.generateErrorResponse("application package name is required for Spring Boot application!");
            throw new ApiErrorException(this.getClass().getName());
        }
        this.checkPackagePattern(input.getPackageName().trim()); // check package pattern
        if (input.getApplicationBuildType() == null || input.getApplicationBuildType().equals("")) {
            output.generateErrorResponse("application build type required for Spring Boot application!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!EnumUtils.isValidEnum(ApplicationBuildType.class, input.getApplicationBuildType())) {
            output.generateErrorResponse("Invalid application build type for Spring Boot. It should be either MAVEN or GRADLE");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    /*
        Check the application package name pattern
     */
    private void checkPackagePattern(String packageName) {
        String pattern = "^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$";
        Matcher m = Pattern.compile(pattern).matcher(packageName);
        if (!m.matches()) {
            output.generateErrorResponse("Invalid Pattern for Package Name!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    /*
    Check If Other application in the company has same name or not
    */
    private void validateApplication() {
        application = applicationRepository.findByIdAndStatus(input.getApplicationId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        Optional<Application> applicationWithSameName = applicationRepository.findByNameIgnoreCaseAndStatusAndIdNotInAndTeamOrganizationCompanyId(input.getName(), Status.V, input.getApplicationId(), this.updateApplicationResponse.getTeam().getOrganization().getCompany().getObjectId());

        if (applicationWithSameName.isPresent()) {
            output.generateErrorResponse("application name already exists in company!");
            throw new ApiErrorException(this.getClass().getName());
        }
         if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)
                && !this.updateApplicationResponse.getTeam().getOrganization().getCompany().getObjectId().toString().equals(application.get().getTeam().getOrganization().getCompany().getObjectId().toString())) {
            output.generateErrorResponse("User does not have authority to update application's team with a team from another company!");
            throw new ApiErrorException(this.getClass().getName());
        }
        if (application.get().getApplicationState() != ApplicationState.PENDING_APP_DETAILS_ADDED
                && application.get().getApplicationState() != ApplicationState.PENDING_APP_VPC_AND_CONFIG_DETAILS_ADDED
                && application.get().getApplicationState() != ApplicationState.PENDING_ADVANCE_CONFIGURATION_ADDED) {
            output.generateErrorResponse("Currently, application Cannot Be Updated!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }
}
