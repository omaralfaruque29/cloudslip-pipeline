package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.UpdateApplicationWebSocketSubscriberCountInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.enums.UpdateApplicationWebSocketSubscriberCountType;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import com.cloudslip.pipeline.updated.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UpdateApplicationWebSocketSubscriberCountHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private UpdateApplicationWebSocketSubscriberCountInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<Application> application;

    @Autowired
    private ApplicationRepository applicationRepository;



    public void init(BaseInput input, Object... extraParams) {
        this.input = (UpdateApplicationWebSocketSubscriberCountInputDTO) input;
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
        checkAuthority(application.get());
    }


    protected void doPerform() {
        log.info("Performing {} web socket subscriber count for {}", input.getType(), input.getWebSocketTopic());
        int newWebSocketSubscriberCount = application.get().getWebSocketSubscriberCount();
        if(input.getType() == UpdateApplicationWebSocketSubscriberCountType.INCREASE) {
            if(newWebSocketSubscriberCount < 0) {
                newWebSocketSubscriberCount = 1;
            } else {
                newWebSocketSubscriberCount++;
            }
        } else if(input.getType() == UpdateApplicationWebSocketSubscriberCountType.DECREASE) {
            newWebSocketSubscriberCount--;
            if(newWebSocketSubscriberCount < 0) {
                newWebSocketSubscriberCount = 0;
            }
        } else {
            output.generateErrorResponse("Invalid UpdateApplicationWebSocketSubscriberCountType provided!");
            throw new ApiErrorException(this.getClass().getName());
        }

        log.info("Web socket subscriber count for {}, old value: {}, updated value: {}", input.getWebSocketTopic(), application.get().getWebSocketSubscriberCount(), newWebSocketSubscriberCount);

        application.get().setWebSocketSubscriberCount(newWebSocketSubscriberCount);
        application.get().setUpdatedBy(requester.getUsername());
        application.get().setUpdateDate(String.valueOf(LocalDateTime.now()));
        applicationRepository.save(application.get());
        output.generateSuccessResponse(newWebSocketSubscriberCount, "application Web Socket Subscriber Count updated");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean checkAuthority(Application application) {
        if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            if (!application.getTeam().getCompanyObjectId().equals(this.requester.getCompanyId())) {
                output.generateErrorResponse("Unauthorized user!");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
        if (requester.hasAuthority(Authority.ROLE_DEV) || requester.hasAuthority(Authority.ROLE_OPS)) {
            if (!application.getTeam().existInTeamIdList(this.requester.getTeamIdList())) {
                output.generateErrorResponse("");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
        return true;
    }
}
