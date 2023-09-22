package com.cloudslip.pipeline.updated.helper.app_secrets;

import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetObjectInputDTO;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppSecret;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import com.cloudslip.pipeline.updated.repository.AppSecretRepository;
import com.cloudslip.pipeline.updated.util.AES256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class GetAppSecretHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(GetAppSecretHelper.class);
    @Autowired
    private AppSecretRepository appSecretsRepository;
    private GetObjectInputDTO input;
    private ResponseDTO output = new ResponseDTO();
    private Optional<AppSecret> appSecret;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetObjectInputDTO) input;
        this.setOutput(output);
    }

    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void checkValidity() {
        appSecret = appSecretsRepository.findByIdAndStatus(input.getId(), Status.V);
        if(!appSecret.isPresent()) {
            output.generateErrorResponse("App Secrets Not Found!");
            throw new ApiErrorException(this.getClass().getName());
        }
        dataListValueAESConversionToString();
    }

    protected void doPerform() {
        output.generateSuccessResponse(appSecret.get(), "Application Secrets Response");
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private void dataListValueAESConversionToString(){
        for(int i = 0; i < appSecret.get().getDataList().size(); i++){
            appSecret.get().getDataList().get(i).setValue( AES256.decrypt(appSecret.get().getDataList().get(i).getValue()));
        }
    }
}
