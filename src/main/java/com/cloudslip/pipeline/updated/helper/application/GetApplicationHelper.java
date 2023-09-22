package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.constant.ApplicationProperties;
import com.cloudslip.pipeline.updated.core.CustomRestTemplate;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.UserInfoResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.ResponseStatus;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.util.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetApplicationHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    Optional<Application> application;

    @Autowired
    ApplicationRepository applicationRepository;

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
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (!checkAuthority(application.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        output.generateSuccessResponse(application);
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
