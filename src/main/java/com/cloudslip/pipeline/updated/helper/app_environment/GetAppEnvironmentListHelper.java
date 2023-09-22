package com.cloudslip.pipeline.updated.helper.app_environment;

import com.cloudslip.pipeline.updated.dto.*;
import com.cloudslip.pipeline.updated.dto.app_environment.GetAppEnvironmentResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAppEnvironmentListHelper extends AbstractHelper {

    private GetListFilterInput input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<Application> application;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetListFilterInput) input;
        this.setOutput(output);
        this.application = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if(input.getFilterParamsMap().containsKey("appId")) {
            ObjectId appId;
            try {
                appId = new ObjectId(input.getFilterParamsMap().get("appId"));
            } catch (Exception e) {
                output.generateErrorResponse("Invalid Parameter!");
                throw new ApiErrorException(this.getClass().getName());
            }
            application = applicationRepository.findByIdAndStatus(appId, Status.V);
            if(!application.isPresent()) {
                output.generateErrorResponse("application Not Found!");
                throw new ApiErrorException(this.getClass().getName());
            }
            if (!this.validateAuthority(application.get())) {
                output.generateErrorResponse("User do not have authority to access the application!");
                throw new ApiErrorException(this.getClass().getName());
            }
        } else {
            output.generateErrorResponse("application Must Be Provided In Params");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        GetAppEnvironmentResponseDTO getAppEnvironmentResponseDTO = new GetAppEnvironmentResponseDTO();
        getAppEnvironmentResponseDTO.setApplication(application.get());
        getAppEnvironmentResponseDTO.setAppEnvironmentList(appEnvironmentRepository.findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(application.get().getObjectId(), Status.V));
        output.generateSuccessResponse(getAppEnvironmentResponseDTO, "application & application environment response");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean validateAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return requester.getCompanyId().toString().equals(application.getTeam().getCompanyId());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && !requester.hasAuthority(Authority.ROLE_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }
}
