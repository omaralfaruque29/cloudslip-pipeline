package com.cloudslip.pipeline.updated.helper.app_secrets;
import com.cloudslip.pipeline.updated.dto.BaseInput;
import com.cloudslip.pipeline.updated.dto.ResponseDTO;
import com.cloudslip.pipeline.updated.dto.app_secrets.CreateAppSecretDTO;
import com.cloudslip.pipeline.updated.dto.kubeconfig.SecretConfig;
import com.cloudslip.pipeline.updated.enums.AgentCommand;
import com.cloudslip.pipeline.updated.enums.Authority;
import com.cloudslip.pipeline.updated.enums.Status;
import com.cloudslip.pipeline.updated.exception.model.ApiErrorException;
import com.cloudslip.pipeline.updated.helper.AbstractHelper;
import com.cloudslip.pipeline.updated.kafka.KafkaPublisher;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessage;
import com.cloudslip.pipeline.updated.kafka.dto.KafkaMessageHeader;
import com.cloudslip.pipeline.updated.model.AppSecret;
import com.cloudslip.pipeline.updated.model.AppVpc;
import com.cloudslip.pipeline.updated.model.dummy.NameValue;
import com.cloudslip.pipeline.updated.model.universal.Vpc;
import com.cloudslip.pipeline.updated.repository.AppSecretRepository;
import com.cloudslip.pipeline.updated.repository.AppVpcRepository;
import com.cloudslip.pipeline.updated.util.AES256;
import com.cloudslip.pipeline.updated.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CreateAppSecretHelper extends AbstractHelper {

    private final Logger log = LoggerFactory.getLogger(CreateAppSecretHelper.class);
    @Autowired
    private AppSecretRepository appSecretRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private AppVpcRepository appVpcRepository;

    private CreateAppSecretDTO input;
    private ResponseDTO output = new ResponseDTO();
    List<NameValue> encryptedDataList;
    private AppSecret newAppSecret;
    private String uniqueName;

    public void init(BaseInput input, Object... extraParams) {
        this.input = (CreateAppSecretDTO) input;
        encryptedDataList = new ArrayList<>();
        this.setOutput(output);
    }

    protected void checkPermission() {
        if (requester == null || !requester.hasAnyAuthority(Authority.ROLE_SUPER_ADMIN, Authority.ROLE_ADMIN,Authority.ROLE_DEV,Authority.ROLE_OPS)) {
            output.generateErrorResponse("Unauthorized user!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void checkValidity() {
        if (input.getApplicationId() == null) {
            output.generateErrorResponse("Application Id is required!");
            throw new ApiErrorException(this.getClass().getName());
        } else if (requester.getCompanyId() == null){
            output.generateErrorResponse("You are not assigned to any company.Make sure you are a company user!");
            throw new ApiErrorException(this.getClass().getName());
        }
        Optional<AppSecret> existingAppSecret = appSecretRepository.findByApplicationIdAndCompanyIdAndSecretNameAndStatus(input.getApplicationId(),requester.getCompanyId(),input.getSecretName(), Status.V);
        if (existingAppSecret.isPresent()) {
            output.generateErrorResponse("Duplicate App secrets name found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    protected void doPerform() {
        uniqueName = input.getSecretName().trim().replaceAll("_+", "-");
        uniqueName =  Utils.removeAllSpaceWithDash(uniqueName + "-" + Utils.generateRandomString(10)).toLowerCase().trim().replaceAll("-+", "-");
        newAppSecret = new AppSecret();
        newAppSecret.setApplicationId(input.getApplicationId());
        newAppSecret.setCompanyId(requester.getCompanyId());
        newAppSecret.setSecretName(input.getSecretName());
        newAppSecret.setUniqueName(uniqueName);
        newAppSecret.setEnvironmentList(input.getEnvironmentList());
        newAppSecret.setUseAsEnvironmentVariable(input.isUseAsEnvironmentVariable());
        dataListValueConversionToAESandBase64Encode(input.getDataList());
        newAppSecret.setDataList(encryptedDataList);
        newAppSecret.setCreatedBy(requester.getUsername());
        newAppSecret.setCreateDate(String.valueOf(LocalDateTime.now()));
        newAppSecret.setCreateActionId(actionId);
        try {
            generateAppSecretConfigAndPublishInKafka(uniqueName);
        }catch (Exception ex){
            output.generateErrorResponse("Error Occurs in Publishing KafkaMessage For Create Secret!");
            throw new ApiErrorException(this.getClass().getName());
        }
        appSecretRepository.save(newAppSecret);
        output.generateSuccessResponse("App Secrets is created");
    }

    private void generateAppSecretConfigAndPublishInKafka(String uniqueName) {
        List<AppVpc> appVpcList = appVpcRepository.findAllByApplicationIdAndStatus(input.getApplicationId(), Status.V);
        if(appVpcList.size() > 0) {
            List<Vpc> vpcList = new ArrayList<>();
            for (AppVpc appVpc : appVpcList) {
                for (int i = 0; i < input.getEnvironmentList().size(); i++) {
                    if (input.getEnvironmentList().get(i).getAppEnvironmentId().equals(appVpc.getAppEnvironmentId())) {
                        if(vpcList.size() > 0) {
                            generateAppVpcList(vpcList, appVpc.getVpc());
                        } else {
                            vpcList.add(appVpc.getVpc());
                        }
                    }
                }
            }
            for (Vpc vpc : vpcList) {
                SecretConfig secretConfig = new SecretConfig();
                secretConfig.setSecretName(uniqueName);
                secretConfig.setNamespace(vpc.getNamespace());
                secretConfig.setData(input.getDataList());
                publishKafkaMessageForCreatingSecret(vpc.getId(), vpc.getKubeCluster().getKafkaTopic(), secretConfig);
            }
        } else {
            output.generateErrorResponse("App Vpc not found!");
            throw new ApiErrorException(this.getClass().getName());
        }
    }

    private void publishKafkaMessageForCreatingSecret(String vpcId, String targetVpcKafkaTopic, SecretConfig secretConfig) {
        KafkaMessageHeader header = new KafkaMessageHeader(secretConfig.getNamespace(), AgentCommand.CREATE_SECRET.toString());
        header.setCompanyId(requester.getCompanyId().toHexString());
        header.addToExtra("vpcId", vpcId);
        KafkaMessage<SecretConfig> message = new KafkaMessage(header, secretConfig);
        this.kafkaPublisher.publishMessage(targetVpcKafkaTopic, message);
    }

    protected void postPerformCheck() {
    }

    protected void doRollback() {
    }

    private void dataListValueConversionToAESandBase64Encode(List<NameValue> dataList){
        for(int i = 0; i < dataList.size(); i++){
            encryptedDataList.add(new NameValue(input.getDataList().get(i).getName(),AES256.encrypt(input.getDataList().get(i).getValue())));
            input.getDataList().get(i).setValue( Utils.getBase64EncodedString(input.getDataList().get(i).getValue()));
        }
    }

    private List<Vpc> generateAppVpcList(List<Vpc> vpcList, Vpc vpc){
        for(int j=0 ; j < vpcList.size(); j++) {
            if (vpcList.get(j).getId().equals(vpc.getId())) {
                return vpcList;
            }
        }
        vpcList.add(vpc);
        return vpcList;
    }
}
