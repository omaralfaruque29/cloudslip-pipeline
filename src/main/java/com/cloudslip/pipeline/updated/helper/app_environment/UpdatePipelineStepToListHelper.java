package com.cloudslip.pipeline.updated.helper.app_environment;

import com.cloudslip.pipeline.updated.dto.AddPipelineStepToListDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.AppPipelineStep;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UpdatePipelineStepToListHelper extends AbstractHelper {

    private AddPipelineStepToListDTO input;
    private ResponseDTO output = new ResponseDTO();

    private Optional<AppEnvironment> appEnvironment;

    @Autowired
    AppEnvironmentRepository appEnvironmentRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (AddPipelineStepToListDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
        this.appEnvironment = appEnvironmentRepository.findByIdAndStatus(input.getAppEnvironmentId(), Status.V);
        if (!appEnvironment.isPresent()) {
            output.generateErrorResponse("application Environment Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void doPerform() {
        List<AppPipelineStep> appPipelineStepList = appEnvironment.get().getAppPipelineStepList();

    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
