package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetApplicationTeamHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<Application> application;

    @Autowired
    private ApplicationRepository applicationRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
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
        application = applicationRepository.findByIdAndStatus(input.getId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application not found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!checkAuthority(application.get())) {
            output.generateErrorResponse("User do not have authority to access the application!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        output.generateSuccessResponse(application.get().getTeam(), "application team response");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean checkAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) &&!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString());
        } else if (!requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }
}
