package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IsUserAllowedToSubscribeToApplicationWebSocketTopicHelper extends AbstractHelper {

    private IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<Application> application;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ObjectMapper objectMapper;


    public void init(BaseInput input, Object... extraParams) {
        this.input = (IsUserAllowedToSubscribeToApplicationWebSocketTopicInputDTO) input;
        this.setOutput(output);
        this.application = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_GIT_AGENT) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if(input.getWebSocketTopic() == null) {
            output.generateErrorResponse("Web Socket Topic name is missing in the input");
            throw new ApiErrorException(this.getClass().getName());
        }
        application = applicationRepository.findByWebSocketTopicAndStatus(input.getWebSocketTopic(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        boolean result = isUserAllowedToSubscribe(application.get());
        output.generateSuccessResponse(result);
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean isUserAllowedToSubscribe(Application application) {
        if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            if (!application.getTeam().getCompanyObjectId().equals(this.requester.getCompanyId())) {
                return false;
            }
        }
        if (requester.hasAuthority(Authority.ROLE_DEV) || requester.hasAuthority(Authority.ROLE_OPS)) {
            if (!application.getTeam().existInTeamIdList(this.requester.getTeamIdList())) {
                return false;
            }
        }
        return true;
    }
}
