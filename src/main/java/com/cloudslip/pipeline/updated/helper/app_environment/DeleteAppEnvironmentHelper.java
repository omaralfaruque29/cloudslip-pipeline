package com.cloudslip.pipeline.updated.helper.app_environment;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.vpcresourceupdate.EnvironmentInfoUpdateDTO;
import com.cloudslip.pipeline.updated.dto.vpcresourceupdate.VpcResourceDTO;
import com.cloudslip.pipeline.updated.dto.vpcresourceupdate.VpcResourceUpdateDTO;
import com.cloudslip.pipeline.updated.enums.ApplicationStatus;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.AppVpcService;
import com.cloudslip.pipeline.updated.service.AppEnvironmentStateForAppCommitService;
import com.cloudslip.pipeline.updated.service.AppPipelineStepService;
import com.cloudslip.pipeline.updated.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeleteAppEnvironmentHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppVpcService appVpcService;

    @Autowired
    AppPipelineStepService appPipelineStepService;

    @Autowired
    AppEnvironmentStateForAppCommitService appEnvironmentStateForAppCommitService;

    private Optional<Application> application;

    @Autowired
    private CustomRestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
        application = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.application = applicationRepository.findByIdAndStatus(input.getId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        List<EnvironmentInfoUpdateDTO> selectedEnvInfo = new ArrayList<>();
        List<EnvironmentInfoUpdateDTO> unselectedEnvInfo = new ArrayList<>();
        List<AppEnvironment> deletedAppEnvList = new ArrayList<>(); // It requires for the agent update
        List<AppEnvironment> appEnvironmentList = appEnvironmentRepository.findAllByApplicationIdAndStatus(application.get().getObjectId(), Status.V);
        for (AppEnvironment appEnvironment : appEnvironmentList) {

            // Deleting All App VPCs For Each App Environment
            if (appEnvironment.getAppVpcList() != null) {
                ResponseDTO appVpcListResponse = appVpcService.deleteAllAppVpc(new GetObjectInputDTO(appEnvironment.getObjectId()), requester, actionId);
                if (appVpcListResponse.getStatus() == ResponseStatus.error) {
                    output.generateErrorResponse(appVpcListResponse.getMessage());
                    throw new ApiErrorException(this.getClass().getName());
                }
                EnvironmentInfoUpdateDTO environmentInfoUpdateDTO = new EnvironmentInfoUpdateDTO();
                environmentInfoUpdateDTO.setEnvironmentId(appEnvironment.getEnvironment().getObjectId());
                environmentInfoUpdateDTO.setVpcList(this.getVpcResourceDTOList(appEnvironment.getAppVpcList()));
                unselectedEnvInfo.add(environmentInfoUpdateDTO);
            }

            // Deleting All application Pipeline Steps For Each App Environment
            if (appEnvironment.getAppPipelineStepList() != null) {
                ResponseDTO appPipelineListResponse = appPipelineStepService.deleteAllAppPipelineStep(new GetObjectInputDTO(appEnvironment.getObjectId()), requester, actionId);
                if (appPipelineListResponse.getStatus() == ResponseStatus.error) {
                    output.generateErrorResponse(appPipelineListResponse.getMessage());
                    throw new ApiErrorException(this.getClass().getName());
                }
            }

            // Delete All application Environment State
            if (application.get().getAppCreateStatus() == ApplicationStatus.COMPLETED) {
                ResponseDTO appEnvStateForAppCommitResponse = appEnvironmentStateForAppCommitService.deleteAllAppEnvStateForAppCommit(new GetObjectInputDTO(appEnvironment.getObjectId()), requester, actionId);
                if (appEnvStateForAppCommitResponse.getStatus() == ResponseStatus.error) {
                    output.generateErrorResponse(appEnvStateForAppCommitResponse.getMessage());
                    throw new ApiErrorException(this.getClass().getName());
                }
            }
            appEnvironment.setStatus(Status.D);
            appEnvironment.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appEnvironment.setUpdatedBy(requester.getUsername());
            appEnvironment.setLastUpdateActionId(actionId);
            appEnvironment = appEnvironmentRepository.save(appEnvironment);
            deletedAppEnvList.add(appEnvironment);
        }
        VpcResourceUpdateDTO vpcResourceUpdateDTO = new VpcResourceUpdateDTO(selectedEnvInfo, unselectedEnvInfo);
        this.updateVpcInUserManagementService(vpcResourceUpdateDTO);

        output.generateSuccessResponse(deletedAppEnvList,  " application Environment List Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }


    /*
        Update Vpc in user management service
        Get the updated vpc information and update the vpc in existing app Vpc and app environment
    */
    private void updateVpcInUserManagementService(VpcResourceUpdateDTO vpcResourceUpdateDTO) {
        if (!vpcResourceUpdateDTO.getUnselectedEnvInfoList().isEmpty()) {
            HttpHeaders headers = Utils.generateHttpHeaders(requester, actionId.toString());
            HttpEntity<VpcResourceUpdateDTO> request = new HttpEntity<>(vpcResourceUpdateDTO, headers);
            ResponseDTO response = restTemplate.postForObject(applicationProperties.getUserManagementServiceBaseUrl() + "api/app-env/update-vpc-resource", request, ResponseDTO.class);
            if(response.getStatus() == ResponseStatus.error){
                output.generateErrorResponse("Vpc could not update!");
                throw new ApiErrorException(this.getClass().getName());
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

            int maxCpu = 0, maxMemory = 0, maxStorage = 0, numberOfInstance = 0;
            if (appVpc.getResourceDetails() != null) {
                maxCpu = appVpc.getResourceDetails().getMaxCpu();
                maxMemory = appVpc.getResourceDetails().getMaxMemory();
                maxStorage = appVpc.getResourceDetails().getMaxStorage();
                numberOfInstance = appVpc.getResourceDetails().isAutoScalingEnabled() ? appVpc.getResourceDetails().getMinNumOfInstance() : appVpc.getResourceDetails().getDesiredNumberOfInstance();
           }
            vpcResourceDTO.setSelectedCpuSize(maxCpu);
            vpcResourceDTO.setSelectedMemorySize(maxMemory);
            vpcResourceDTO.setSelectedStorageSize(maxStorage);
            vpcResourceDTO.setNumberOfInstance(numberOfInstance);
            vpcResourceDTOList.add(vpcResourceDTO);
        }
        return vpcResourceDTOList;
    }
}
