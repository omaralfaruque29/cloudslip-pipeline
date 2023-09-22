package com.cloudslip.pipeline.updated.helper.application;

import com.cloudslip.pipeline.updated.dto.application.ApplicationAndAppVpcListByVpcResponseDTO;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.Application;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GetApplicationAndAppVpcListByVpcHelper extends AbstractHelper {

    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();

    @Autowired
    private AppVpcRepository appVpcRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || requester.hasAuthority(Authority.ANONYMOUS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
     }


    protected void doPerform() {
        List<AppVpc> appVpcList = appVpcRepository.findAllByVpcIdAndStatus(input.getId(), Status.V);
        List<ApplicationAndAppVpcListByVpcResponseDTO> applicationListByVpcDTOAndAppVpcListResponse = new ArrayList<>();
        for (AppVpc appVpc : appVpcList) {
            Optional<Application> application = applicationRepository.findByIdAndStatus(appVpc.getApplicationId(), Status.V);
            if (application.isPresent() && checkAuthority(application.get())) {
                applicationListByVpcDTOAndAppVpcListResponse.add(new ApplicationAndAppVpcListByVpcResponseDTO(application.get(), appVpc));
            }
        }
        output.generateSuccessResponse(applicationListByVpcDTOAndAppVpcListResponse, "application List By Vpc");
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
