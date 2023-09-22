package com.cloudslip.pipeline.updated.helper.app_secrets;

import com.cloudslip.pipeline.updated.constant.ListFetchMode;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.GetListFilterInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.model.AppSecret;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import com.cloudslip.pipeline.updated.repository.AppSecretRepository;
import com.cloudslip.pipeline.updated.util.AES256;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAppSecretListByApplicationHelper extends AbstractHelper {
    private final Logger log = LoggerFactory.getLogger(GetAppSecretListByApplicationHelper.class);
    private GetListFilterInput input;
    private ResponseDTO output = new ResponseDTO();
    private Pageable pageable;
    private ObjectId applicationId;
    List<AppSecret> appSecretList;
    Page<AppSecret> appSecretPage = null;


    @Autowired
    private AppSecretRepository appSecretsRepository;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (GetListFilterInput) input;
        this.pageable = (Pageable) extraParams[0];
        this.applicationId = (ObjectId) extraParams[1];
        appSecretList = new ArrayList<>();
        this.setOutput(output);
    }


    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_ADMIN, Authority.ROLE_DEV,Authority.ROLE_OPS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void checkValidity() {
    }


    protected void doPerform() {

        if (input.getFetchMode() == null || input.getFetchMode().equals(ListFetchMode.PAGINATION)) {
            appSecretPage = appSecretsRepository.findByApplicationIdAndCompanyIdAndStatus(pageable, applicationId, requester.getCompanyId(), Status.V);
            if(appSecretPage != null && appSecretPage.getContent().size() > 0) {
                for (int i = 0; i < appSecretPage.getContent().size(); i++) {
                    appSecretPage.getContent().get(i).setDataList(dataListValueAESConversionToString(appSecretPage.getContent().get(i).getDataList()));
                }
            }
            output.generateSuccessResponse(appSecretPage);
        } else if (input.getFetchMode() != null || input.getFetchMode().equals(ListFetchMode.ALL)) {
            appSecretList = appSecretsRepository.findByCompanyIdAndApplicationIdAndStatus(requester.getCompanyId(), applicationId, Status.V);
            if(appSecretList.size() > 0) {
                for (int i = 0; i < appSecretList.size(); i++) {
                    appSecretList.get(i).setDataList(dataListValueAESConversionToString(appSecretList.get(i).getDataList()));
                }
            }
            output.generateSuccessResponse(appSecretList);
        }
         else {
            output.generateErrorResponse("Invalid Params In Fetch Mode");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {

    }

    private List<NameValue> dataListValueAESConversionToString(List<NameValue> dataList){
        for(int i = 0; i < dataList.size(); i++){
            dataList.get(i).setValue( AES256.decrypt(dataList.get(i).getValue()));
        }
        return dataList;
    }

}
