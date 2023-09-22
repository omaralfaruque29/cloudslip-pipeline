package com.cloudslip.pipeline.updated.helper.app_pipe_line_step;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.enums.TriggerMode;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.model.dummy.SuccessorPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import com.cloudslip.pipeline.updated.repository.AppPipelineStepRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AutoSuccessorAddHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppEnvironmentRepository appEnvironmentRepository;

    @Autowired
    private AppPipelineStepRepository appPipelineStepRepository;

    private Optional<Application> application;

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
        application = applicationRepository.findByIdAndStatus(input.getId(), Status.V);
        if (!application.isPresent()) {
            output.generateErrorResponse("application Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }  else if (!checkAuthority(application.get())) {
            output.generateErrorResponse("Unauthorized User!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        List<AppEnvironment> appEnvironmentList = appEnvironmentRepository.findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(application.get().getObjectId(), Status.V);
        for (AppEnvironment appEnvironment : appEnvironmentList) {
            if (appEnvironment.getAppPipelineStepList() != null) {
                List<AppPipelineStep> appPipelineStepList = appEnvironment.getAppPipelineStepList();
                for (int index = 0; index < appPipelineStepList.size(); index++) {
                    List<SuccessorPipelineStep> successorPipelineSteps = new ArrayList<>();
                    AppPipelineStep appPipelineStep = appPipelineStepList.get(index);
                    if (index < appPipelineStepList.size() -1) { // if there is another pipeline step in same env
                        successorPipelineSteps.add(new SuccessorPipelineStep(appPipelineStepList.get(index + 1), TriggerMode.MANUAL));
                    } else { // look for next environment
                        Optional<AppEnvironment> nextExistingAppEnv = appEnvironmentRepository.findFirstByApplicationIdAndStatusAndEnvironment_OrderNoGreaterThanAndAppPipelineStepListNotNullOrderByEnvironment_OrderNo(application.get().getObjectId(), Status.V, appEnvironment.getEnvironment().getOrderNo());
                        if (nextExistingAppEnv.isPresent()) {
                            successorPipelineSteps.add(new SuccessorPipelineStep(nextExistingAppEnv.get().getAppPipelineStepList().get(0), TriggerMode.MANUAL));
                        }
                    }
                    appPipelineStep.setSuccessors(successorPipelineSteps);
                    appPipelineStep = appPipelineStepRepository.save(appPipelineStep);
                    appPipelineStepList.set(index, appPipelineStep);
                }
                appEnvironment.setAppPipelineStepList(appPipelineStepList);
                appEnvironmentRepository.save(appEnvironment);
            }
        }
        output.generateSuccessResponse(null,  "Auto pipeline successor added");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private boolean checkAuthority(Application application) {
        if (requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().getCompanyObjectId().toString().equals(requester.getCompanyId().toString());
        } else if (!requester.hasAuthority(Authority.ROLE_ADMIN) && !requester.hasAuthority(Authority.ROLE_SUPER_ADMIN)) {
            return application.getTeam().existInTeamIdList(requester.getTeamIdList());
        }
        return true;
    }
}
