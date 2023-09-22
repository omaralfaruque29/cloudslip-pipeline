package com.cloudslip.pipeline.updated.helper.app_secrets;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppEnvironment;
import com.cloudslip.pipeline.updated.model.dummy.AppSecretEnvironment;
import com.cloudslip.pipeline.updated.repository.AppEnvironmentRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAppSecretEnvironmentListHelper extends AbstractHelper {
    private final Logger log = LoggerFactory.getLogger(GetAppSecretEnvironmentListHelper.class);
    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private ObjectId applicationId;
    private List<AppEnvironment> appEnvironmentList;
    private List<AppSecretEnvironment> appSecretEnvironmentList;


    @Autowired
    private AppEnvironmentRepository appEnvironmentRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        applicationId = ((GetObjectInputDTO) input).getId();
        appEnvironmentList = new ArrayList<>();
        appSecretEnvironmentList = new ArrayList<>();
        this.setOutput(output);
    }

    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_ADMIN, Authority.ROLE_DEV,Authority.ROLE_OPS)) {
            output.generateErrorResponse("Unauthorized user !! Only User Role Admin,Dev,Ops Have Access!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }


    protected void checkValidity() {
    }


    protected void doPerform() {
        appEnvironmentList = appEnvironmentRepository.findAllByApplicationIdAndStatusOrderByEnvironment_OrderNo(applicationId, Status.V);

        if(appEnvironmentList.size() > 0) {
            for(int i = 0; i < appEnvironmentList.size(); i++){
                appSecretEnvironmentList.add(new AppSecretEnvironment(appEnvironmentList.get(i).getId(),appEnvironmentList.get(i).getEnvironment().getName()));
            }
        }

        output.generateSuccessResponse(appSecretEnvironmentList);
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }
}
