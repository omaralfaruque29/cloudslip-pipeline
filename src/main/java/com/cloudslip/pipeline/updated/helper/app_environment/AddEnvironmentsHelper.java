package com.cloudslip.pipeline.updated.helper.app_environment;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.app_environment.AddAppEnvironmentsDTO;
import com.cloudslip.pipeline.updated.dto.app_environment.AddAppEnvironmentsResponseDTO;
import com.cloudslip.pipeline.updated.dto.app_pipeline_step.CreateAppPipelineStepDTO;
import com.cloudslip.pipeline.updated.dto.vpcresourceupdate.*;
import com.cloudslip.pipeline.updated.enums.*;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.*;
import com.cloudslip.pipeline.updated.model.dummy.SuccessorPipelineStep;
import com.cloudslip.pipeline.updated.model.universal.*;
import com.cloudslip.pipeline.updated.repository.*;
import com.cloudslip.pipeline.updated.service.*;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AddEnvironmentsHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(AddEnvironmentsHelper.class);

    private AddAppEnvironmentsDTO input;
    private ResponseDTO output = new ResponseDTO();

    /*
        contains the response from user management service
     */
    private ResponseDTO response = new ResponseDTO();

    /*
        addAppEnvironmentsResponseDTO is the response from user management.
        it contains the company information, selected company environments and app vpcs with resource config.
     */
    private AddAppEnvironmentsResponseDTO addAppEnvironmentsResponseDTO;

    /*
        application will contain the provided application
     */
    private Optional<Application> application;

    private List<AppEnvironment> appEnvironmentList;

    /*
        selected company environment list will contain all the selected company environments id's by the user
        It is required to find the company environment list which are not selected
      */
    private List<ObjectId> selectedEnvironmentIdList;

    /*
        appEnvListForCheckingPipeLineSteps for checking the pipline steps under the environment
        to check whether the connections are valid or the successors are valid
     */
    private List<AppEnvironment> appEnvListForCheckingPipeLineSteps;


    /*
        Response Message List if any prob occurs during adding app environment and app vpc
        It will contain all the error messages which will be returned to the user
     */
    private List<String> appPipelineStepWarningMessageList;

    /*
        selectedAppEnvironments will contain all the selected environment list along with selected app vpcs and pipeline steps
        It is required for the application template creation
     */
    private List<AppEnvironment> selectedAppEnvironments;


    /*
        unselectedAppEnvironments will contain all the un selected environment list which were previously selected
        It is required for the application template creation
    */
    private List<AppEnvironment> unselectedAppEnvironments;

    /*
        'unselectedAppVpcMap' will contain the unselected app vpcs which were previously selected from a selected app environment
        The key is the selected app environment id
     */
    private Map<String, List<AppVpc>> unselectedAppVpcMap;

    /*
        'selectedAppPipeLineStepMap' will contain the selected pipeline step from a selected app environment
        The key is the selected app environment id
        It is required for updating the app commit state
    */
    private Map<String, List<AppPipelineStep>> selectedAppPipeLineStepMap;

    /*
        'unselectedAppPipeLineStepMap' will contain the selected pipeline step which were selected previously from a selected app environment
        The key is the selected app environment id
        It is required for updating the app commit state
    */
    private Map<String, List<AppPipelineStep>> unselectedAppPipeLineStepMap;


    /*
        previousAppVpcCpu, previousAppVpcMemory, previousAppVpcStorage, previousAppVpcNumberOfInstance
        these maps will contain the previous values from App Vpc Resource
        It is required for calculating the available resource for Vpc
        The values for these four variables will come from App Vpc Create Helper

        key = company environment id, value = vpc resource map
        vpc resource map  contains map with key = vpc name, value = vpc resource value
     */
    private Map<String, Map<String, Integer>> previousAppVpcCpu;
    private Map<String, Map<String, Integer>> previousAppVpcMemory;
    private Map<String, Map<String, Integer>> previousAppVpcStorage;
    private Map<String, Map<String, Integer>> previousAppVpcNumberOfInstance;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppVpcService appVpcService;

    @Autowired
    AppPipelineStepService appPipelineStepService;

    @Autowired
    AppVpcRepository appVpcRepository;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    AppCommitStateRepository appCommitStateRepository;

    @Autowired
    AppEnvironmentStateForAppCommitService appEnvironmentStateForAppCommitService;

    @Autowired
    AppEnvironmentStateForAppCommitRepository appEnvironmentStateForAppCommitRepository;

    @Autowired
    AppCommitPipelineStepService appCommitPipelineStepService;

    @Autowired
    AppCommitPipelineStepRepository appCommitPipelineStepRepository;


    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddAppEnvironmentsDTO) input;
        this.setOutput(output);
        this.response = (ResponseDTO) extraParams[0];
        this.selectedEnvironmentIdList = new ArrayList<>();
        this.appEnvironmentList = new ArrayList<>();
        this.selectedAppEnvironments = new ArrayList<>();
        this.unselectedAppEnvironments = new ArrayList<>();
        this.unselectedAppVpcMap = new HashMap<>();
        this.selectedAppPipeLineStepMap = new HashMap<>();
        this.unselectedAppPipeLineStepMap = new HashMap<>();
        this.appEnvListForCheckingPipeLineSteps = new ArrayList<>();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.addAppEnvironmentsResponseDTO = objectMapper.convertValue(response.getData(), new TypeReference<AddAppEnvironmentsResponseDTO>() { });
        this.application = null;
        appPipelineStepWarningMessageList = new ArrayList<>();

        this.previousAppVpcCpu = new HashMap<>();
        this.previousAppVpcMemory = new HashMap<>();
        this.previousAppVpcStorage = new HashMap<>();
        this.previousAppVpcNumberOfInstance = new HashMap<>();
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        // check if application id is given by the user or not
        if (this.input.getApplicationId() == null) {
            output.generateErrorResponse("application id required!");
            throw new ApiErrorException(this.getClass().getName());
        }

        // check if application exists for the provided application id by the user
        application = applicationRepository.findByIdAndStatus(this.input.getApplicationId(), Status.V);
        if(!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }

        // check if user has authority to access the application
        if (!this.checkAuthority(application.get())) {
            output.generateErrorResponse("User does not have the authority to access the application!");
            throw new ApiErrorException(this.getClass().getName());
        }

        // validate the input vpc resource with the available vpc resource
        this.validateVpcResourceAvailability();
    }


    protected void doPerform() {
        /*
            Adding the selected app environments and save those to database
         */
        this.addEnvironmentsToApplication();

        /*
            Remove the unselected app environments which were previously selected and save those to database
         */
        this.deleteUncheckedEnvironments();

        /*
            If force remove is not active then the successors validity will be checked and will return error message if any error occurs
            if force delete is active then the unselected appPipeline step will be removed which are successors of other steps
         */
        if (!input.isForceRemove() && application.get().getApplicationState() != ApplicationState.PENDING_APP_DETAILS_ADDED && application.get().getApplicationState() != ApplicationState.PENDING_APP_VPC_AND_CONFIG_DETAILS_ADDED) {
            this.checkPipelineSuccessorsForAppEnvList();
            if (!appPipelineStepWarningMessageList.isEmpty()) {
                output.generateWarningResponse(appPipelineStepWarningMessageList);
                throw new ApiErrorException(this.getClass().getName());
            }
        }

        /*
             Saving the selected and unselected app environments only.
             After saving, for each of the environments the app pipeline step and app vpc will be added and deleted
         */
        this.appEnvironmentList  = appEnvironmentRepository.saveAll(appEnvironmentList);

        /*
            Add the selected app vpcs with resource details and pipeline step to database for each selected environment
            Remove the unselected app vpcs and pipeline step which were previously selected
            Update each selected app environments with the selected app vpcs
         */
        this.setAppVpcAndPipeLineStep();

        /*
            selectedAppEnvironments will be returned to the user which will contain the selected app environment list by order
         */
        this.selectedAppEnvironments = appEnvironmentRepository.findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(application.get().getObjectId(), Status.V);
        output.generateSuccessResponse(this.selectedAppEnvironments, "Environments successfully Added to application");

        if (input.isAutoSuccessorEnabled() && (application.get().getApplicationState() == ApplicationState.PENDING_APP_DETAILS_ADDED || application.get().getApplicationState() == ApplicationState.PENDING_APP_VPC_AND_CONFIG_DETAILS_ADDED)) {
            ResponseDTO appPipelineStepListResponse = appPipelineStepService.autoSuccessorCreate(application.get().getObjectId(), requester, actionId);
            if(appPipelineStepListResponse.getStatus() == ResponseStatus.error){
                output.generateErrorResponse("Auto pipeline successor could not create!");
                throw new ApiErrorException(this.getClass().getName());
            }
        }



        /*
            Update all the  existing app commit states with the selected app environment and pipeline step
            remove the unselected app environment and pipeline step from all existing app commit states which were previously selected
         */
        this.updateAppCommitStates(this.selectedAppEnvironments, this.unselectedAppEnvironments, this.selectedAppPipeLineStepMap, this.unselectedAppPipeLineStepMap);

        /*
            if application details were added at the application creation, then update the application state and save it
            Check if use same config for all app vpc is enabled or not and save it to database
         */
        if (application.get().getApplicationState() == ApplicationState.PENDING_APP_DETAILS_ADDED) {
            application.get().setApplicationState(ApplicationState.PENDING_APP_VPC_AND_CONFIG_DETAILS_ADDED);
        }
        application.get().setUseSameConfigInAllAppVpc(input.isUseSameConfig());
        application.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        application.get().setUpdatedBy(requester.getUsername());
        applicationRepository.save(application.get());


        /*
            Update the available resource in each selected vpc in user management service
         */
        this.updateVpcInUserManagementService();
    }


    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        Add the selected environments for an application
     */
    private void addEnvironmentsToApplication() {
        for (EnvironmentOption environmentOption : addAppEnvironmentsResponseDTO.getEnvironmentOptionList()) {
            this.selectedEnvironmentIdList.add(environmentOption.getObjectId()); // it needed for removing environments which are not selected
            AppEnvironment appEnvironment;
            Optional<AppEnvironment> existingAppEnvironment = appEnvironmentRepository.findByEnvironmentIdAndApplicationIdAndStatus(environmentOption.getObjectId(), application.get().getObjectId(), Status.V);
            if (!existingAppEnvironment.isPresent()) {
                appEnvironment =  new AppEnvironment();
                appEnvironment.setApplicationId(application.get().getObjectId());
                appEnvironment.setCompanyId(addAppEnvironmentsResponseDTO.getCompany().getObjectId());
                appEnvironment.setEnvironment(environmentOption);
                appEnvironment.setCreatedBy(requester.getUsername());
                appEnvironment.setCreateDate(String.valueOf(LocalDateTime.now()));
                appEnvironment.setCreateActionId(actionId);
                appEnvironment.setStatus(Status.V);
            } else {
                appEnvironment = existingAppEnvironment.get();
                appEnvironment.setUpdateDate(String.valueOf(LocalDateTime.now()));
                appEnvironment.setUpdatedBy(requester.getUsername());
                appEnvListForCheckingPipeLineSteps.add(existingAppEnvironment.get());
            }
            appEnvironmentList.add(appEnvironment);
        }
    }

    /*
        Remove the unselected app environments which were previously selected for the given application
        No user can unselect the development environment
     */
    private void deleteUncheckedEnvironments() {
        List<AppEnvironment> appEnvironments =  appEnvironmentRepository.findAllByEnvironmentIdNotInAndApplicationIdAndStatus(this.selectedEnvironmentIdList, application.get().getObjectId(), Status.V);
        for (AppEnvironment appEnvironment : appEnvironments) {
            if (!appEnvironment.getEnvironment().getShortName().equalsIgnoreCase("dev")) {
                appEnvironment.setStatus(Status.D);
                AppEnvironment deletedAppEnv = new AppEnvironment();
                deletedAppEnv.setAppVpcList(appEnvironment.getAppVpcList());
                deletedAppEnv.setEnvironment(appEnvironment.getEnvironment());
                this.unselectedAppEnvironments.add(deletedAppEnv);
            }
            appEnvironment.setAppVpcList(new ArrayList<>());
            appEnvironment.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appEnvironment.setUpdatedBy(requester.getUsername());
            appEnvironmentList.add(appEnvironment);
            appEnvListForCheckingPipeLineSteps.add(appEnvironment);
        }
    }

    /*
        Adding the selected app vpcs with resource config and pipeline steps
        removing the unselected app vpcs with resource config and pipeline steps which were previously selected
     */
    private void setAppVpcAndPipeLineStep() {
        for (AppEnvironment appEnvironment : appEnvironmentList) {
            Map<ObjectId, List<VpcResourceDetails>> appVpc = addAppEnvironmentsResponseDTO.getVpcResourceDetailsMap();
            List<VpcResourceDetails> vpcResourceDetailsList = appVpc.get(appEnvironment.getEnvironment().getObjectId());
            vpcResourceDetailsList = vpcResourceDetailsList == null ? new ArrayList<>() : vpcResourceDetailsList;

            // Add and remove app vpcs and add that to app environment
            ResponseDTO appVpcListResponse = appVpcService.createAppVpcList(new CreateAppVpcDTO(vpcResourceDetailsList, appEnvironment, addAppEnvironmentsResponseDTO.getCompany(), application.get()), requester, actionId);
            CreateAppVpcResponseDTO appVpcResponseDTO = objectMapper.convertValue(appVpcListResponse.getData(),  new TypeReference<CreateAppVpcResponseDTO>() { });
            appEnvironment.setAppVpcList(appVpcResponseDTO.getSelectedAppVpc());

            // this four maps is required to update the vpc and company environment in user management. if  vpc exists for an env then it contains the previous resource values
            this.previousAppVpcCpu.put(appEnvironment.getObjectId().toString(), appVpcResponseDTO.getPreviousAppVpcCPU());
            this.previousAppVpcMemory.put(appEnvironment.getObjectId().toString(), appVpcResponseDTO.getPreviousAppVpcMemory());
            this.previousAppVpcStorage.put(appEnvironment.getObjectId().toString(), appVpcResponseDTO.getPreviousAppVpcStorage());
            this.previousAppVpcNumberOfInstance.put(appEnvironment.getObjectId().toString(), appVpcResponseDTO.getPreviousNumberOfInstance());

            // Get Unselected App Vpc List From Selected App Environment Which are Active
            if (appEnvironment.getStatus() == Status.V) {
                this.unselectedAppVpcMap.put(appEnvironment.getObjectId().toString(), appVpcResponseDTO.getUnselectedAppVpc());
            }

            // Add and remove app pipeline steps for eachapp vpc and add that to app environment
            ResponseDTO appPipelineStepListResponse = appPipelineStepService.createAppPipeLineStepList(new CreateAppPipelineStepDTO(appEnvironment, appEnvironment.getAppVpcList()), requester, actionId);
            CreateAppPipelineResponseDTO appPipelineResponseDTO = objectMapper.convertValue(appPipelineStepListResponse.getData(), new TypeReference<CreateAppPipelineResponseDTO>() { });
            appEnvironment.setAppPipelineStepList(appPipelineResponseDTO.getSelectedAppPipeLineSteps());
            appEnvironment = appEnvironmentRepository.save(appEnvironment);

            // If the app environment is selected than put the selected and unselected pipelinse steps. its required for updating all the app commit state
            if (appEnvironment.getStatus() == Status.V) {
                this.selectedAppPipeLineStepMap.put(appEnvironment.getObjectId().toString(), appPipelineResponseDTO.getSelectedAppPipeLineSteps());
                this.unselectedAppPipeLineStepMap.put(appEnvironment.getObjectId().toString(), appPipelineResponseDTO.getUnselectedAppPipeLineSteps());
            }
        }
    }

    /*
        Check if user has authority to access the application
     */
    private boolean checkAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return requester.getCompanyId().toString().equals(application.getTeam().getCompanyId());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && !requester.hasAuthority(Authority.ROLE_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }

    /*
        Check pipeline steps for any unselected pipeline step whether there is any error
     */
    private void checkPipelineSuccessorsForAppEnvList() {
        for (AppEnvironment appEnv :  appEnvListForCheckingPipeLineSteps) {
            List<AppVpc> unSelectedAppVpcList;
            if (appEnv.getStatus() == Status.V && !appEnv.getEnvironment().getShortName().equalsIgnoreCase("dev")) {
                Map<ObjectId, List<VpcResourceDetails>> appVpcResourceDetailsMap = addAppEnvironmentsResponseDTO.getVpcResourceDetailsMap();
                List<VpcResourceDetails> vpcResourceDetailsList = appVpcResourceDetailsMap.get(appEnv.getEnvironment().getObjectId());
                List<ObjectId> appVpcObjectIdList = this.getClusterObjectIdList(vpcResourceDetailsList);
                unSelectedAppVpcList = appVpcRepository.findByVpcIdNotInAndAppEnvironmentIdAndStatus(appVpcObjectIdList, appEnv.getObjectId(), Status.V);
            } else {
                unSelectedAppVpcList = appVpcRepository.findAllByAppEnvironmentIdAndStatus(appEnv.getObjectId(), Status.V);
            }
            this.checkPipelineSuccessorsForCluster(appEnv, unSelectedAppVpcList);
        }
    }

    /*
        check the successor of pipeline
     */
    private void checkPipelineSuccessorsForCluster(AppEnvironment appEnvironment, List<AppVpc> unSelectedAppVpcList) {
        for (AppVpc unselectedAppVpc : unSelectedAppVpcList) {
            Optional<AppPipelineStep> appPipelineStep = appPipelineStepRepository.findByAppVpcIdAndAppEnvironmentIdAndStatus(unselectedAppVpc.getObjectId(), appEnvironment.getObjectId(), Status.V);
            if (appPipelineStep.isPresent()) {

                //Check If AppPipeline Step is a Successor of other App Pipeline Step's
                List<AppPipelineStep> parentAppPipelineStepList = appPipelineStepRepository.findAllBySuccessorsAppPipelineStepIdAndStatus(appPipelineStep.get().getObjectId(), Status.V);
                if (!parentAppPipelineStepList.isEmpty()) {
                    for (AppPipelineStep parentPipelineStep : parentAppPipelineStepList) {
                        appPipelineStepWarningMessageList.add("'"+ appPipelineStep.get().getName() + "' is a successor of '" + parentPipelineStep.getName() + "'");
                    }
                }

                // Check If AppPipeline Step has a Successor App Pipeline Step's
                if (appPipelineStep.get().getSuccessors() != null && !appPipelineStep.get().getSuccessors().isEmpty()) {
                    for (SuccessorPipelineStep successorPipelineStep : appPipelineStep.get().getSuccessors()) {
                        AppPipelineStep successor = successorPipelineStep.getAppPipelineStep();
                        if (successor.getStepType() == PipelineStepType.CUSTOM) {
                            Optional<AppPipelineStep> customPipeline = appPipelineStepRepository.findByIdAndStatus(successor.getObjectId(), Status.V);
                            successor = customPipeline.get();
                        }
                        appPipelineStepWarningMessageList.add("'" + appPipelineStep.get().getName() + " has a successor step  " + successor.getName() + "'");
                    }
                }
            }
        }
    }

    /*
        Update All Existing App Commit States
    */
    private void updateAppCommitStates(List<AppEnvironment> selectedAppEnvironments, List<AppEnvironment> unselectedAppEnvironments, Map<String,List<AppPipelineStep>> selectedAppPipeLineStepMap, Map<String, List<AppPipelineStep>> unselectedAppPipeLineStepMap) {
        List<AppCommitState> appCommitStateList = appCommitStateRepository.findAllByAppCommitApplicationIdAndStatus(application.get().getObjectId(), Status.V);
        for (AppCommitState appCommitState : appCommitStateList) {
            this.updateAppEnvStateForAppCommit(appCommitState, selectedAppEnvironments, selectedAppPipeLineStepMap, unselectedAppPipeLineStepMap);
            this.deleteAppEnvStateForAppCommit(appCommitState, unselectedAppEnvironments);
        }
    }

    /*
        Update the selected app environment to all the app commit states
     */
    private void  updateAppEnvStateForAppCommit(AppCommitState appCommitState, List<AppEnvironment> selectedAppEnvironments, Map<String, List<AppPipelineStep>> selectedAppPipeLineStepMap, Map<String, List<AppPipelineStep>> unselectedAppPipeLineStepMap) {
        List<AppEnvironmentStateForAppCommit> appEnvStateList = new ArrayList<>();
        for (AppEnvironment appEnvironment : selectedAppEnvironments) {
            if (appEnvironment.getObjectId() != null) {
                int environmentStateIndex = appCommitState.getEnvironmentStateIndexForEnvironment(appEnvironment.getObjectId()); // get index of the app env state from list of env states
                if (environmentStateIndex == -1) { // If selected environment is not present in a app commit state then create new app commit environment
                    ResponseDTO appEnvStateForAppCommitResponse = appEnvironmentStateForAppCommitService.createAppEnvStateForAddEnvironment(new AppEnvStateForAddEnvironmentDTO(application.get(), appEnvironment, appCommitState.getAppCommit().getObjectId()), requester, actionId);
                    AppEnvironmentStateForAppCommit appEnvState = objectMapper.convertValue(appEnvStateForAppCommitResponse.getData(),
                            new TypeReference<AppEnvironmentStateForAppCommit>() { });
                    appEnvStateList.add(appEnvState);
                } else { // if exists then check for any update in app pipeline step
                    if (selectedAppPipeLineStepMap.containsKey(appEnvironment.getObjectId().toString())) {
                        List<AppCommitPipelineStep> appCommitPipelineStepList = this.updateAppPipelineStepsForAppCommitState(appCommitState, appEnvironment, environmentStateIndex, selectedAppPipeLineStepMap.get(appEnvironment.getObjectId().toString()));
                        appCommitState.getEnvironmentStateList().get(environmentStateIndex).setSteps(appCommitPipelineStepList);
                    }
                    if (unselectedAppPipeLineStepMap.containsKey(appEnvironment.getObjectId().toString())) {
                        appCommitState = this.deleteAppPipelineStepsForAppCommitState(appCommitState, environmentStateIndex, unselectedAppPipeLineStepMap.get(appEnvironment.getObjectId().toString()));
                    }
                    appEnvStateList.add(appCommitState.getEnvironmentStateList().get(environmentStateIndex));
                }
            }
        }
        appCommitState.setEnvironmentStateList(appEnvStateList);
        appCommitStateRepository.save(appCommitState);
    }

    /*
        Remove the unselected app environments from all the app commit state
     */
    private void deleteAppEnvStateForAppCommit(AppCommitState appCommitState, List<AppEnvironment> unselectedAppEnvironments) {
        for (AppEnvironment appEnvironment : unselectedAppEnvironments) {
            if (appEnvironment.getObjectId() != null) {
                int environmentStateIndex = appCommitState.getEnvironmentStateIndexForEnvironment(appEnvironment.getObjectId());
                if (environmentStateIndex != -1) {
                    AppEnvironmentStateForAppCommit appEnvironmentStateForAppCommit = appCommitState.getEnvironmentStateList().get(environmentStateIndex);
                    appEnvironmentStateForAppCommit.setStatus(Status.D);
                    appEnvironmentStateForAppCommit.setUpdateDate(String.valueOf(LocalDateTime.now()));
                    appEnvironmentStateForAppCommit.setUpdatedBy(requester.getUsername());
                    appEnvironmentStateForAppCommit.setLastUpdateActionId(actionId);
                    appEnvironmentStateForAppCommitRepository.save(appEnvironmentStateForAppCommit);
                    appCommitState.getEnvironmentStateList().remove(environmentStateIndex);
                    appCommitStateRepository.save(appCommitState);
                }
            }
        }
    }

    /*
        Update app commit pipeline steps for selected app commit state
    */
    private List<AppCommitPipelineStep> updateAppPipelineStepsForAppCommitState(AppCommitState appCommitState, AppEnvironment appEnvironment, int environmentStateIndex, List<AppPipelineStep> selectedPipelineStepList) {
        List<AppCommitPipelineStep> appCommitPipelineStepList = appCommitState.getEnvironmentStateList().get(environmentStateIndex).getSteps();
        for (AppPipelineStep appPipelineStep : selectedPipelineStepList) {
            if  (appPipelineStep != null) {
                int pipeLineStepIndex = this.getAppCommitPipelineStepIndex(appCommitPipelineStepList, appPipelineStep);
                if (pipeLineStepIndex != -1) { // check if new step exists in selected app pipeline step
                    appCommitPipelineStepList.get(pipeLineStepIndex).getAppPipelineStep().setSuccessors(appPipelineStep.getSuccessors());
                } else { // if does not exists then create new app commit pipeline step for the app environment and add it to app commit environment's pipeline step list
                    ResponseDTO appCommitPipelineStepResponse = appCommitPipelineStepService.createAppCommitPipelineStepForAddEnv(new CreateAppCommitPipelineStepForAddEnvDTO(appCommitState.getAppCommit().getObjectId(), appEnvironment, appPipelineStep), requester, actionId);
                    AppCommitPipelineStep appCommitPipelineStep = objectMapper.convertValue(appCommitPipelineStepResponse.getData(),
                            new TypeReference<AppCommitPipelineStep>() { });
                    appCommitPipelineStepList.add(appCommitPipelineStep);
                }
            }
        }
        return appCommitPipelineStepList;
    }

    /*
        Remove App Commit Pipeline Steps Which were not selected in the new commit
    */
    private AppCommitState deleteAppPipelineStepsForAppCommitState(AppCommitState appCommitState, int environmentStateIndex, List<AppPipelineStep> unselectedPipelineStepList) {
        List<AppCommitPipelineStep> appCommitPipelineStepList = appCommitState.getEnvironmentStateList().get(environmentStateIndex).getSteps();
        for (AppPipelineStep appPipelineStep : unselectedPipelineStepList) {
            int pipeLineStepIndex = this.getAppCommitPipelineStepIndex(appCommitPipelineStepList, appPipelineStep);
            if (pipeLineStepIndex != -1) { // if exists the remove it
                AppCommitPipelineStep appCommitPipelineStep = appCommitPipelineStepList.get(pipeLineStepIndex);
                appCommitPipelineStep.setStatus(Status.D);
                appCommitPipelineStep.setUpdateDate(String.valueOf(LocalDateTime.now()));
                appCommitPipelineStep.setUpdatedBy(requester.getUsername());
                appCommitPipelineStep.setLastUpdateActionId(actionId);
                appCommitPipelineStepRepository.save(appCommitPipelineStep);
                appCommitState.getEnvironmentStateList().get(environmentStateIndex).getSteps().remove(pipeLineStepIndex);
            }
        }
        return appCommitState;
    }

    /*
        Fetching the index of a particular app commit pipeline step from app commit pipeline step list matching with the pipeline step id
     */
    private int getAppCommitPipelineStepIndex (List<AppCommitPipelineStep> appCommitPipelineStepList, AppPipelineStep appPipelineStep) {
        int index = 0;
        for (AppCommitPipelineStep appCommitPipelineStep : appCommitPipelineStepList) {
            if(appPipelineStep.getId().equals(appCommitPipelineStep.getAppPipelineStep().getId())){
                return index;
            }
            index++;
        }
        return -1;
    }

    /*
        Get the object id list from list of app vpc resource object
     */
    private List<ObjectId> getClusterObjectIdList(List<VpcResourceDetails> vpcResourceDetailsList) {
        if (vpcResourceDetailsList.isEmpty()) {
            return null;
        }
        List<ObjectId> appVpcObjectIdList = new ArrayList<>();
        for (VpcResourceDetails vpcResourceDetails : vpcResourceDetailsList) {
            appVpcObjectIdList.add(vpcResourceDetails.getVpc().getObjectId());
        }
        return appVpcObjectIdList;
    }


    /*
        Update Vpc in user management service
        Get the updated vpc information and update the vpc in existing app Vpc and app environment
     */
    private void updateVpcInUserManagementService() {
        VpcResourceUpdateDTO vpcResourceUpdateDTO = this.getVpcResourceUpdateDTO();
        HttpHeaders headers = Utils.generateHttpHeaders(requester, actionId.toString());
        HttpEntity<VpcResourceUpdateDTO> request = new HttpEntity<>(vpcResourceUpdateDTO, headers);
        ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/app-env/update-vpc-resource", request, ResponseDTO.class);
        if(response.getStatus() == ResponseStatus.error){
            output.generateErrorResponse("Vpc could not update!");
            throw new ApiErrorException(this.getClass().getName());
        }
        List<Vpc> vpcListWithUpdatedResourceDetails = objectMapper.convertValue(response.getData(), new TypeReference<List<Vpc>>() { });
        this.updateResourceInAllExistingAppVpcAndAppEnv(vpcListWithUpdatedResourceDetails);
    }

    /*
        Fetch the vpc resource for selected and delete vpc for selected environment and unselected environment which were previously selected
        it is required to update the vpc and company environment in user management service
     */
    private VpcResourceUpdateDTO  getVpcResourceUpdateDTO() {
        List<EnvironmentInfoUpdateDTO> selectedEnvInfoList = new ArrayList<>();
        List<EnvironmentInfoUpdateDTO> unselectedEnvInfoList = new ArrayList<>();

        for (int countAppEnv = 0; countAppEnv < selectedAppEnvironments.size(); countAppEnv++) {
            List<VpcResourceDTO> selectedVpcList = this.getVpcResourceDTOList(selectedAppEnvironments.get(countAppEnv).getAppVpcList());
            if (!selectedVpcList.isEmpty()) {
                EnvironmentInfoUpdateDTO selectedEnvironmentInfoUpdateDTO = new EnvironmentInfoUpdateDTO();
                selectedEnvironmentInfoUpdateDTO.setEnvironmentId(selectedAppEnvironments.get(countAppEnv).getEnvironment().getObjectId());
                selectedEnvironmentInfoUpdateDTO.setVpcList(selectedVpcList);
                selectedEnvInfoList.add(selectedEnvironmentInfoUpdateDTO);
            }

            // Unselected App Vpc List For Selected App Environments
            if (unselectedAppVpcMap.containsKey(selectedAppEnvironments.get(countAppEnv).getObjectId().toString())) {
                List <AppVpc> unselectedAppVpcList = unselectedAppVpcMap.get(selectedAppEnvironments.get(countAppEnv).getObjectId().toString());
                List<VpcResourceDTO> unselectedVpcList = this.getVpcResourceDTOList(unselectedAppVpcList);
                if (!unselectedVpcList.isEmpty()) {
                    EnvironmentInfoUpdateDTO unselectedEnvironmentInfoUpdateDTO = new EnvironmentInfoUpdateDTO();
                    unselectedEnvironmentInfoUpdateDTO.setEnvironmentId(selectedAppEnvironments.get(countAppEnv).getEnvironment().getObjectId());
                    unselectedEnvironmentInfoUpdateDTO.setVpcList(unselectedVpcList);
                    unselectedEnvInfoList.add(unselectedEnvironmentInfoUpdateDTO);
                }

            }
        }

        for (int countAppEnv = 0; countAppEnv < unselectedAppEnvironments.size(); countAppEnv++) {
            List<VpcResourceDTO> unselectedVpcList = this.getVpcResourceDTOList(unselectedAppEnvironments.get(countAppEnv).getAppVpcList());
            if (!unselectedVpcList.isEmpty()) {
                EnvironmentInfoUpdateDTO unselectedEnvironmentInfoUpdateDTO = new EnvironmentInfoUpdateDTO();
                unselectedEnvironmentInfoUpdateDTO.setEnvironmentId(unselectedAppEnvironments.get(countAppEnv).getEnvironment().getObjectId());
                unselectedEnvironmentInfoUpdateDTO.setVpcList(unselectedVpcList);
                unselectedEnvInfoList.add(unselectedEnvironmentInfoUpdateDTO);
            }
        }

        VpcResourceUpdateDTO vpcResourceUpdateDTO = new VpcResourceUpdateDTO();
        vpcResourceUpdateDTO.setSelectedEnvInfoList(selectedEnvInfoList);
        vpcResourceUpdateDTO.setUnselectedEnvInfoList(unselectedEnvInfoList);

        return vpcResourceUpdateDTO;
    }

    /*
        Vpc List with updated resource details are added to existing app vpc and app environments
    */
    private void updateResourceInAllExistingAppVpcAndAppEnv(List<Vpc> updatedVpcList) {
        for (Vpc vpc : updatedVpcList) {
            List<AppEnvironment> allAppEnvironmentList = appEnvironmentRepository.findAllByAppVpcListVpcIdAndStatus(vpc.getObjectId(), Status.V);
            for (AppEnvironment appEnvironment : allAppEnvironmentList) {
                Optional<AppVpc> appVpc = appVpcRepository.findByVpcIdAndAppEnvironmentIdAndStatus(vpc.getObjectId(),appEnvironment.getObjectId(), Status.V);
                if (appVpc.isPresent()) {
                    int appVpcIndex = appEnvironment.getAppVpcIndex(appVpc.get().getObjectId());
                    appVpc.get().setVpc(vpc);
                    AppVpc savedAppVpc = appVpcRepository.save(appVpc.get());
                    if (appVpcIndex != -1) {
                        appEnvironment.getAppVpcList().set(appVpcIndex, savedAppVpc);
                        appEnvironmentRepository.save(appEnvironment);
                    }
                }
            }
        }
    }

    /*
        Getting the list of converting the app vpc into vpc resource dto
        this is required to update in user management
     */
    private List<VpcResourceDTO> getVpcResourceDTOList(List<AppVpc> appVpcList) {
        List<VpcResourceDTO> vpcResourceDTOList = new ArrayList<>();
        for (AppVpc appVpc : appVpcList) {
            VpcResourceDTO vpcResourceDTO = new VpcResourceDTO();
            vpcResourceDTO.setVpcId(appVpc.getVpc().getObjectId());
            vpcResourceDTO.setSelectedCpuSize(appVpc.getResourceDetails().getMaxCpu());
            vpcResourceDTO.setSelectedMemorySize(appVpc.getResourceDetails().getMaxMemory());
            vpcResourceDTO.setSelectedStorageSize(appVpc.getResourceDetails().getMaxStorage());
            if (appVpc.getResourceDetails().isAutoScalingEnabled()) {
                vpcResourceDTO.setNumberOfInstance(appVpc.getResourceDetails().getMinNumOfInstance());
            } else {
                vpcResourceDTO.setNumberOfInstance(appVpc.getResourceDetails().getDesiredNumberOfInstance());
            }
            if (this.previousAppVpcCpu.containsKey(appVpc.getAppEnvironmentId())) {
                Map<String, Integer> previousVpcCpu = this.previousAppVpcCpu.get(appVpc.getAppEnvironmentId());
                if (previousVpcCpu.containsKey(appVpc.getVpc().getName())) {
                    vpcResourceDTO.setPreviousSelectedCpuSize(previousVpcCpu.get(appVpc.getVpc().getName()));
                }
            }
            if (this.previousAppVpcMemory.containsKey(appVpc.getAppEnvironmentId())) {
                Map<String, Integer> previousVpcMemory = this.previousAppVpcMemory.get(appVpc.getAppEnvironmentId());
                if (previousVpcMemory.containsKey(appVpc.getVpc().getName())) {
                    vpcResourceDTO.setPreviousSelectedMemorySize(previousVpcMemory.get(appVpc.getVpc().getName()));
                }
            }
            if (this.previousAppVpcStorage.containsKey(appVpc.getAppEnvironmentId())) {
                Map<String, Integer> previousVpcStorage = this.previousAppVpcStorage.get(appVpc.getAppEnvironmentId());
                if (previousVpcStorage.containsKey(appVpc.getVpc().getName())) {
                    vpcResourceDTO.setPreviousSelectedStorageSize(previousVpcStorage.get(appVpc.getVpc().getName()));
                }
            }
            if (this.previousAppVpcNumberOfInstance.containsKey(appVpc.getAppEnvironmentId())) {
                Map<String, Integer> previousVpcNumberOfInstance = this.previousAppVpcNumberOfInstance.get(appVpc.getAppEnvironmentId());
                if (previousVpcNumberOfInstance.containsKey(appVpc.getVpc().getName())) {
                    vpcResourceDTO.setPreviousNumberOfInstance(previousVpcNumberOfInstance.get(appVpc.getVpc().getName()));
                }
            }
            vpcResourceDTOList.add(vpcResourceDTO);
        }
        return vpcResourceDTOList;
    }

    /*
        It will Check if any selected vpc exists for application
        if exists then the resource input will validate with the existing vpc resource
        else just check if vpc has available resource or not
     */
    private void validateVpcResourceAvailability() {
        List<EnvironmentOption> selectedEnvironmentList = this.addAppEnvironmentsResponseDTO.getEnvironmentOptionList();
        if (selectedEnvironmentList != null) {
            for (EnvironmentOption environmentOption : selectedEnvironmentList) {
                Map<ObjectId, List<VpcResourceDetails>> vpcResourceMap = addAppEnvironmentsResponseDTO.getVpcResourceDetailsMap();
                List<VpcResourceDetails> vpcResourceDetailsList = vpcResourceMap.get(environmentOption.getObjectId());
                vpcResourceDetailsList = vpcResourceDetailsList == null ? new ArrayList<>() : vpcResourceDetailsList;

                Optional<AppEnvironment> existingAppEnv = appEnvironmentRepository.findByEnvironmentIdAndApplicationIdAndStatus(environmentOption.getObjectId(), application.get().getObjectId(), Status.V);
                for (VpcResourceDetails vpcResourceDetails : vpcResourceDetailsList) {
                    int availableCPU = vpcResourceDetails.getVpc().getAvailableCPU(), availableMemory = vpcResourceDetails.getVpc().getAvailableMemory(), availableStorage = vpcResourceDetails.getVpc().getAvailableStorage();
                    int inputNumberOfInstance = vpcResourceDetails.isAutoScalingEnabled() ? vpcResourceDetails.getMinNumOfInstance() : vpcResourceDetails.getDesiredNumberOfInstance();
                    if (existingAppEnv.isPresent()) {
                        Optional<AppVpc> existingAppVpc = appVpcRepository.findByVpcIdAndAppEnvironmentIdAndStatus(vpcResourceDetails.getVpc().getObjectId(), existingAppEnv.get().getObjectId(), Status.V);
                        if (existingAppVpc.isPresent()) {
                            if (existingAppVpc.get().getResourceDetails() != null) { // if already vpc details exists then at first add previous value and then deduct the current value
                                int prevNumberOfInstance = existingAppVpc.get().getResourceDetails().isAutoScalingEnabled() ? existingAppVpc.get().getResourceDetails().getMinNumOfInstance() : existingAppVpc.get().getResourceDetails().getDesiredNumberOfInstance();
                                availableCPU = vpcResourceDetails.getVpc().getAvailableCPU() + (existingAppVpc.get().getResourceDetails().getMaxCpu() * prevNumberOfInstance);
                                availableMemory = vpcResourceDetails.getVpc().getAvailableMemory() + (existingAppVpc.get().getResourceDetails().getMaxMemory() * prevNumberOfInstance);
                                availableStorage = vpcResourceDetails.getVpc().getAvailableStorage() + (existingAppVpc.get().getResourceDetails().getMaxStorage() * prevNumberOfInstance);
                            }
                        }
                    }
                    if (availableCPU < (vpcResourceDetails.getMaxCpu() * inputNumberOfInstance)) {
                        output.generateErrorResponse(String.format("Available CPU has been exceeded for '%s' vpc of '%s' environment!", vpcResourceDetails.getVpc().getName(), environmentOption.getName()));
                        throw new ApiErrorException(this.getClass().getName());
                    }
                    if (availableMemory < (vpcResourceDetails.getMaxMemory() * inputNumberOfInstance)) {
                        output.generateErrorResponse(String.format("Available memory has been exceeded for '%s' vpc of '%s' environment!", vpcResourceDetails.getVpc().getName(), environmentOption.getName()));
                        throw new ApiErrorException(this.getClass().getName());
                    }
                    if (availableStorage < (vpcResourceDetails.getMaxStorage() * inputNumberOfInstance)) {
                        output.generateErrorResponse(String.format("Available storage has been exceeded for '%s' vpc of '%s' environment!", vpcResourceDetails.getVpc().getName(), environmentOption.getName()));
                        throw new ApiErrorException(this.getClass().getName());
                    }
                }
            }
        }
    }
}