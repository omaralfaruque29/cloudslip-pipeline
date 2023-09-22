package com.cloudslip.pipeline.updated.helper.app_commit;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.CreateAppCommitDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppCommit;
import com.cloudslip.pipeline.updated.repository.AppCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateAppCommitHelper extends AbstractHelper {

    private CreateAppCommitDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    AppCommitRepository appCommitRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppCommitDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS) || requester.hasAuthority(Authority.ROLE_AGENT_SERVICE)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
    }


    protected void doPerform() {
        AppCommit appCommit = new AppCommit();
        appCommit.setApplicationId(input.getApplicationId());
        appCommit.setGitCommitId(input.getCommitId());
        appCommit.setCommitMessage(input.getCommitMessage());
        appCommit.setCommitDate(input.getCommitDate());
        appCommit.setStatus(Status.V);
        output.generateSuccessResponse(appCommitRepository.save(appCommit), "App Commit Successfully Created");
    }



    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
