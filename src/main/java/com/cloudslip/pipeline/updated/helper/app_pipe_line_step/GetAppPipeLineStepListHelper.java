package com.cloudslip.pipeline.updated.helper.app_pipe_line_step;

import com.cloudslip.pipeline.updated.constant.ListFetchMode;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAppPipeLineStepListHelper extends AbstractHelper {

    private GetListFilterInput input;
    private ResponseDTO output = new ResponseDTO();
    private Pageable pageable;

    private Optional<AppEnvironment> appEnvironment;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    AppPipelineStepRepository appPipelineStepRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetListFilterInput) input;
        this.pageable = (Pageable) extraParams[0];
        this.setOutput(output);
        this.appEnvironment = null;
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        if(input.getFilterParamsMap().containsKey("appEnvId")) {
            ObjectId appEnvId;
            try {
                appEnvId = new ObjectId(input.getFilterParamsMap().get("appEnvId"));
            } catch (Exception e) {
                output.generateErrorResponse("Invalid Parameter!");
                throw new ApiErrorException(this.getClass().getName());
            }
            appEnvironment = appEnvironmentRepository.findById(appEnvId);
            if(!appEnvironment.isPresent()) {
                output.generateErrorResponse("application Environment Not Found!");
                throw new ApiErrorException(this.getClass().getName());
            }  else if (!checkAuthority(appEnvironment.get())){
                output.generateErrorResponse("Unauthorized User!");
                throw new ApiErrorException(this.getClass().getName());
            }
        }
    }


    protected void doPerform() {
        if(requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            this.fetchForSuperAdmin();
        } else {
            this.fetchForOtherUser();
        }
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void fetchForSuperAdmin() {
        if (appEnvironment == null) {
            this.fetchAll();
        } else {
            this.fetchByAppEnvironment(appEnvironment.get().getObjectId());
        }
    }

    private void fetchForOtherUser() {
        if (appEnvironment == null) {
            output.generateErrorResponse("application Environment Must Be Provided In Params");
            throw new ApiErrorException(this.getClass().getName());
        } else {
            this.fetchByAppEnvironment(appEnvironment.get().getObjectId());
        }
    }

    private  void fetchAll() {
        if(input.getFetchMode() == null || input.getFetchMode().equals(ListFetchMode.PAGINATION)) {
                output.generateSuccessResponse(appPipelineStepRepository.findAllByStatus(pageable, Status.V));
        } else if(input.getFetchMode() != null || input.getFetchMode().equals(ListFetchMode.ALL)) {
                output.generateSuccessResponse(appPipelineStepRepository.findAllByStatus(Status.V));
        } else {
            output.generateErrorResponse("Invalid params in fetch mode");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void fetchByAppEnvironment(ObjectId appEnvId) {
        if(input.getFetchMode() == null || input.getFetchMode().equals(ListFetchMode.PAGINATION)) {
            output.generateSuccessResponse(appPipelineStepRepository.findAllByAppEnvironmentIdAndStatus(pageable, appEnvId, Status.V));
        } else if(input.getFetchMode() != null || input.getFetchMode().equals(ListFetchMode.ALL)) {
            output.generateSuccessResponse(appPipelineStepRepository.findAllByAppEnvironmentIdAndStatus(appEnvId, Status.V));
        } else {
            output.generateErrorResponse("Invalid params in fetch mode");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private boolean checkAuthority(AppEnvironment appEnvironment) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return requester.getCompanyId().toString().equals(appEnvironment.getCompanyId());
        } else if (!requester.hasAuthority(Authority.ROLE_SUPER_ADMIN) && !requester.hasAuthority(Authority.ROLE_ADMIN)) {
            Optional<Application> application = applicationRepository.findByIdAndStatus(appEnvironment.getApplicationObjectId(), Status.V);
            return application.get().getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }
}
