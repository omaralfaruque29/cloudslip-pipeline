package com.cloudslip.pipeline.updated.helper.app_vpc;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeleteAllAppVpcHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppVpcRepository appVpcRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
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
        List<AppVpc> appVpcList = appVpcRepository.findAllByAppEnvironmentIdAndStatus(input.getId(), Status.V);
        for (AppVpc appVpc : appVpcList) {
            appVpc.setStatus(Status.D);
            appVpc.setUpdateDate(String.valueOf(LocalDateTime.now()));
            appVpc.setUpdatedBy(requester.getUsername());
            appVpc.setLastUpdateActionId(actionId);
            appVpcRepository.save(appVpc);
            this.updateResourceInAllExistingAppVpcAndAppEnv(appVpc);
        }
        output.generateSuccessResponse(null,  " application Vpc List Deleted Successfully");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    /*
        Vpc List with updated resource details are added to existing app vpc and app environments
    */
    private void updateResourceInAllExistingAppVpcAndAppEnv(AppVpc unelectedAppVpc) {
        List<AppEnvironment> allAppEnvironmentList = appEnvironmentRepository.findAllByAppVpcListVpcIdAndStatusAndApplicationIdNotIn(unelectedAppVpc.getVpc().getObjectId(), Status.V, unelectedAppVpc.getApplicationId());
        for (AppEnvironment appEnvironment : allAppEnvironmentList) {
            Optional<AppVpc> existingAppVpc = appVpcRepository.findByVpcIdAndAppEnvironmentIdAndStatus(unelectedAppVpc.getVpc().getObjectId(),appEnvironment.getObjectId(), Status.V);
            if (existingAppVpc.isPresent()) {
                int appVpcIndex = appEnvironment.getAppVpcIndex(existingAppVpc.get().getObjectId());
                int removedMaxCpu = 0, removedMaxMemory = 0, removedMaxStorage = 0, removedNumberOfInstance = 0;
                if (unelectedAppVpc.getResourceDetails() != null) {
                    removedMaxCpu = unelectedAppVpc.getResourceDetails().getMaxCpu();
                    removedMaxMemory = unelectedAppVpc.getResourceDetails().getMaxMemory();
                    removedMaxStorage = unelectedAppVpc.getResourceDetails().getMaxStorage();
                    removedNumberOfInstance = unelectedAppVpc.getResourceDetails().isAutoScalingEnabled() ? unelectedAppVpc.getResourceDetails().getMinNumOfInstance() : unelectedAppVpc.getResourceDetails().getDesiredNumberOfInstance();
                }
                existingAppVpc.get().getVpc().setAvailableCPU(existingAppVpc.get().getVpc().getAvailableCPU() + (removedMaxCpu * removedNumberOfInstance));
                existingAppVpc.get().getVpc().setAvailableMemory(existingAppVpc.get().getVpc().getAvailableMemory() + (removedMaxMemory * removedNumberOfInstance));
                existingAppVpc.get().getVpc().setAvailableStorage(existingAppVpc.get().getVpc().getAvailableStorage() + (removedMaxStorage * removedNumberOfInstance));

                AppVpc savedAppVpc = appVpcRepository.save(existingAppVpc.get());
                if (appVpcIndex != -1) {
                    appEnvironment.getAppVpcList().set(appVpcIndex, savedAppVpc);
                    appEnvironmentRepository.save(appEnvironment);
                }
            }
        }
    }
}
