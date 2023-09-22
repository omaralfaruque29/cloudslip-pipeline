package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.application.CreateApplicationDTO;
import com.cloudslip.pipeline.updated.dto.application.CreateApplicationResponseDTO;
import com.cloudslip.pipeline.updated.dto.app_environment.AddDevelopmentEnvironmentDTO;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.AppEnvironmentService;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.EnumUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CreateApplicationHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(CreateApplicationHelper.class);

    private CreateApplicationDTO input;
    private ResponseDTO output = new ResponseDTO();

    private ResponseDTO response = new ResponseDTO();
    private CreateApplicationResponseDTO createApplicationResponse;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentService appEnvironmentService;

    @Autowired
    private ObjectMapper objectMapper;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateApplicationDTO) input;
        this.setOutput(output);
        this.response = (ResponseDTO) extraParams[0];
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.createApplicationResponse = objectMapper.convertValue(response.getData(), new TypeReference<CreateApplicationResponseDTO>() { });
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.validateRequiredFields(); // Check Required Fields For application
        this.validateEnums(); // check application enum validity
        this.validateCompanyGitAndDockerInfo();
        this.checkApplicationWithSameName();

    }


    protected void doPerform() {
        Application application = new Application();
        application.setId(new ObjectId()); // for Web Socket Topic
        application.setName(this.input.getName().trim().replaceAll(" +", " "));
        application.setUniqueName(Utils.removeAllSpaceWithDash(this.input.getName().toLowerCase().trim().replaceAll(" +", " ")) + "-" + application.getId());
        application.setTeam(this.createApplicationResponse.getTeam());
        application.setCreationType(ApplicationCreationType.valueOf(input.getAppCreationType()));
        application.setType(ApplicationType.valueOf(input.getApplicationType()));
        application.setApplicationState(ApplicationState.PENDING_APP_DETAILS_ADDED);
        application.setDockerRepoName(createApplicationResponse.getCompany().getDockerHubInfo().getDockerhubId() + "/" + application.getName().toLowerCase().trim().replaceAll(" ","_"));
        if (ApplicationType.valueOf(input.getApplicationType()) == ApplicationType.SPRING_BOOT) {
            application.setPackageName(input.getPackageName().trim());
            application.setBuildType(ApplicationBuildType.valueOf(input.getApplicationBuildType()));
        }
        application.setAppCreateStatus(ApplicationStatus.PENDING);
        application.setWebSocketTopic(generateWebSocketTopic(application.getId()));
        application.setCreatedBy(requester.getUsername());
        application.setCreateDate(String.valueOf(LocalDateTime.now()));
        application.setCreateActionId(actionId);
        application.setAppBuildJobNameInJenkins(createApplicationResponse.getCompany().getName().toLowerCase().trim().replaceAll(" ","_")
                +"-"+ application.getName().toLowerCase().trim().replaceAll(" ", "-") + "-pipeline");
        if (ApplicationCreationType.valueOf(input.getAppCreationType()) == ApplicationCreationType.FROM_GIT_SOURCE) {
            application.setGitRepositoryName(this.input.getGitRepositoryName());
            application.setGitBranchName(this.input.getGitBranchName());
        }
        application = applicationRepository.save(application);
        this.addDevelopmentEnvironment(application);
        output.generateSuccessResponse(application, String.format("application '%s' has successfully created", application.getName()));
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {
    }

    /*
        Check if Company Has Git Information or not
     */
    private void validateCompanyGitAndDockerInfo() {
        if (createApplicationResponse.getCompany().getGitInfo() == null) {
            output.generateErrorResponse("Company Git Info Required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (createApplicationResponse.getCompany().getGitInfo().getUsername() == null || createApplicationResponse.getCompany().getGitInfo().getUsername().equals("")) {
            output.generateErrorResponse("Git user name is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (createApplicationResponse.getCompany().getGitInfo().getSecretKey() == null || createApplicationResponse.getCompany().getGitInfo().getSecretKey().equals("")) {
            output.generateErrorResponse("Git api token is required!");
            throw new ApiErrorException(this.getClass().getName());
        }

        if (createApplicationResponse.getCompany().getDockerHubInfo() == null) {
            output.generateErrorResponse("Company Docker info required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (createApplicationResponse.getCompany().getDockerHubInfo().getDockerhubId() == null || createApplicationResponse.getCompany().getDockerHubInfo().getDockerhubId().equals("")) {
            output.generateErrorResponse("Company Docker id required!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void validateRequiredFields() {
        if (input.getName() == null || input.getName().equals("")) {
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

    /*
        Checking Enum Validity
     */
    private void validateEnums() {
        if (!EnumUtils.isValidEnum(ApplicationCreationType.class, input.getAppCreationType())) {
            output.generateErrorResponse("Invalid application creation type!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (ApplicationCreationType.valueOf(input.getAppCreationType()) == ApplicationCreationType.FROM_GIT_SOURCE && (input.getGitRepositoryName() == null || input.getGitBranchName() == null)) {
            output.generateErrorResponse("Git Repository and Branch name required!");
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
    private void checkApplicationWithSameName() {
        Optional<Application> applicationWithSameName = applicationRepository.findByNameIgnoreCaseAndStatusAndTeamOrganizationCompanyId(input.getName(),
                Status.V, this.createApplicationResponse.getTeam().getCompanyObjectId());
        if (applicationWithSameName.isPresent()) {
            output.generateErrorResponse("application name already exists in company!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private String generateWebSocketTopic(String applicationId) {
        return "wst-app-" + applicationId;
    }

    /*
        Add Development Environment For The application
     */
    private void addDevelopmentEnvironment(Application application) {
        AddDevelopmentEnvironmentDTO addDevelopmentEnvironmentDTO = new AddDevelopmentEnvironmentDTO(createApplicationResponse.getCompany(), application, createApplicationResponse.getEnvironmentOption());
        ResponseDTO response = appEnvironmentService.addDevelopmentEnvironment(addDevelopmentEnvironmentDTO, requester, actionId);
        if (response.getStatus() == ResponseStatus.error) {
            output.generateErrorResponse("Development environment not added to application!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }
}
