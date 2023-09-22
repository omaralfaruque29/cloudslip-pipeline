package com.cloudslip.pipeline.updated.helper.app_vpc;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.kubeconfig.IngressConfig;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.dummy.ResourceDetails;
import com.cloudslip.pipeline.updated.model.universal.Vpc;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class CreateAppVpcHelper extends AbstractHelper {

    private CreateAppVpcDTO input;
    private ResponseDTO output = new ResponseDTO();

    /*
        previousAppVpcCPU, previousAppVpcMemory, previousAppVpcStorage, previousNumberOfInstance
        these maps will contain the previous values from App Vpc Resource
        It is required for calculating the available resource for Vpc
        The values for these four variables will come from App Vpc Create Helper

        key = vpc name, value = vpc resource value
     */
    private Map<String, Integer> previousAppVpcCPU;
    private Map<String, Integer> previousAppVpcMemory;
    private Map<String, Integer> previousAppVpcStorage;
    private Map<String, Integer> previousNumberOfInstance;

    /*
        app vpc list used to collect all the selected and unselected app vpc and save it to database
     */
    private List<AppVpc> appVpcList;

    /*
        select app vpc list will contain all the selected app vpc for a environment
        after saving the selected appVpcs it will be passed to app environment helper
     */
    private List<AppVpc> selectedAppVpcList;

    /*
        unselect app vpc list will contain all the unselecred app vpc for a environment which were previously selected
        after saving the selected appVpcs it will be passed to app environment helper
    */
    private List<AppVpc> unselectedAppVpcList;

    @Autowired
    AppVpcRepository appVpcRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppVpcDTO) input;
        this.setOutput(output);
        this.appVpcList = new ArrayList<>();
        this.selectedAppVpcList = new ArrayList<>();
        this.unselectedAppVpcList = new ArrayList<>();

        this.previousAppVpcCPU = new HashMap<>();
        this.previousAppVpcMemory = new HashMap<>();
        this.previousAppVpcStorage = new HashMap<>();
        this.previousNumberOfInstance = new HashMap<>();
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
        /*
            Adding the selected appVpc for any environment to database
         */
        this.addAppVpcs();

        /*
            Removing the unselected appVpc for any environment from database
         */
        this.deleteUnselectedAppVpcs();

        appVpcRepository.saveAll(appVpcList);
        this.selectedAppVpcList = appVpcRepository.findAllByAppEnvironmentIdAndStatus(input.getAppEnvironment().getObjectId(), Status.V);
        CreateAppVpcResponseDTO createAppVpcResponseDTO = new CreateAppVpcResponseDTO();
        createAppVpcResponseDTO.setSelectedAppVpc(selectedAppVpcList);
        createAppVpcResponseDTO.setUnselectedAppVpc(unselectedAppVpcList);
        createAppVpcResponseDTO.setPreviousAppVpcCPU(previousAppVpcCPU);
        createAppVpcResponseDTO.setPreviousAppVpcMemory(previousAppVpcMemory);
        createAppVpcResponseDTO.setPreviousAppVpcStorage(previousAppVpcStorage);
        createAppVpcResponseDTO.setPreviousNumberOfInstance(previousNumberOfInstance);
        output.generateSuccessResponse(createAppVpcResponseDTO, "Clusters Are Successfully Created");
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        Add the selected app vpc for each selected app environment
     */
    private void addAppVpcs() {
        for (VpcResourceDetails inputVpcResourceDetails : input.getAppVpcResourceList()) {
            AppVpc appVpc;
            Optional<AppVpc> existingAppVpc = appVpcRepository.findByVpcIdAndAppEnvironmentIdAndStatus(inputVpcResourceDetails.getVpc().getObjectId(), input.getAppEnvironment().getObjectId(), Status.V);
            if (!existingAppVpc.isPresent()) {
                appVpc = new AppVpc();
                appVpc.setVpc(inputVpcResourceDetails.getVpc());
                appVpc.setApplicationId(input.getApplication().getObjectId());
                appVpc.setAppEnvironmentId(input.getAppEnvironment().getObjectId());
                appVpc.setDeploymentName(input.getApplication().getUniqueName() + "-" + input.getAppEnvironment().getEnvironment().getShortName().toLowerCase());
                appVpc.setCreatedBy(requester.getUsername());
                appVpc.setCreateDate(String.valueOf(LocalDateTime.now()));
                appVpc.setCreateActionId(actionId);
                appVpc.setStatus(Status.V);
            } else {
                appVpc = existingAppVpc.get();
                appVpc.setUpdatedBy(requester.getUsername());
                appVpc.setUpdateDate(String.valueOf(LocalDateTime.now()));
                appVpc.setLastUpdateActionId(actionId);
            }

            /*
                It will add the previous resource details to certain maps for updating the vpc in user management service
             */
            this.addPreviousResourceDetails(appVpc, inputVpcResourceDetails.getVpc());

            /*
                Adding resource details to each of the app vpc
             */
            appVpc.setResourceDetails(this.getResourceDetails(inputVpcResourceDetails));

             /*
                If ingress is selected by the user then a ingress url will generate for each app vpc
             */
            IngressConfig ingressConfig = appVpc.getIngressConfig() == null ? new IngressConfig() : appVpc.getIngressConfig();

            if (input.getApplication().isIngressEnabled()) {
                ingressConfig.setDefaultIngressUrl(this.generateUrl(appVpc));
                appVpc.setIngressConfig(ingressConfig);
            }

            /*
                If blue green deployment is selected by the user then a blue green deployment url will generate for each app vpc
             */
            if (inputVpcResourceDetails.isCanaryDeploymentEnabled()) {
                ingressConfig.setCanaryIngressUrl("canary-" + this.generateUrl(appVpc));
                appVpc.setIngressConfig(ingressConfig);
            }
            appVpc.setCanaryDeploymentEnabled(inputVpcResourceDetails.isCanaryDeploymentEnabled());

            /*
                Check if any appVpc id entered multiple times by the user
             */
            if (!containsDuplicateAppVpc(appVpcList, inputVpcResourceDetails.getVpc().getObjectId())) {
                appVpcList.add(appVpc);
            }
        }
    }


    /*
        Reduce the max number of cpu, memory and storage from available resources of a vpc
    */
    private void addPreviousResourceDetails(AppVpc appVpc, Vpc vpc) {
        int previousMaxCpu = 0, previousMaxMemory = 0, previousMaxStorage = 0, previousNumberOfInstance = 0;
        if (appVpc.getResourceDetails() != null) {
            previousMaxCpu = appVpc.getResourceDetails().getMaxCpu();
            previousMaxMemory = appVpc.getResourceDetails().getMaxMemory();
            previousMaxStorage = appVpc.getResourceDetails().getMaxStorage();
            previousNumberOfInstance = appVpc.getResourceDetails().isAutoScalingEnabled() ? appVpc.getResourceDetails().getMinNumOfInstance() : appVpc.getResourceDetails().getDesiredNumberOfInstance();
        }

        /*
              Setting the previous resource values for passing these to user management
              if vpc resource details don't exists then put zero on previous
         */
        this.previousAppVpcCPU.put(vpc.getName(), previousMaxCpu);
        this.previousAppVpcMemory.put(vpc.getName(), previousMaxMemory);
        this.previousAppVpcStorage.put(vpc.getName(), previousMaxStorage);
        this.previousNumberOfInstance.put(vpc.getName(), previousNumberOfInstance);
    }


    /*
        Remove the unselected app vpc which were previously selected for any app environment
    */
    private void deleteUnselectedAppVpcs() {
        List<ObjectId> appVpcObjectIdList = this.getAppVpcObjectIdList(input.getAppVpcResourceList());
        List<AppVpc> unSelectedAppVpcList = appVpcRepository.findByVpcIdNotInAndAppEnvironmentIdAndStatus(appVpcObjectIdList, input.getAppEnvironment().getObjectId(), Status.V);
        for (AppVpc appVpc : unSelectedAppVpcList) {
            appVpc.setStatus(Status.D);
            appVpc.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appVpc.setUpdatedBy(requester.getUsername());
            appVpc.setLastUpdateActionId(actionId);
            appVpcList.add(appVpc);

            //  for template creation
            AppVpc unselectedAppVpc = new AppVpc();
            unselectedAppVpc.setAppEnvironmentId(appVpc.getAppEnvironmentObjectId());
            unselectedAppVpc.setVpc(appVpc.getVpc());
            unselectedAppVpc.setApplicationId(appVpc.getApplicationId());
            unselectedAppVpc.setResourceDetails(appVpc.getResourceDetails());
            this.unselectedAppVpcList.add(unselectedAppVpc);
        }
    }

    /*
        Getting The Resource Details
    */
    private ResourceDetails getResourceDetails(VpcResourceDetails vpcResourceDetails) {
        ResourceDetails resourceDetails = new ResourceDetails();
        resourceDetails.setAutoScalingEnabled(vpcResourceDetails.isAutoScalingEnabled());
        if (vpcResourceDetails.isAutoScalingEnabled()) {
            resourceDetails.setMaxNumOfInstance(vpcResourceDetails.getMaxNumOfInstance());
            resourceDetails.setMinNumOfInstance(vpcResourceDetails.getMinNumOfInstance());
            resourceDetails.setDesiredNumberOfInstance(vpcResourceDetails.getMinNumOfInstance());
        } else {
            resourceDetails.setMaxNumOfInstance(vpcResourceDetails.getDesiredNumberOfInstance());
            resourceDetails.setMinNumOfInstance(vpcResourceDetails.getDesiredNumberOfInstance());
            resourceDetails.setDesiredNumberOfInstance(vpcResourceDetails.getDesiredNumberOfInstance());
        }
        resourceDetails.setMaxMemory(vpcResourceDetails.getMaxMemory());
        resourceDetails.setMinMemory(vpcResourceDetails.getMinMemory());
        resourceDetails.setMinCpu(vpcResourceDetails.getMinCpu());
        resourceDetails.setMaxCpu(vpcResourceDetails.getMaxCpu());
        resourceDetails.setMaxStorage(vpcResourceDetails.getMaxStorage());
        resourceDetails.setCpuThreshold(vpcResourceDetails.getCpuThreshold());
        resourceDetails.setTransactionPerSecondThreshold(vpcResourceDetails.getTransactionPerSecondThreshold());
        return resourceDetails;
    }

    /*
        Check if any appVpc id entered multiple times by the user
     */
    private boolean containsDuplicateAppVpc(List<AppVpc> appVpcList, ObjectId appVpcId) {
        for (AppVpc appVpc : appVpcList) {
            if (appVpc.getVpc().getObjectId().toString().equals(appVpcId.toString())) {
                return true;
            }
        }
        return false;
    }

    private String generateUrl(AppVpc appVpc) {
        String defaultDomain = "cloudslip.io";
        return appVpc.getDeploymentName() + "." + defaultDomain;
    }

    /*
        Get the object id list from list of appVpc resource object
     */
    private List<ObjectId> getAppVpcObjectIdList(List<VpcResourceDetails> vpcResourceDetailsList) {
        if (vpcResourceDetailsList.isEmpty()) {
            return null;
        }
        List<ObjectId> appVpcObjectIdList = new ArrayList<>();
        for (VpcResourceDetails vpcResourceDetails : vpcResourceDetailsList) {
            appVpcObjectIdList.add(vpcResourceDetails.getVpc().getObjectId());
        }
        return appVpcObjectIdList;
    }
}
