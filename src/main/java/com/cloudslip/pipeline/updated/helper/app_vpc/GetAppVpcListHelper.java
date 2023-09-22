package com.cloudslip.pipeline.updated.helper.app_vpc;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GetAppVpcListHelper extends AbstractHelper {

    private GetListFilterInput input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<Application> application;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppVpcRepository appVpcRepository;

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
        List<AppVpc> appVpcList = appVpcRepository.findAllByApplicationIdAndStatus(application.get().getObjectId(), Status.V);
        output.generateSuccessResponse(appVpcList, "application & application environment response");
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
